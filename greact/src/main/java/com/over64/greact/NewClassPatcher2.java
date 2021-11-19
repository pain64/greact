package com.over64.greact;

import com.greact.model.JSExpression;
import com.over64.greact.dom.Document;
import com.over64.greact.dom.GReact;
import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.Node;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class NewClassPatcher2 {

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
        Symbol.ClassSymbol clNode = util.lookupClass(com.over64.greact.dom.Node.class);
        Symbol.MethodSymbol mtNodeAppendChild = util.lookupMember(clNode, "appendChild");
        Symbol.MethodSymbol mtReplaceChildren = util.lookupMember(clNode, "replaceChildren");
        //Symbol.ClassSymbol clBoolean = symtab.enterClass(symtab.java_base, names.fromString("java.lang.Boolean"));
        Symbol.ClassSymbol clDocument = util.lookupClass(Document.class);
        Symbol.MethodSymbol mtDocumentCreateElement = util.lookupMember(clDocument, "createElement");
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

    public void patch(JCTree classDecl) {
        classDecl.accept(new TreeTranslator() {
            final Map<JCTree.JCNewClass, Symbol.VarSymbol> parentMap = new HashMap<>();
            Symbol.VarSymbol current = null;
            int nextElNumber = 0;

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

            @Override public void visitNewClass(JCTree.JCNewClass nc) {
                var classified = util.classifyView(nc.type);
                if (classified instanceof Util.IsNativeComponent nativeComp) {
                    var parent = parentMap.get(nc);
                    System.out.println("<<NEWCLASS>> " + nc);
                    System.out.println("\t<<PARENT>> " + parent);

                    var htmlElementType = nativeComp.htmlElementType();
                    var nativeComponentName = htmlElementType.tsym.getSimpleName().toString();

                    var nextElSymbol = new Symbol.VarSymbol(Flags.FINAL,
                        names.fromString("_el" + nextElNumber++),
                        htmlElementType,
                        nc.type.tsym.owner);

                    var nextEl = parent == null ?
                        maker.VarDef(nextElSymbol,
                            makeCall(symbols.clJSExpression, symbols.mtJSExpressionOf, List.of(
                                maker.Literal("document.createElement('" + nativeComponentName + "')").setType(symbols.clString.type)
                            ))) :
                        maker.VarDef(nextElSymbol,
                            makeCall(parent, symbols.mtNodeAppendChild, List.of(
                                makeCall(symbols.clJSExpression, symbols.mtJSExpressionOf, List.of(
                                    maker.Literal("document.createElement('" + nativeComponentName + "')").setType(symbols.clString.type)
                                )))));


                    var lmbType = parent == null ? symbols.clAsyncCallable.type : symbols.clAsyncRunnable.type;
                    var lmb = maker.Lambda(List.nil(), maker.Block(Flags.BLOCK, com.sun.tools.javac.util.List.nil())).setType(lmbType);

                    lmb.target = lmbType;
                    lmb.polyKind = JCTree.JCPolyExpression.PolyKind.POLY;
                    lmb.paramKind = JCTree.JCLambda.ParameterKind.EXPLICIT;

                    var block = mapNewClassBody(nextElSymbol, htmlElementType, nc);
                    block.stats = block.stats.prepend(nextEl);

                    if(parent == null) {
                        var ret = maker.Return(maker.Ident(nextEl.sym));
                        block.stats = block.stats.append(ret);
                    }

                    lmb.body = block;

                    result = maker.App(maker.Select(lmb,
                        parent == null ? symbols.mtAsyncCallableCall : symbols.mtAsyncRunnableRun),
                        List.nil());

//                    var old = current;
//                    current = nc;
//                    super.visitNewClass(nc);
//                    current = old;
                } else
                    super.visitNewClass(nc);
            }
        });
    }
}
