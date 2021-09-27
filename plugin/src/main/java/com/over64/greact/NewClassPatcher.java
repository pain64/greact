package com.over64.greact;

import com.over64.greact.ViewEntryFinder.ClassEntry;
import com.over64.greact.dom.GReact;
import com.over64.greact.dom.HTMLNativeElements;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.over64.greact.EffectCallFinder.Effect;
import static com.over64.greact.Util.*;
import static com.over64.greact.ViewUpdateStrategy.Node;

public class NewClassPatcher {
    final Symtab symtab;
    final Names names;
    final TreeMaker maker;
    final Types types;
    final Util util;
    final ViewHolderPatcher viewHolderPatcher;
    final ViewUpdateStrategy viewUpdateStrategy;
    final Name rootVarName;
    final Name defaultConstructorMethodName;

    class Symbols {
        Symbol.ClassSymbol clGReact = util.lookupClass(GReact.class);
        Symbol.MethodSymbol mtGReactEntry = util.lookupMember(clGReact, "entry");
        Symbol.MethodSymbol mtGReactMount = util.lookupMember(clGReact, "mount");
        Symbol.MethodSymbol mtGReactMake = util.lookupMember(clGReact, "make");
        Symbol.ClassSymbol clRunnable = util.lookupClass(Runnable.class);
        Symbol.MethodSymbol mtRunnableRun = util.lookupMember(clRunnable, "run");
        Symbol.ClassSymbol clConsumer = util.lookupClass(Consumer.class);
        Symbol.ClassSymbol clString = util.lookupClass(String.class);
        Symbol.ClassSymbol clObject = util.lookupClass(Object.class);
        //Symbol.ClassSymbol clBoolean = symtab.enterClass(symtab.java_base, names.fromString("java.lang.Boolean"));
    }

    final Symbols symbols;

