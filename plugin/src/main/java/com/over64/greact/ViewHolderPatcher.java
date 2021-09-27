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
        Symbol.ClassSymbol clGReact = util.lookupClass(GReact.class);
        Symbol.VarSymbol flGReactElement = util.lookupMember(clGReact, "element");
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
        if(util.classifyView(rootElementType) instanceof IsComponent comp)
            htmlElementType = comp.htmlElementType();
        else throw new IllegalStateException("unreachable");

        var rootVar = new Symbol.VarSymbol(
            Flags.FINAL | Flags.HASINIT,
            rootVarName, htmlElementType, owner);

        var rootVarDecl = maker.VarDef(rootVar,
            maker.TypeCast(htmlElementType,
                maker.Select(
                    buildStatic(symbols.clGReact),
                    symbols.flGReactElement)));

        body.stats = body.stats.prepend(rootVarDecl);

        return new ViewHolderWithRoot(holder, rootVar);
    }
}
