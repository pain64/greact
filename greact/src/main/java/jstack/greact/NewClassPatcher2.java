package jstack.greact;

import jstack.jscripter.transpiler.model.JSExpression;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import jstack.greact.dom.GReact;
import jstack.greact.dom.HTMLNativeElements;
import jstack.greact.dom.Node;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NewClassPatcher2 {

    final Symtab symtab;
    final Names names;
    final TreeMaker maker;
    final Types types;
    final Util util;
    final ViewUpdateStrategy viewUpdateStrategy;
    final Name rootVarName;
    final Name defaultConstructorMethodName;

    class Symbols {
        Symbol.ClassSymbol clString = util.lookupClass(String.class);
        Symbol.ClassSymbol clObject = util.lookupClass(Object.class);
        Symbol.ClassSymbol clGReact = util.lookupClass(GReact.class);
        Symbol.ClassSymbol clSlot = util.lookupClass(HTMLNativeElements.slot.class);
        Symbol.MethodSymbol mtGReactMMount = util.lookupMember(clGReact, "mmount");
        Symbol.MethodSymbol mtGReactReplace = util.lookupMember(clGReact, "replace");
        Symbol.ClassSymbol clRunnable = util.lookupClass(Runnable.class);
        Symbol.MethodSymbol mtRunnableRun = util.lookupMember(clRunnable, "run");
        Symbol.ClassSymbol clNode = util.lookupClass(Node.class);
        Symbol.MethodSymbol mtNodeAppendChild = util.lookupMember(clNode, "appendChild");
        Symbol.ClassSymbol clAsyncRunnable = util.lookupClass(GReact.AsyncRunnable.class);
        Symbol.MethodSymbol mtAsyncRunnableRun = util.lookupMember(clAsyncRunnable, "run");
        Symbol.ClassSymbol clAsyncCallable = util.lookupClass(GReact.AsyncCallable.class);
        Symbol.MethodSymbol mtAsyncCallableCall = util.lookupMember(clAsyncCallable, "call");
        Symbol.ClassSymbol clJSExpression = util.lookupClass(JSExpression.class);
        Symbol.MethodSymbol mtJSExpressionOf = util.lookupMember(clJSExpression, "of");
    }

    final Symbols symbols;

    public NewClassPatcher2(Context context) {
        this.maker = TreeMaker.instance(context);
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.util = new Util(context);
        this.viewUpdateStrategy = new ViewUpdateStrategy();
        this.symbols = new Symbols();
        this.rootVarName = names.fromString("_root");
        this.defaultConstructorMethodName = names.fromString("<init>");
    }

    JCTree.JCExpression buildStatic(Symbol sym) {
        return sym.owner instanceof Symbol.RootPackageSymbol
            ? maker.Ident(sym)
            : maker.Select(buildStatic(sym.owner), sym);
    }

    JCTree.JCMethodInvocation makeCall(Symbol self, Symbol.MethodSymbol method, com.sun.tools.javac.util.List<JCTree.JCExpression> args) {
        var select = (self instanceof Symbol.ClassSymbol || self.isStatic())
            ? maker.Select(buildStatic(self), method)
            : maker.Select(maker.Ident(self), method);

        return maker.App(select, args);
    }

    Symbol.MethodSymbol extractActualConstructor(JCTree.JCNewClass newClass) {
        if (newClass.def == null)
            return (Symbol.MethodSymbol) newClass.constructor;

        var syntheticConstructor = newClass.def.defs.stream()
            .filter(d -> d instanceof JCTree.JCMethodDecl mt &&
                mt.name.equals(defaultConstructorMethodName))
            .map(d -> (JCTree.JCMethodDecl) d)
            .findFirst().orElseThrow(() ->
                new IllegalStateException("unreachable: cannot find synthetic constructor"));

        var superExprStmt = (JCTree.JCExpressionStatement) syntheticConstructor.body.stats.head;
        var superInvocation = ((JCTree.JCMethodInvocation) superExprStmt.expr);

        return (Symbol.MethodSymbol)
            ((JCTree.JCIdent) superInvocation.meth).sym;
    }

    com.sun.tools.javac.util.List<JCTree.JCStatement> consArgsToStatements(
        Symbol.MethodSymbol cons, Type.ClassType htmlElType,
        JCTree.JCNewClass newClass, Symbol.VarSymbol elVarSymbol,
        TreeTranslator me) {

        return IntStream.range(0, newClass.args.length())
            .mapToObj(i -> {
                var arg = me.translate(newClass.args.get(i));
                var memberName = cons.params.get(i)
                    .getAnnotation(HTMLNativeElements.DomProperty.class)
                    .value();

                var argSymbol = util.lookupMemberOpt((Symbol.ClassSymbol) htmlElType.tsym, memberName)
                    .orElseGet(() ->
                        // fix this strange code
                        util.lookupMemberOpt((Symbol.ClassSymbol) (((Symbol.ClassSymbol) htmlElType.tsym).getSuperclass()).tsym, memberName)
                            .orElseThrow(() ->
                                new IllegalStateException("cannot find member with name " + memberName)));

                return maker.Exec(
                    maker.Assign(
                        maker.Select(
                            maker.Ident(elVarSymbol),
                            argSymbol),
                        arg));
            }).reduce(
                com.sun.tools.javac.util.List.nil(),
                com.sun.tools.javac.util.List::append,
                com.sun.tools.javac.util.List::appendList);
    }

    int nextViewRenderId = 0;

    public void patch(JCTree.JCClassDecl classDecl, java.util.List<EffectCallFinder.Effect> effects) {
        var allEffectedVars = effects.stream()
            .flatMap(ef -> ef.effected().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        var allViewEntries = new ArrayList<JCTree.JCNewClass>();
        classDecl.accept(new TreeScanner() {
            @Override public void visitNewClass(JCTree.JCNewClass newClass) {
                if (!(util.classifyView(newClass.type) instanceof Util.IsNotComponent))
                    allViewEntries.add(newClass);
            }
        });

        var allViewsForUpdate = new ArrayList<JCTree.JCNewClass>();
        var allViewRenderSymbols = new ArrayList<Symbol.VarSymbol>();

        for (var viewEntry : allViewEntries) {
            var unconditionalTree = viewUpdateStrategy.buildTree(viewEntry, allEffectedVars,
                symbols.clSlot);
            var nodesForUpdate = effects.stream()
                .flatMap(ef -> viewUpdateStrategy.findNodesForUpdate(
                    unconditionalTree, ef.effected()).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream().toList();
            var viewsForUpdate = nodesForUpdate.stream().map(ViewUpdateStrategy.Node::self).toList();
            allViewsForUpdate.addAll(viewsForUpdate);

            var viewRenderSymbols = nodesForUpdate.stream()
                .map(node -> {
                    var viewRendererVar = new Symbol.VarSymbol(Flags.PRIVATE,
                        names.fromString("_render" + nextViewRenderId++),
                        symbols.clRunnable.type,
                        classDecl.sym);

                    classDecl.sym.members_field.enterIfAbsent(viewRendererVar);
                    classDecl.defs = classDecl.defs
                        .append(maker.VarDef(viewRendererVar, null));

                    return viewRendererVar;
                }).toList();
            allViewRenderSymbols.addAll(viewRenderSymbols);

            for (var i = 0; i < effects.size(); i++) {
                var effect = effects.get(i);
                var methodSym = new Symbol.MethodSymbol(
                    Flags.PRIVATE,
                    names.fromString("_effect" + i),
                    new Type.MethodType(
                        com.sun.tools.javac.util.List.of(symbols.clObject.type),
                        new Type.JCVoidType(), com.sun.tools.javac.util.List.nil(), classDecl.sym),
                    classDecl.sym);

                methodSym.params = com.sun.tools.javac.util.List.of(new Symbol.VarSymbol(
                    0, names.fromString("x0"), symbols.clObject.type, methodSym));
                classDecl.sym.members_field.enterIfAbsent(methodSym);
                effect.invocation().meth = maker.Ident(methodSym);

                var effectedNodes = viewUpdateStrategy.findNodesForUpdate
                    (unconditionalTree, effect.effected());
                com.sun.tools.javac.util.List<JCTree.JCStatement> renderCalls = effectedNodes.stream()
                    .map(node -> {
                        var forRender = viewRenderSymbols.get(nodesForUpdate.indexOf(node));
                        var checkNotNull = maker.Binary(JCTree.Tag.NE, maker.Ident(forRender), maker.Literal(TypeTag.BOT, null).setType(symtab.botType));
                        checkNotNull.operator = new Symbol.OperatorSymbol(
                            names.fromString("!="),
                            new Type.MethodType(
                                com.sun.tools.javac.util.List.of(
                                    symbols.clObject.type, symbols.clObject.type),
                                new Type.JCPrimitiveType(TypeTag.BOOLEAN, symtab.booleanType.tsym),
                                com.sun.tools.javac.util.List.nil(),
                                symtab.methodClass.type.tsym
                            ),
                            166 /* don't ask why */,
                            classDecl.sym);
                        checkNotNull.type = new Type.JCPrimitiveType(TypeTag.BOOLEAN, symtab.booleanType.tsym);

                        return maker.If(
                            maker.Parens(checkNotNull).setType(new Type.JCPrimitiveType(TypeTag.BOOLEAN, symtab.booleanType.tsym)),
                            maker.Exec(
                                maker.App(
                                    maker.Select(
                                        maker.Ident(forRender), symbols.mtRunnableRun))),
                            null);
                    }).reduce(
                        com.sun.tools.javac.util.List.nil(),
                        com.sun.tools.javac.util.List::append,
                        com.sun.tools.javac.util.List::appendList);

                var method = maker.MethodDef(methodSym,
                    maker.Block(Flags.BLOCK, renderCalls));

                method.mods = maker.Modifiers(Flags.PRIVATE);
                classDecl.defs = classDecl.defs.append(method);
            }
        }

        var lambdaToBlock = new HashSet<JCTree.JCLambda>();

        classDecl.accept(new TreeTranslator() {
            final Map<JCTree.JCNewClass, Symbol.VarSymbol> parentMap = new HashMap<>();
            final Set<Symbol.TypeSymbol> mappedNativeElementTypes = new HashSet<>();
            Symbol.VarSymbol current = null;
            Symbol.MethodSymbol currentMethod = null;
            int nextElNumber = 0;

            @Override public void visitMethodDef(JCTree.JCMethodDecl tree) {
                var old = currentMethod;
                currentMethod = tree.sym;
                super.visitMethodDef(tree);
                currentMethod = old;
            }

            @Override public void visitExec(JCTree.JCExpressionStatement tree) {
                if (tree.expr instanceof JCTree.JCNewClass nc)
                    if (current != null)
                        parentMap.put(nc, current);

                super.visitExec(tree);
            }

            void atThis(Symbol.VarSymbol newThis, Runnable block) {
                var prevThis = current;
                current = newThis;
                block.run();
                current = prevThis;
            }

            @Override public void visitIdent(JCTree.JCIdent id) {
                // FIXME: не будет работать для вложенных inner классов
                //        нужно держать Map<mapped new class, el>
                if (current != null)
                    if (id.sym.owner.type != null) { // foreach loop var
                        if (current.type.tsym == id.sym.owner.type.tsym ||
                            types.isSubtype(current.type, id.sym.owner.type)) {
                            this.result = maker.Select(maker.Ident(current), id.sym);
                            return;
                        }

                        if (id.name.equals(names.fromString("this")) &&
                            types.isSubtype(id.sym.owner.type, current.type)) {
                            this.result = maker.Ident(current);
                            return;
                        }
                    }

                super.visitIdent(id);
            }

            JCTree.JCBlock mapNewClassBody(Symbol.VarSymbol thisEl, Type.ClassType htmlElementType,
                                           JCTree.JCNewClass newClass, TreeTranslator me) {
                var cons = extractActualConstructor(newClass);
                var mappedArgs = consArgsToStatements(cons, htmlElementType, newClass, thisEl, me);
                var block = maker.Block(Flags.BLOCK, mappedArgs);

                if (newClass.def != null) {
                    var initBlock = newClass.def.defs.stream()
                        .filter(d -> d instanceof JCTree.JCBlock)
                        .map(d -> (JCTree.JCBlock) d)
                        .findFirst();

                    initBlock.ifPresent(init -> {
                        atThis(thisEl, () -> init.accept(this));
                        block.stats = block.stats.appendList(init.stats);
                    });
                }

                return block;
            }

            @Override public void visitNewClass(JCTree.JCNewClass newClass) {
                var classified = util.classifyView(newClass.type);
                var parent = parentMap.get(newClass);

                if (classified instanceof Util.IsNativeComponent nativeComp) {
                    mappedNativeElementTypes.add(newClass.type.tsym);

                    var htmlElementType = nativeComp.htmlElementType();
                    var nativeComponentName = htmlElementType.tsym.getSimpleName().toString();

                    var defaultConstructor = classDecl.sym.getEnclosedElements().stream()
                        .filter(Symbol::isConstructor).findAny().get();
                    var nextElOwner = currentMethod != null ? currentMethod : defaultConstructor;

                    var nextNum = nextElNumber++;
                    var nextElSymbol = new Symbol.VarSymbol(Flags.FINAL,
                        names.fromString("_el" + nextNum),
                        htmlElementType,
                        nextElOwner);

                    var nextEl = maker.VarDef(nextElSymbol,
                        makeCall(symbols.clJSExpression, symbols.mtJSExpressionOf, List.of(
                            maker.Literal("document.createElement('" + nativeComponentName + "')").setType(symbols.clString.type))));

                    var lmbType = parent == null ? symbols.clAsyncCallable.type : symbols.clAsyncRunnable.type;
                    var lmbApplyMethod = parent == null ? symbols.mtAsyncCallableCall : symbols.mtAsyncRunnableRun;
                    var lmb = maker.Lambda(List.nil(), maker.Block(Flags.BLOCK, com.sun.tools.javac.util.List.nil())).setType(lmbType);

                    lmb.target = lmbType;
                    lmb.polyKind = JCTree.JCPolyExpression.PolyKind.POLY;
                    lmb.paramKind = JCTree.JCLambda.ParameterKind.EXPLICIT;

                    var lmbBody = maker.Block(Flags.BLOCK, List.nil());
                    lmb.body = lmbBody;

                    var classBody = mapNewClassBody(nextElSymbol, htmlElementType, newClass, this);

                    final Symbol.VarSymbol resultElSymbol;
                    var updateIndex = allViewsForUpdate.indexOf(newClass);

                    if (updateIndex != -1) { // rerenderable
                        var elHolderSymbol = new Symbol.VarSymbol(Flags.HASINIT,
                            names.fromString("_holder" + nextNum),
                            htmlElementType,
                            nextElOwner);

                        var elHolder = maker.VarDef(elHolderSymbol, maker.Literal(TypeTag.BOT, null).setType(symtab.botType));
                        lmbBody.stats = lmbBody.stats.append(elHolder);

                        var viewRenderSymbol = allViewRenderSymbols.get(updateIndex);
                        var renderBody = maker.Block(Flags.BLOCK, com.sun.tools.javac.util.List.nil());
                        var renderLambda = maker.Lambda(com.sun.tools.javac.util.List.nil(), renderBody)
                            .setType(symbols.clAsyncRunnable.type);

                        renderLambda.target = symbols.clAsyncRunnable.type;
                        renderLambda.polyKind = JCTree.JCPolyExpression.PolyKind.POLY;
                        renderLambda.paramKind = JCTree.JCLambda.ParameterKind.EXPLICIT;

                        lmbBody.stats = lmbBody.stats.append(
                            maker.Block(Flags.BLOCK, com.sun.tools.javac.util.List.of(
                                maker.Exec(
                                    maker.App(
                                        maker.Select(
                                            maker.Parens(
                                                maker.Assign(
                                                    maker.Ident(viewRenderSymbol),
                                                    renderLambda
                                                )).setType(symbols.clAsyncRunnable.type),
                                            symbols.mtAsyncRunnableRun))))));

                        renderLambda.body = maker.Block(Flags.BLOCK, classBody.stats
                            .prepend(nextEl)
                            .append(
                                maker.Exec(maker.Assign(
                                    maker.Ident(elHolderSymbol),
                                    makeCall(symbols.clGReact, symbols.mtGReactReplace, List.of(
                                        maker.Ident(nextElSymbol), maker.Ident(elHolderSymbol)))))));

                        resultElSymbol = elHolderSymbol;
                    } else {
                        lmbBody.stats = lmbBody.stats.append(nextEl).appendList(classBody.stats);
                        resultElSymbol = nextElSymbol;
                    }


                    lmbBody.stats = lmbBody.stats.append(
                        parent != null ?
                            maker.Exec(
                                makeCall(parent, symbols.mtNodeAppendChild, List.of(
                                    maker.Ident(resultElSymbol)))) :
                            maker.Return(maker.Ident(resultElSymbol)));

                    if (parent != null) lambdaToBlock.add(lmb);

                    result = maker.App(
                        maker.Select(maker.Parens(lmb).setType(lmb.type), lmbApplyMethod), List.nil());
                } else if (classified instanceof Util.IsCustomComponent custom) {
                    var ct = (Type.ClassType) newClass.type;
                    while (mappedNativeElementTypes.contains(ct.getEnclosingType().tsym))
                        ct.setEnclosingType(ct.getEnclosingType().getEnclosingType());

                    newClass.args = newClass.args.map(this::translate);
                    if (newClass.def != null) newClass.def = this.translate(newClass.def);

                    if (parent == null) {
                        if (custom instanceof Util.IsSlot)
                            throw new RuntimeException("not implemented");
                        this.result = newClass;
                    } else {
                        var forMount = custom instanceof Util.IsSlot ? newClass.args.head : newClass;
                        var mountArgs = custom instanceof Util.IsSlot ? newClass.args.tail
                            : com.sun.tools.javac.util.List.<JCTree.JCExpression>nil();

                        List<JCTree.JCExpression> args = com.sun.tools.javac.util.List.of(
                            maker.Ident(current), forMount);
                        args = args.appendList(mountArgs);
                        var invoke = makeCall(symbols.clGReact, symbols.mtGReactMMount, args);
                        invoke.varargsElement = symbols.clObject.type;
                        this.result = invoke;
                    }
                } else
                    super.visitNewClass(newClass);
            }
        });

        classDecl.accept(new TreeTranslator() {
            @Override public void visitExec(JCTree.JCExpressionStatement estmt) {
                if (estmt.expr instanceof JCTree.JCMethodInvocation invoke &&
                    invoke.meth instanceof JCTree.JCFieldAccess fa &&
                    fa.selected instanceof JCTree.JCParens parens &&
                    parens.expr instanceof JCTree.JCLambda lmb &&
                    lambdaToBlock.contains(lmb)) {

                    lmb.body.accept(this);
                    result = lmb.body;
                } else {
                    estmt.expr.accept(this);
                    result = estmt;
                }
            }
        });
    }
}