    public NewClassPatcher(Context context) {
        this.maker = TreeMaker.instance(context);
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.util = new Util(context);
        this.viewHolderPatcher = new ViewHolderPatcher(context);
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
        JCTree.JCNewClass newClass, Symbol.VarSymbol elVarSymbol) {

        return IntStream.range(0, newClass.args.length())
            .mapToObj(i -> {
                var arg = newClass.args.get(i);
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

    public void patch(ClassEntry classEntry, List<Effect> effects) {

        var allEffectedVars = effects.stream()
            .flatMap(ef -> ef.effected().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        for (var vh : classEntry.viewHolders()) {
            var root = viewHolderPatcher.patch(vh).root();
            var unconditionalTree = viewUpdateStrategy.buildTree(vh.view(), allEffectedVars);
            var nodesForUpdate = effects.stream()
                .flatMap(ef -> viewUpdateStrategy.findNodesForUpdate(
                    unconditionalTree, ef.effected()).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream().toList();
            var viewsForUpdate = nodesForUpdate.stream().map(Node::self).toList();

            var classDecl = classEntry.classDecl();
            var viewRenderSymbols = IntStream.range(0, nodesForUpdate.size())
                .mapToObj(i -> {
                    var viewRendererVar = new Symbol.VarSymbol(Flags.PRIVATE,
                        names.fromString("_render" + i),
                        symbols.clRunnable.type,
                        classDecl.sym);

                    classDecl.sym.members_field.enterIfAbsent(viewRendererVar);
                    classDecl.defs = classDecl.defs
                        .append(maker.VarDef(viewRendererVar, null));

                    return viewRendererVar;
                }).toList();

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

            vh.target().accept(new TreeTranslator() {
                Symbol.VarSymbol currentThis;
                int nextElNumber = 0;

                void atThis(Symbol.VarSymbol newThis, Runnable block) {
                    var prevThis = currentThis;
                    currentThis = newThis;
                    block.run();
                    currentThis = prevThis;
                }

                JCTree.JCBlock mapNewClassBody(Symbol.VarSymbol thisEl, Type.ClassType htmlElementType, JCTree.JCNewClass newClass) {
                    var cons = extractActualConstructor(newClass);
                    var mappedArgs = consArgsToStatements(cons, htmlElementType, newClass, thisEl);
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
                    if (classified instanceof IsNotComponent) {
                        this.result = newClass;
                        return;
                    }

                    var isViewEntry = vh.view() == newClass;
                    var component = (IsComponent) classified;
                    var htmlElementType = component.htmlElementType();
                    var nativeComponentName = htmlElementType.tsym.getSimpleName().toString();

                    final com.sun.tools.javac.util.List<JCTree.JCVariableDecl> lambdaArgs;
                    final Symbol.VarSymbol newClassEl;

                    if (isViewEntry) {
                        lambdaArgs = com.sun.tools.javac.util.List.nil();
                        newClassEl = root;
                    } else {
                        var nextEl = maker.Param(names.fromString("_el" + nextElNumber++), htmlElementType, vh.owner().sym);
                        lambdaArgs = com.sun.tools.javac.util.List.of(nextEl);
                        newClassEl = nextEl.sym;
                    }

                    var lmbType = isViewEntry ? symbols.clRunnable.type : symbols.clConsumer.type;
                    var lmb = maker.Lambda(lambdaArgs, maker.Block(Flags.BLOCK, com.sun.tools.javac.util.List.nil())).setType(lmbType);

                    lmb.target = lmbType;
                    lmb.polyKind = JCTree.JCPolyExpression.PolyKind.POLY;
                    lmb.paramKind = JCTree.JCLambda.ParameterKind.EXPLICIT;

                    var updateIndex = viewsForUpdate.indexOf(newClass);
                    final JCTree.JCLambda destLambda;
                    if (updateIndex != -1) {
                        var viewRenderSymbol = viewRenderSymbols.get(updateIndex);

                        var renderBody = maker.Block(Flags.BLOCK, com.sun.tools.javac.util.List.nil());
                        var renderLambda = maker.Lambda(com.sun.tools.javac.util.List.nil(), renderBody)
                            .setType(symbols.clRunnable.type);

                        renderLambda.target = symbols.clRunnable.type;
                        renderLambda.polyKind = JCTree.JCPolyExpression.PolyKind.POLY;
                        renderLambda.paramKind = JCTree.JCLambda.ParameterKind.EXPLICIT;

                        lmb.body = maker.Block(Flags.BLOCK, com.sun.tools.javac.util.List.of(
                            maker.Exec(maker.App(
                                maker.Select(
                                    maker.Parens(
                                        maker.Assign(
                                            maker.Ident(viewRenderSymbol),
                                            renderLambda
                                        )).setType(symbols.clRunnable.type),
                                    symbols.mtRunnableRun)
                            ))));
                        destLambda = renderLambda;
                    } else
                        destLambda = lmb;

                    if (component instanceof IsCustomComponent custom) {
                        var forMount = custom instanceof IsSlot ? newClass.args.head : newClass;
                        var mountArgs = custom instanceof IsSlot ? newClass.args.tail
                            : com.sun.tools.javac.util.List.<JCTree.JCExpression>nil();

                        destLambda.body = maker.Block(Flags.BLOCK, com.sun.tools.javac.util.List.of(
                            maker.Exec(makeCall(symbols.clGReact, symbols.mtGReactMount,
                                com.sun.tools.javac.util.List.of(
                                    maker.Ident(newClassEl),
                                    forMount,
                                    maker.NewArray(maker.Ident(symbols.clObject), com.sun.tools.javac.util.List.nil(),
                                            mountArgs)
                                        .setType(types.makeArrayType(symbols.clObject.type))))
                            )));
                    } else  // native
                        destLambda.body = mapNewClassBody(newClassEl, htmlElementType, newClass);

                    this.result = isViewEntry ?
                        makeCall(symbols.clGReact, symbols.mtGReactEntry,
                            com.sun.tools.javac.util.List.of(lmb)) :
                        makeCall(symbols.clGReact, symbols.mtGReactMake,
                            com.sun.tools.javac.util.List.of(
                                maker.Ident(currentThis),
                                maker.Literal(nativeComponentName).setType(symbols.clString.type),
                                lmb));
                }

                @Override public void visitIdent(JCTree.JCIdent id) {
                    if (currentThis != null)
                        if (id.sym.owner.type != null) { // foreach loop var
                            if (currentThis.type.tsym == id.sym.owner.type.tsym ||
                                types.isSubtype(currentThis.type, id.sym.owner.type)) {
                                this.result = maker.Select(maker.Ident(currentThis), id.sym);
                                return;
                            }

                            if (id.name.equals(names.fromString("this")) &&
                                types.isSubtype(id.sym.owner.type, currentThis.type)) {
                                this.result = maker.Ident(currentThis);
                                return;
                            }
                        }

                    super.visitIdent(id);
                }
            });
        }
    }
}
