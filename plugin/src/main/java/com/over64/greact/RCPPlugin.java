package com.over64.greact;

import com.over64.greact.dom.*;
import com.over64.greact.rpc.RPC;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

public class RCPPlugin {

    final Context context;
    final JavacProcessingEnvironment env;
    final Symtab symtab;
    final Names names;
    final Types types;
    final TreeMaker maker;

    class Symbols {
        Symbol.ClassSymbol lookupClass(String name) {
            return symtab.enterClass(symtab.unnamedModule, names.fromString(name));
        }

        <T extends Symbol> T lookupMember(Symbol.ClassSymbol from, String name) {
            @SuppressWarnings("unchecked")
            var res = (T) from.getEnclosedElements().stream()
                .filter(el -> el.name.equals(names.fromString(name)))
                .findFirst().orElseThrow(() ->
                    new RuntimeException("oops"));
            return res;
        }

        Symbol.ClassSymbol clObject = symtab.enterClass(symtab.java_base, names.fromString("java.lang.Object"));
        Symbol.ClassSymbol clString = symtab.enterClass(symtab.java_base, names.fromString("java.lang.String"));
        Symbol.ClassSymbol clRPC = lookupClass(RPC.class.getName());


        Symbol.ClassSymbol clDocument = lookupClass(Document.class.getName());
        Symbol.ClassSymbol clNode = lookupClass(Node.class.getName());
        Symbol.ClassSymbol clHtmlElement = lookupClass(HtmlElement.class.getName());
        Symbol.ClassSymbol clViewFragment = lookupClass(Fragment.ViewFragment.class.getName());
        Symbol.ClassSymbol clSlot = lookupClass(HTMLNativeElements.slot.class.getName());

        Symbol.ClassSymbol clFragment = lookupClass(Fragment.class.getName());
        Symbol.ClassSymbol clRenderer = lookupClass(Fragment.Renderer.class.getName());
        Symbol.MethodSymbol mtFragmentOf = lookupMember(clFragment, "of");
        Symbol.MethodSymbol mtFragmentCleanup = lookupMember(clFragment, "cleanup");
        Symbol.MethodSymbol mtFragmentAppendChild = lookupMember(clFragment, "appendChild");
        Symbol.VarSymbol flFragmentRenderer = lookupMember(clFragment, "renderer");
        Symbol.MethodSymbol mtRendererRender = lookupMember(clRenderer, "render");


        Symbol.ClassSymbol clGlobals = lookupClass("com.over64.greact.dom.Globals");
        Symbol.MethodSymbol mtGReactMount = lookupMember(clGlobals, "gReactMount");
        Symbol.MethodSymbol mtGReactReturn = lookupMember(clGlobals, "gReactReturn");
        Symbol.VarSymbol documentField = lookupMember(clGlobals, "document");
        Symbol.VarSymbol flGlobalsGReactElement = lookupMember(clGlobals, "gReactElement");

        Symbol.MethodSymbol createElementMethod = lookupMember(clDocument, "createElement");
        Symbol.MethodSymbol appendChildMethod = lookupMember(clNode, "appendChild");
    }

    final Symbols symbols;

    public RCPPlugin(Context context) {
        this.context = context;
        this.env = JavacProcessingEnvironment.instance(context);
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.maker = TreeMaker.instance(context);
        this.symbols = new Symbols();
    }

    void apply(JCTree.JCCompilationUnit cu) {
        cu.accept(new TreeScanner() {
            @Override
            public void visitApply(JCTree.JCMethodInvocation invoke) {
                if (invoke.meth instanceof JCTree.JCIdent id) {
                    if (id.sym.getAnnotation(RPC.RPCEntryPoint.class) != null) {

                        var zz = 1;
                    }
                }

            }
        });

    }
}
