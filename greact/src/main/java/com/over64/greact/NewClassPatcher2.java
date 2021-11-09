package com.over64.greact;

import com.over64.greact.dom.Document;
import com.over64.greact.dom.GReact;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
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
        Symbol.MethodSymbol mtReplaceChildren = util.lookupMember(clNode, "replaceChildren");
        //Symbol.ClassSymbol clBoolean = symtab.enterClass(symtab.java_base, names.fromString("java.lang.Boolean"));
        Symbol.ClassSymbol clDocument = util.lookupClass(Document.class);
        Symbol.MethodSymbol mtDocumentCreateElement = util.lookupMember(clDocument, "createElement");
        Symbol.ClassSymbol clAsyncRunnable = util.lookupClass(GReact.AsyncRunnable.class);
        Symbol.MethodSymbol mtAsyncRunnableRun = util.lookupMember(clAsyncRunnable, "run");
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

    public void patch(JCTree classDecl) {
        classDecl.accept(new TreeTranslator() {
            final Map<JCTree.JCNewClass, JCTree.JCNewClass> parentMap = new HashMap<>();
            JCTree.JCNewClass current = null;
            int nextElNumber = 0;

            @Override public void visitExec(JCTree.JCExpressionStatement tree) {
                if (tree.expr instanceof JCTree.JCNewClass nc)
                    if (current != null)
                        parentMap.put(nc, current);

                super.visitExec(tree);
            }

            @Override public void visitNewClass(JCTree.JCNewClass nc) {
                var classified = util.classifyView(nc.type);
                if (classified instanceof Util.IsNativeComponent nativeComp) {
                    var parent = parentMap.get(nc);
                    System.out.println("<<NEWCLASS>> " + nc);
                    System.out.println("\t<<PARENT>> " + parent);

                    if (parent == null) {
                        var lmbType = symbols.clAsyncRunnable.type;
                        var lmb = maker.Lambda(List.nil(), maker.Block(Flags.BLOCK, com.sun.tools.javac.util.List.nil())).setType(lmbType);

                        lmb.target = lmbType;
                        lmb.polyKind = JCTree.JCPolyExpression.PolyKind.POLY;
                        lmb.paramKind = JCTree.JCLambda.ParameterKind.EXPLICIT;

                        // lmb.body = maker.Block(Flags.BLOCK, List.nil());

                        var htmlElementType = nativeComp.htmlElementType();
                        var nativeComponentName = htmlElementType.tsym.getSimpleName().toString();

                        var nextElSymbol = new Symbol.VarSymbol(Flags.FINAL,
                            names.fromString("_el" + nextElNumber++),
                            htmlElementType,
                            nc.type.tsym.owner);

                        var nextEl = maker.VarDef(nextElSymbol,
                            makeCall(symbols.clDocument, symbols.mtDocumentCreateElement, List.of(
                                maker.Literal(nativeComponentName).setType(symbols.clString.type)
                            )));

                        var ret = maker.Return(maker.Ident(nextEl.sym));

                        lmb.body = maker.Block(Flags.BLOCK, List.of(nextEl, ret));
                        result = lmb;

                    } else {
                        super.visitNewClass(nc);
                    }


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
