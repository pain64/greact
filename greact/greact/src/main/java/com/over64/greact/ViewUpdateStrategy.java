package com.over64.greact;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

import java.util.*;

public class ViewUpdateStrategy {
    public record Node(
        JCTree.JCNewClass self,
        Set<Symbol.VarSymbol> dependsSelf,
        Set<Symbol.VarSymbol> dependsConditional,
        List<Node> children) {

        public static Node make(JCTree.JCNewClass self) {
            return new Node(self, new HashSet<>(), new HashSet<>(), new ArrayList<>());
        }
    }

    static class UnconditionalNodeVisitor extends TreeScanner {
        final Set<Symbol.VarSymbol> effectedVars;
        final Node root;
        final Symbol.ClassSymbol clSlot;

        public UnconditionalNodeVisitor(Set<Symbol.VarSymbol> effectedVars, Node root,
                                        Symbol.ClassSymbol clSlot) {
            this.effectedVars = effectedVars;
            this.root = root;
            this.clSlot = clSlot;
        }

        boolean hasCondition = false;

        void atConditional(Runnable block) {
            var hasConditionOld = hasCondition;
            hasCondition = true;
            block.run();
            hasCondition = hasConditionOld;
        }

        @Override public void visitIdent(JCTree.JCIdent id) {
            if (id.sym instanceof Symbol.VarSymbol sym)
                if (effectedVars.contains(sym))
                    if (hasCondition)
                        root.dependsConditional.add(sym);
                    else root.dependsSelf.add(sym);
        }

        @Override public void visitNewClass(JCTree.JCNewClass tree) {
            if (root.self == tree || hasCondition || tree.type.tsym == clSlot) {
                super.visitNewClass(tree);
            } else { // new unconditional child
                var nextRoot = Node.make(tree);
                root.children.add(nextRoot);
                new UnconditionalNodeVisitor(effectedVars, nextRoot, clSlot).scan(tree);
            }
        }

        @Override public void visitIf(JCTree.JCIf tree) {
            atConditional(() -> super.visitIf(tree));
        }
        @Override public void visitForLoop(JCTree.JCForLoop tree) {
            atConditional(() -> super.visitForLoop(tree));
        }
        @Override public void visitForeachLoop(JCTree.JCEnhancedForLoop tree) {
            atConditional(() -> super.visitForeachLoop(tree));
        }
        @Override public void visitWhileLoop(JCTree.JCWhileLoop tree) {
            atConditional(() -> super.visitWhileLoop(tree));
        }
        @Override public void visitSwitch(JCTree.JCSwitch tree) {
            atConditional(() -> super.visitSwitch(tree));
        }
        @Override public void visitSwitchExpression(JCTree.JCSwitchExpression tree) {
            atConditional(() -> super.visitSwitchExpression(tree));
        }
        @Override public void visitTry(JCTree.JCTry tree) {
            atConditional(() -> super.visitTry(tree));
        }
        @Override public void visitLambda(JCTree.JCLambda tree) { }
    }

    public Node buildTree(JCTree.JCNewClass view,
                          Set<Symbol.VarSymbol> effectedVars,
                          Symbol.ClassSymbol clSlot) {
        var root = Node.make(view);
        new UnconditionalNodeVisitor(effectedVars, root, clSlot).scan(view);
        return root;
    }

    <T> boolean containsAny(Set<T> at, Set<T> anyOf) {
        for (var el : anyOf)
            if (at.contains(el)) return true;

        return false;
    }

    private void pushNodesForUpdate(List<Node> dest, Node root, Set<Symbol.VarSymbol> changed) {
        if (containsAny(root.dependsSelf, changed) ||
            containsAny(root.dependsConditional, changed))
            dest.add(root);
        else
            for (var child : root.children)
                pushNodesForUpdate(dest, child, changed);
    }

    public List<Node> findNodesForUpdate(Node root, Set<Symbol.VarSymbol> changed) {
        var dest = new ArrayList<Node>();
        pushNodesForUpdate(dest, root, changed);
        return dest;
    }
}
