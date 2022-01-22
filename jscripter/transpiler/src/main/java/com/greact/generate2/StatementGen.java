package com.greact.generate2;

import com.sun.tools.javac.tree.JCTree;

import java.util.List;

abstract class StatementGen extends ExpressionGen {
    @Override public void visitBlock(JCTree.JCBlock block) {
        out.writeCBOpen();
        block.stats.forEach(stmt -> stmt.accept(this));
        out.writeCBEnd();
    }

    @Override public void visitReturn(JCTree.JCReturn ret) {
        out.write("return ");
        ret.expr.accept(this);
    }

    @Override public void visitBreak(JCTree.JCBreak brk) {
        out.write("break");
        if (brk.label != null) {
            out.write(" ");
            out.write(brk.label.toString());
        }
    }

    @Override public void visitContinue(JCTree.JCContinue cont) {
        out.write("continue");
        if (cont.label != null) {
            out.write(" ");
            out.write(cont.label.toString());
        }
    }

    @Override public void visitVarDef(JCTree.JCVariableDecl varDef) {
        out.write(varDef.sym.isFinal() ? "const " : "let ");
        out.write(varDef.getName().toString());
        out.write(" = ");

        if (varDef.init != null) varDef.init.accept(this);
        else out.write("null");

        out.writeLn(";");
    }

    @Override public void visitExec(JCTree.JCExpressionStatement eStmt) {
        eStmt.expr.accept(this);
        out.writeLn(";");
    }

    @Override public void visitIf(JCTree.JCIf ifStmt) {
        out.write("if");
        ifStmt.cond.accept(this);
        ifStmt.thenpart.accept(this);
        var elseStmt = ifStmt.elsepart;
        if (elseStmt != null) {
            out.write("else");
            elseStmt.accept(this);
        }
    }

    @Override public void visitWhileLoop(JCTree.JCWhileLoop whileStmt) {
        out.write("while");
        whileStmt.cond.accept(this);
        whileStmt.body.accept(this);
    }

    @Override public void visitDoLoop(JCTree.JCDoWhileLoop doWhile) {
        out.write("do");
        doWhile.body.accept(this);
        out.write(" while");
        doWhile.cond.accept(this);
    }

    @Override public void visitForLoop(JCTree.JCForLoop forStmt) {
        out.write("for");

        if (forStmt.init.isEmpty())
            out.write("(;");
        else
            out.mkString(forStmt.init, init -> {
                var varDef = (JCTree.JCVariableDecl) init;
                out.write(varDef.name.toString());
                out.write(" = ");
                varDef.init.accept(this);
            }, "(let ", ", ", "; ");

        forStmt.cond.accept(this);

        if (forStmt.init.isEmpty())
            out.write(";)");
        else
            out.mkString(forStmt.step, init ->
                init.expr.accept(this), "; ", ", ", ")");

        forStmt.body.accept(this);
    }

    @Override public void visitForeachLoop(JCTree.JCEnhancedForLoop forEach) {
        var varDef = forEach.getVariable();
        out.write("for(let ");
        out.write(varDef.getName().toString());
        out.write(" of ");
        forEach.expr.accept(this);
        out.write(")");
        forEach.body.accept(this);
    }

    @Override public void visitLabelled(JCTree.JCLabeledStatement lStmt) {
        out.write(lStmt.label.toString());
        out.writeLn(":");
        lStmt.body.accept(this);
    }

    @Override public void visitSwitch(JCTree.JCSwitch swc) {
        out.write("switch");
        swc.selector.accept(this);
        out.writeCBOpen();
        swc.cases.forEach(caseStmt -> {
            if (caseStmt.getExpressions().isEmpty())
                out.write("default:");
            else {
                // FIXME: bug
                out.write("case");
                out.mkString(caseStmt.getExpressions(), caseExpr ->
                    caseExpr.accept(this), " ", ", ", ":");
            }

            out.writeLn("");
            // FIXME: accept block directly???
            caseStmt.getStatements().forEach(blockStmt -> {
                blockStmt.accept(this);
                out.writeLn("");
            });

        });

        out.writeCBEnd();
    }

    @Override public void visitYield(JCTree.JCYield yStmt) {
        out.write("return ");
        yStmt.value.accept(this);
    }

    @Override public void visitThrow(JCTree.JCThrow tStmt) {
        out.write("throw ");
        tStmt.expr.accept(this);
    }

    @Override public void visitTry(JCTree.JCTry tryStmt) {
        out.write("try ");
        tryStmt.body.accept(this);

        var catchList = tryStmt.catchers;
        if (!catchList.isEmpty()) {
            out.write(" catch(");
            var firstCatchVar = tryStmt.getCatches().get(0)
                .getParameter().getName().toString();
            out.write(firstCatchVar);
            out.writeLn(") {");

            for (var i = 0; i < catchList.size(); i++) {
                var ct = catchList.get(i);
                var catchVar = ct.getParameter().getName().toString();
                var catchType = ct.getParameter().getType();
                var alternatives = catchType instanceof JCTree.JCTypeUnion
                    ? ((JCTree.JCTypeUnion) catchType).alternatives
                    : List.of(catchType);

                if (i == 0) out.write("if(");
                else out.write(" else if(");

                out.mkString(alternatives, alt -> {
                    out.write(firstCatchVar);
                    out.write(" instanceof ");
                    out.write(alt.type.tsym.getQualifiedName().toString()
                        .replace(".", "$"));
                }, "", " || ", "");

                out.writeLn(") {");
                for (var blockStm : ct.getBlock().getStatements()) {
                    if (i != 0) {
                        out.write("let ");
                        out.write(catchVar);
                        out.write(" = ");
                        out.write(firstCatchVar);
                        out.writeLn("");
                    }
                    blockStm.accept(this);
                    out.writeLn("");
                }
                out.writeCBEnd();
            }

            out.writeLn(" else {");
            out.write("throw ");
            out.write(firstCatchVar);
            out.writeLn("");
            out.writeLn("}");
            out.write("}");
        }

        var finallyBlock = tryStmt.getFinallyBlock();
        if (finallyBlock != null) {
            out.write(" finally");
            finallyBlock.accept(this);
        }
    }
}