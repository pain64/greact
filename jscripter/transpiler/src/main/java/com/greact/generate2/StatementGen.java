package com.greact.generate2;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;

import java.util.List;

abstract class StatementGen extends ExpressionGen {
    @Override public void visitBlock(JCTree.JCBlock block) {
        out.writeCBOpen(false);
        block.stats.forEach(stmt -> stmt.accept(this));
        out.writeCBEnd(false);
    }

    @Override public void visitReturn(JCTree.JCReturn ret) {
        out.write("return");
        if (ret.expr != null) {
            out.write(" ");
            ret.expr.accept(this);
        }
        out.writeLn(";");
    }

    @Override public void visitBreak(JCTree.JCBreak brk) {
        out.write("break");
        if (brk.label != null) {
            out.write(" ");
            out.write(brk.label);
        }
        out.writeLn(";");
    }

    @Override public void visitContinue(JCTree.JCContinue cont) {
        out.write("continue");
        if (cont.label != null) {
            out.write(" ");
            out.write(cont.label);
        }
        out.writeLn(";");
    }

    @Override public void visitVarDef(JCTree.JCVariableDecl varDef) {
        var isConst = varDef.sym.isFinal() ||
            (varDef.sym.flags_field & Flags.EFFECTIVELY_FINAL) != 0;
        out.write(isConst ? "const " : "let ");
        out.write(varDef.getName());
        out.write(" = ");

        if (varDef.init != null) varDef.init.accept(this);
        else out.write("null");
        out.writeLn(";");
    }

    @Override public void visitExec(JCTree.JCExpressionStatement eStmt) {
        if (eStmt.expr instanceof JCTree.JCMethodInvocation call &&
            call.meth instanceof JCTree.JCIdent id &&
            id.name.equals(names.fromString("super")) &&
            (id.sym.owner.flatName().equals(names.fromString("java.lang.Object")) ||
                id.sym.owner.flatName().equals(names.fromString("java.lang.Record"))))
            return;

        eStmt.expr.accept(this);
        out.writeLn(";");
    }

    void writeStmtBody(JCTree.JCStatement body, boolean blockNewLine) {
        if (body instanceof JCTree.JCBlock) {
            out.write(" ");
            body.accept(this);
            if (blockNewLine) out.writeNL();
        } else {
            out.writeNL();
            out.deepIn();
            body.accept(this);
            out.deepOut();
        }
    }

    @Override public void visitIf(JCTree.JCIf ifStmt) {
        out.write("if");
        ifStmt.cond.accept(this);
        writeStmtBody(ifStmt.thenpart, false);

        var elseStmt = ifStmt.elsepart;
        if (elseStmt != null) {
            out.write("else");
            writeStmtBody(elseStmt, true);
        }
    }

    @Override public void visitWhileLoop(JCTree.JCWhileLoop whileStmt) {
        out.write("while");
        whileStmt.cond.accept(this);
        writeStmtBody(whileStmt.body, true);
    }

    @Override public void visitDoLoop(JCTree.JCDoWhileLoop doWhile) {
        out.write("do");
        writeStmtBody(doWhile.body, false);
        out.write(" while");
        doWhile.cond.accept(this);
        out.writeLn(";");
    }

    @Override public void visitForLoop(JCTree.JCForLoop forStmt) {
        out.write("for");

        if (forStmt.init.isEmpty())
            out.write("(;");
        else
            out.mkString(forStmt.init, init -> {
                var varDef = (JCTree.JCVariableDecl) init;
                out.write(varDef.name);
                out.write(" = ");
                varDef.init.accept(this);
            }, "(let ", ", ", "; ");

        if (forStmt.cond != null) forStmt.cond.accept(this);

        if (forStmt.init.isEmpty())
            out.write(";)");
        else
            out.mkString(forStmt.step, init ->
                init.expr.accept(this), "; ", ", ", ")");

        writeStmtBody(forStmt.body, true);
    }

    @Override public void visitForeachLoop(JCTree.JCEnhancedForLoop forEach) {
        var varDef = forEach.getVariable();
        out.write("for(let ");
        out.write(varDef.getName());
        out.write(" of ");
        forEach.expr.accept(this);
        out.write(")");
        writeStmtBody(forEach.body, true);
    }

    @Override public void visitLabelled(JCTree.JCLabeledStatement lStmt) {
        out.write(lStmt.label);
        out.writeLn(":");
        lStmt.body.accept(this);
    }

    @Override public void visitCase(JCTree.JCCase caseStmt) {
        for (var lbl : caseStmt.labels) {
            if (lbl instanceof JCTree.JCDefaultCaseLabel) out.write("default");
            else {
                out.write("case ");
                lbl.accept(this);
            }
            out.writeLn(":");
        }

        out.deepIn();
        for (var stat : caseStmt.stats) {
            stat.accept(this);
            if (stat instanceof JCTree.JCBlock) out.writeNL();
        }
        out.deepOut();
    }
    @Override public void visitSwitch(JCTree.JCSwitch swc) {
        out.write("switch");
        swc.selector.accept(this);
        out.writeCBOpen(true);
        swc.cases.forEach(caseStmt -> caseStmt.accept(this));
        out.writeCBEnd(true);
    }

    @Override public void visitYield(JCTree.JCYield yStmt) {
        out.write("return ");
        yStmt.value.accept(this);
        out.writeLn(";");
    }

    @Override public void visitThrow(JCTree.JCThrow tStmt) {
        out.write("throw ");
        tStmt.expr.accept(this);
        out.writeLn(";");
    }

    @Override public void visitTry(JCTree.JCTry tryStmt) {
        out.write("try ");
        tryStmt.body.accept(this);

        var catchList = tryStmt.catchers;
        if (!catchList.isEmpty()) {
            out.write(" catch(");
            var firstCatchVar = tryStmt.getCatches().get(0)
                .getParameter().getName();
            out.write(firstCatchVar);
            out.write(")");
            out.writeCBOpen(true);

            for (var i = 0; i < catchList.size(); i++) {
                var ct = catchList.get(i);
                var catchVar = ct.getParameter().getName();
                var catchType = ct.getParameter().getType();
                var alternatives = catchType instanceof JCTree.JCTypeUnion
                    ? ((JCTree.JCTypeUnion) catchType).alternatives
                    : List.of(catchType);

                if (i == 0) out.write("if(");
                else out.write(" else if(");

                out.mkString(alternatives, alt -> {
                    out.write(firstCatchVar);
                    out.write(" instanceof ");
                    out.write(replaceOneSymbolInName(alt.type.tsym.getQualifiedName(), ".", "_"));
                }, "", " || ", "");

                out.write(")");
                out.writeCBOpen(true);
                for (var blockStm : ct.getBlock().getStatements()) {
                    if (i != 0) {
                        out.write("const ");
                        out.write(catchVar);
                        out.write(" = ");
                        out.write(firstCatchVar);
                        out.writeLn("");
                    }
                    blockStm.accept(this);
                }
                out.writeCBEnd(false);
            }

            out.write(" else");
            out.writeCBOpen(true);
            out.write("throw ");
            out.write(firstCatchVar);
            out.writeLn(";");
            out.writeCBEnd(true);
            out.writeCBEnd(false);
        }

        var finallyBlock = tryStmt.getFinallyBlock();
        if (finallyBlock != null) {
            out.write(" finally ");
            finallyBlock.accept(this);
            out.writeNL();
        }
    }
}