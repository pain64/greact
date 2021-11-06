package com.over64.greact;

import com.over64.greact.Util.IsComponent;
import com.over64.greact.ViewEntryFinder.LambdaViewHolder;
import com.over64.greact.ViewEntryFinder.MountMethodViewHolder;
import com.over64.greact.ViewEntryFinder.ViewHolder;
import com.over64.greact.dom.GReact;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class ViewHolderPatcher {
    final Symtab symtab;
    final Names names;
    final TreeMaker maker;
    final Util util;
    final Name rootVarName;

    class Symbols {
        Symbol.ClassSymbol clString = util.lookupClass(String.class);
        Symbol.ClassSymbol clGReact = util.lookupClass(GReact.class);
        Symbol.MethodSymbol mtMountMe = util.lookupMember(clGReact, "mountMe");
    }

    final Symbols symbols;

    public ViewHolderPatcher(Context context) {
        this.maker = TreeMaker.instance(context);
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.util = new Util(context);
        this.symbols = new Symbols();
        rootVarName = names.fromString("_root");
    }

    public record ViewHolderWithRoot(ViewHolder holder, Symbol.VarSymbol root) {}

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

    // FIXME: remove ViewHolderWithRoot => just return root
    public ViewHolderWithRoot patch(ViewHolder holder) {
        final Type rootElementType;
        final JCTree.JCBlock body;
        final Symbol owner;

        if (holder instanceof MountMethodViewHolder mm) {
            rootElementType = mm.owner().type.getReturnType();
            body = mm.owner().body;
            owner = mm.owner().sym;
        } else if (holder instanceof LambdaViewHolder lvh) {
            var lmb = lvh.lmb();

            if (lmb.body instanceof JCTree.JCExpression expr)
                lmb.body = maker.Block(Flags.BLOCK, List.of(
                    maker.Return(expr)));

            body = (JCTree.JCBlock) lmb.body;
            rootElementType = lmb.type.allparams().get(0);
            owner = lvh.owner().sym;
        } else
            throw new IllegalStateException("unreachable");

        final Type.ClassType htmlElementType;
        final String nativeComponentName;
        if(util.classifyView(rootElementType) instanceof IsComponent comp) {
            htmlElementType = comp.htmlElementType();
            nativeComponentName = htmlElementType.tsym.getSimpleName().toString();
        } else {
//            System.out.println("##ROOT_ELEMENT_TYPE = " + rootElementType);
            throw new IllegalStateException("unreachable");
        }

        var rootVar = new Symbol.VarSymbol(
            Flags.FINAL | Flags.HASINIT,
            rootVarName, htmlElementType, owner);

        var rootVarDecl = maker.VarDef(rootVar, makeCall(symbols.clGReact, symbols.mtMountMe,
                List.of(maker.Literal(nativeComponentName).setType(symbols.clString.type))));

        body.stats = body.stats.prepend(rootVarDecl);

        return new ViewHolderWithRoot(holder, rootVar);
    }
}
