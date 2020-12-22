package com.greact.generate;

import com.greact.generate.MethodGen.MContext;
import com.greact.generate.TypeGen.TContext;
import com.greact.generate.util.JSOut;
import com.sun.source.tree.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCTypeUnion;

import java.util.List;

public class StatementGen {
    final JSOut out;
    final MContext mctx;
    final ExpressionGen exprGen;

    public StatementGen(JSOut out, MContext mctx) {
        this.out = out;
        this.mctx = mctx;
        this.exprGen = new ExpressionGen(out, mctx, this);
    }

    void block(int deep, Tree tree) {
        if (tree instanceof BlockTree block) {
            out.write(0, " {\n");
            block.getStatements().forEach((bStmt) -> {
                stmt(deep + 2, bStmt);
                out.write(0, "\n");
            });
            out.write(deep, "}");
        } else if (tree instanceof StatementTree stmt) {
            out.write(0, "\n");
            stmt(deep + 2, stmt);
        } else if (tree instanceof ExpressionTree expr) {
            //out.write(deep + 2, "\n");
            exprGen.expr(deep, expr);
        } else
            throw new RuntimeException("unknown kind: " + tree.getKind());
    }

    void stmt(int deep, StatementTree stmt) {
        if (stmt instanceof ReturnTree ret) {
            out.write(deep, "return ");
            exprGen.expr(deep, ret.getExpression());
        } else if (stmt instanceof BreakTree brk) {
            out.write(deep, "break");
            var label = brk.getLabel();
            if (label != null) out.write(1, label.toString());
        } else if (stmt instanceof ContinueTree cont) {
            out.write(deep, "continue");
            var label = cont.getLabel();
            if (label != null) out.write(1, label.toString());
        } else if (stmt instanceof VariableTree varDecl) {
            out.write(deep, "let ");
            out.write(0, varDecl.getName().toString());
            out.write(0, " = ");

            var initializer = varDecl.getInitializer();
            if (initializer != null)
                exprGen.expr(deep, varDecl.getInitializer());
            else
                out.write(0, "null");
            out.write(0, ";");
        } else if (stmt instanceof ExpressionStatementTree exprStmt) {
            out.write(deep, "");
            exprGen.expr(deep, exprStmt.getExpression());
            out.write(0, ";");
        } else if (stmt instanceof IfTree ifStmt) {
            out.write(deep, "if");
            exprGen.expr(deep, ifStmt.getCondition());
            block(deep, ifStmt.getThenStatement());

            var elseStmt = ifStmt.getElseStatement();
            if (elseStmt != null) {
                out.write(0, "\n");
                out.write(deep, "else");
                block(deep, elseStmt);
            }
        } else if (stmt instanceof WhileLoopTree whileStmt) {
            out.write(deep, "while");
            exprGen.expr(deep, whileStmt.getCondition());
            block(deep, whileStmt.getStatement());
        } else if (stmt instanceof DoWhileLoopTree doWhile) {
            out.write(deep, "do");
            block(deep, doWhile.getStatement());
            out.write(0, " while");
            exprGen.expr(deep, doWhile.getCondition());
        } else if (stmt instanceof ForLoopTree forStmt) {
            out.write(deep, "for");

            if (forStmt.getInitializer().isEmpty())
                out.write(0, "(;");
            else
                out.mkString(forStmt.getInitializer(), init -> {
                    var varDecl = (VariableTree) init;
                    out.write(0, varDecl.getName().toString());
                    out.write(0, " = ");
                    exprGen.expr(deep, varDecl.getInitializer());
                }, "(let ", ", ", "; ");


            exprGen.expr(deep, forStmt.getCondition());
            if (forStmt.getInitializer().isEmpty())
                out.write(0, ";)");
            else
                out.mkString(forStmt.getUpdate(), init ->
                    exprGen.expr(deep, init.getExpression()), "; ", ", ", ")");

            block(deep, forStmt.getStatement());
        } else if (stmt instanceof EnhancedForLoopTree forEach) {
            var varDecl = forEach.getVariable();
            out.write(deep, "for(let ");
            out.write(0, varDecl.getName().toString());
            out.write(0, " of ");
            exprGen.expr(deep, forEach.getExpression());
            out.write(0, ")");
            block(deep, forEach.getStatement());
        } else if (stmt instanceof LabeledStatementTree label) {
            out.write(deep, label.getLabel().toString());
            out.write(0, ":\n");
            stmt(deep, label.getStatement());
        } else if (stmt instanceof SwitchTree switchStmt) {
            out.write(deep, "switch");
            exprGen.expr(deep, switchStmt.getExpression());
            out.write(0, " {\n");
            var cases = switchStmt.getCases();
            cases.forEach((caseStmt) -> {
                if (caseStmt.getExpressions().isEmpty())
                    out.write(deep + 2, "default:");
                else {
                    // FIXME: bug
                    out.write(deep + 2, "case");
                    out.mkString(caseStmt.getExpressions(), caseExpr ->
                        exprGen.expr(deep + 2, caseExpr), " ", ", ", ":");
                }

                out.write(0, "\n");
                caseStmt.getStatements().forEach((blockStmt) -> {
                    stmt(deep + 4, blockStmt);
                    out.write(0, "\n");
                });

            });
            out.write(deep, "}");
        } else if (stmt instanceof YieldTree yieldExpr) {
            out.write(deep, "return ");
            exprGen.expr(deep, yieldExpr.getValue());
        } else if (stmt instanceof ThrowTree throwStmt) {
            out.write(deep, "throw ");
            exprGen.expr(deep, throwStmt.getExpression());
        } else if (stmt instanceof TryTree tryStmt) {
            out.write(deep, "try");
            block(deep, tryStmt.getBlock());

            var catchList = tryStmt.getCatches();
            if (!catchList.isEmpty()) {
                out.write(0, " catch(");
                var firstCatchVar = tryStmt.getCatches().get(0)
                    .getParameter().getName().toString();
                out.write(0, firstCatchVar);
                out.write(0, ") {\n");

                for (var i = 0; i < catchList.size(); i++) {
                    var ct = catchList.get(i);
                    var catchVar = ct.getParameter().getName().toString();
                    var catchType = ct.getParameter().getType();
                    var alternatives = catchType instanceof JCTypeUnion
                        ? ((JCTypeUnion) catchType).alternatives
                        : List.of((JCTree) catchType);

                    if (i == 0) out.write(deep + 2, "if(");
                    else out.write(0, " else if(");

                    out.mkString(alternatives, alt -> {
                        out.write(0, firstCatchVar);
                        out.write(0, " instanceof ");
                        out.write(0, alt.type.tsym.getQualifiedName().toString()
                            .replace(".", "$"));
                    }, "", " || ", "");

                    out.write(0, ") {\n");
                    for (var blockStm : ct.getBlock().getStatements()) {
                        if (i != 0) {
                            out.write(deep + 4, "let ");
                            out.write(0, catchVar);
                            out.write(0, " = ");
                            out.write(0, firstCatchVar);
                            out.write(0, "\n");
                        }
                        stmt(deep + 4, blockStm);
                        out.write(0, "\n");
                    }
                    out.write(deep + 2, "}");
                }

                out.write(0, " else {\n");
                out.write(deep + 4, "throw ");
                out.write(0, firstCatchVar);
                out.write(0, "\n");
                out.write(deep + 2, "}\n");
                out.write(deep, "}");
            }

            var finallyBlock = tryStmt.getFinallyBlock();
            if (finallyBlock != null) {
                out.write(0, " finally");
                block(deep, finallyBlock);
            }
        } else if(stmt instanceof BlockTree blockTree) {
            out.write(deep - 1, "");
            block(deep, blockTree);
        } else
            throw new RuntimeException("unknown statement kind: " + stmt.getKind());
    }
    // ASSERT
}

