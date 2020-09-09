package com.greact.generate;

import com.sun.source.tree.*;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;

public class JSGen {
    final Writer out;
    final CompilationUnitTree cu;
    final JavacProcessingEnvironment env;
    final BasicJavacTask task;

    public JSGen(Writer out, CompilationUnitTree cu, JavacProcessingEnvironment env, BasicJavacTask task) {
        this.out = out;
        this.cu = cu;
        this.env = env;
        this.task = task;
    }

    void write(int sp, String text) {
        try {
            for (int i = 0; i < sp; i++)
                out.write(' ');
            out.write(text);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    <T> void mkString(Iterator<T> iter, Consumer<T> fn, String prefix, String delim, String suffix) {
        var isFirst = true;

        write(0, prefix);
        while (iter.hasNext()) {
            fn.accept(iter.next());
            if (isFirst && iter.hasNext()) {
                isFirst = false;
                write(0, delim);
            }
        }
        write(0, suffix);
    }

    <T> void mkString(Iterable<T> iterable, Consumer<T> fn, String prefix, String delim, String suffix) {
        mkString(iterable.iterator(), fn, prefix, delim, suffix);
    }

    void genExpr(int deep, ExpressionTree expr) {
        if (expr instanceof LiteralTree) {
            var value = ((LiteralTree) expr).getValue();
            switch (expr.getKind()) {
                case CHAR_LITERAL, STRING_LITERAL -> {
                    write(0, "'");
                    write(0, value.toString());
                    write(0, "'");
                }
                case NULL_LITERAL -> write(0, "null");
                default -> write(0, value.toString());

            }
        } else if (expr instanceof AssignmentTree assign) {
            write(0, assign.getVariable().toString());
            write(0, " = ");
            genExpr(deep, assign.getExpression());
        } else if (expr instanceof IdentifierTree id) {
            write(0, id.getName().toString());
        } else if (expr instanceof ConditionalExpressionTree ternary) {
            genExpr(deep, ternary.getCondition());
            write(0, " ? ");
            genExpr(deep, ternary.getTrueExpression());
            write(0, " : ");
            genExpr(deep, ternary.getFalseExpression());
        } else if (expr instanceof UnaryTree unary) {
            var opAndIsPrefix = switch (expr.getKind()) {
                case POSTFIX_INCREMENT -> Pair.of("++", false);
                case POSTFIX_DECREMENT -> Pair.of("--", false);
                case PREFIX_INCREMENT -> Pair.of("++", true);
                case PREFIX_DECREMENT -> Pair.of("--", true);
                case UNARY_PLUS -> Pair.of("+", true);
                case UNARY_MINUS -> Pair.of("-", true);
                case BITWISE_COMPLEMENT -> Pair.of("~", true);
                case LOGICAL_COMPLEMENT -> Pair.of("!", true);
                default -> throw new RuntimeException("Unknown kind " + expr.getKind());
            };

            if (opAndIsPrefix.snd) write(0, opAndIsPrefix.fst);
            genExpr(deep, unary.getExpression());
            if (!opAndIsPrefix.snd) write(0, opAndIsPrefix.fst);
        } else if (expr instanceof BinaryTree binary) {
            var op = switch (binary.getKind()) {
                case MULTIPLY -> "*";
                case DIVIDE -> "/";
                case REMAINDER -> "%";
                case PLUS -> "+";
                case MINUS -> "-";
                case LEFT_SHIFT -> "<<";
                case RIGHT_SHIFT -> ">>";
                case UNSIGNED_RIGHT_SHIFT -> ">>>";
                case LESS_THAN -> "<";
                case GREATER_THAN -> ">";
                case LESS_THAN_EQUAL -> "<=";
                case GREATER_THAN_EQUAL -> ">=";
                case EQUAL_TO -> "==";
                case NOT_EQUAL_TO -> "!=";
                case AND -> "&";
                case XOR -> "^";
                case OR -> "|";
                case CONDITIONAL_AND -> "&&";
                case CONDITIONAL_OR -> "||";
                default -> throw new RuntimeException("unexpected kind: " + binary.getKind());
            };

            genExpr(deep, binary.getLeftOperand());
            write(0, " ");
            write(0, op);
            write(0, " ");
            genExpr(deep, binary.getRightOperand());
        } else if (expr instanceof CompoundAssignmentTree compoundAssign) {
            write(0, compoundAssign.getVariable().toString());
            var op = switch (expr.getKind()) {
                case MULTIPLY_ASSIGNMENT -> "*";
                case DIVIDE_ASSIGNMENT -> "/";
                case REMAINDER_ASSIGNMENT -> "%";
                case PLUS_ASSIGNMENT -> "+";
                case MINUS_ASSIGNMENT -> "-";
                case LEFT_SHIFT_ASSIGNMENT -> "<<";
                case RIGHT_SHIFT_ASSIGNMENT -> ">>";
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT -> ">>>";
                case AND_ASSIGNMENT -> "&";
                case XOR_ASSIGNMENT -> "^";
                case OR_ASSIGNMENT -> "|";
                default -> throw new RuntimeException("unknown kind: " + expr.getKind());
            };
            write(0, " ");
            write(0, op);
            write(0, "= ");
            genExpr(deep, compoundAssign.getExpression());
        } else if (expr instanceof NewArrayTree newArray) {
            var init = newArray.getInitializers();
            if (init == null) init = Collections.emptyList();
            mkString(init, e -> genExpr(deep, e), "[", ", ", "]");
        } else if (expr instanceof ArrayAccessTree accessTree) {
            genExpr(deep, accessTree.getExpression());
            write(0, "[");
            write(0, accessTree.getIndex().toString());
            write(0, "]");
        } else if (expr instanceof MemberSelectTree memberSelect) {
            genExpr(deep, memberSelect.getExpression());
            write(0, ".");
            write(0, memberSelect.getIdentifier().toString());
        } else if (expr instanceof TypeCastTree cast) {
            genExpr(deep, cast.getExpression()); // erased
        } else if (expr instanceof ParenthesizedTree parens) {
            write(0, "(");
            genExpr(deep, parens.getExpression());
            write(0, ")");
        } else if (expr instanceof LambdaExpressionTree lambda) {
            mkString(lambda.getParameters(), (arg) ->
                write(0, arg.getName().toString()), "(", ", ", ") =>");
            genBlock(deep, lambda.getBody());
        } else if (expr instanceof SwitchExpressionTree switchExpr) {
            write(0, "(() => {\n");
            write(deep + 2, "switch");
            genExpr(deep + 2, switchExpr.getExpression());
            write(0, " {\n");
            var cases = switchExpr.getCases();
            cases.forEach((caseStmt) -> {
                if (caseStmt.getExpressions().isEmpty())
                    write(deep + 4, "default:\n");
                else
                    caseStmt.getExpressions().forEach((caseExpr) -> {
                        write(deep + 4, "case ");
                        genExpr(deep + 4, caseExpr);
                        write(0, ":\n");
                    });

                var body = caseStmt.getBody();

                if (body instanceof BlockTree block) {
                    block.getStatements().forEach((bStmt) -> {
                        genStmt(deep + 6, bStmt);
                        write(0, "\n");
                    });
                } else if (body instanceof StatementTree stmt) {
                    genStmt(deep + 6, stmt);
                } else if (body instanceof ExpressionTree caseResult) {
                    write(deep + 6, "return ");
                    genExpr(deep + 6, caseResult);
                    write(0, "\n");
                } else
                    throw new RuntimeException("unknown kind: " + body.getKind());
            });
            write(deep + 2, "}\n");
            write(deep, "})()");
        }
        // INSTANCE_OF
        // ...
        // METHOD_INVOCATION :deal with overload
        // MEMBER_REFERENCE  :deal with overload
        // NEW_CLASS
    }

    void genBlock(int deep, Tree tree) {
        if (tree instanceof BlockTree block) {
            write(0, " {\n");
            block.getStatements().forEach((bStmt) -> {
                genStmt(deep + 2, bStmt);
                write(0, "\n");
            });
            write(deep, "}");
        } else if (tree instanceof StatementTree stmt) {
            write(0, "\n");
            genStmt(deep + 2, stmt);
        } else if (tree instanceof ExpressionTree expr) {
            //write(deep + 2, "\n");
            genExpr(deep, expr);
        } else
            throw new RuntimeException("unknown kind: " + tree.getKind());
    }

    void genStmt(int deep, StatementTree stmt) {
        if (stmt instanceof ReturnTree ret) {
            write(deep, "return ");
            genExpr(deep, ret.getExpression());
        } else if (stmt instanceof BreakTree brk) {
            write(deep, "break");
            var label = brk.getLabel();
            if (label != null) write(1, label.toString());
        } else if (stmt instanceof ContinueTree cont) {
            write(deep, "continue");
            var label = cont.getLabel();
            if (label != null) write(1, label.toString());
        } else if (stmt instanceof VariableTree varDecl) {
            write(deep, "let ");
            write(0, varDecl.getName().toString());
            write(0, " = ");

            var initializer = varDecl.getInitializer();
            if (initializer != null)
                genExpr(deep, varDecl.getInitializer());
            else
                write(0, "null");
        } else if (stmt instanceof ExpressionStatementTree exprStmt) {
            write(deep, "");
            genExpr(deep, exprStmt.getExpression());
        } else if (stmt instanceof IfTree ifStmt) {
            write(deep, "if");
            genExpr(deep, ifStmt.getCondition());
            genBlock(deep, ifStmt.getThenStatement());

            var elseStmt = ifStmt.getElseStatement();
            if (elseStmt != null) {
                write(0, "\n");
                write(deep, "else");
                genBlock(deep, elseStmt);
            }
        } else if (stmt instanceof WhileLoopTree whileStmt) {
            write(deep, "while");
            genExpr(deep, whileStmt.getCondition());
            genBlock(deep, whileStmt.getStatement());
        } else if (stmt instanceof DoWhileLoopTree doWhile) {
            write(deep, "do");
            genBlock(deep, doWhile.getStatement());
            write(0, " while");
            genExpr(deep, doWhile.getCondition());
        } else if (stmt instanceof ForLoopTree forStmt) {
            write(deep, "for");

            if (forStmt.getInitializer().isEmpty())
                write(0, "(;");
            else
                mkString(forStmt.getInitializer(), init -> {
                    var varDecl = (VariableTree) init;
                    write(0, varDecl.getName().toString());
                    write(0, " = ");
                    genExpr(deep, varDecl.getInitializer());
                }, "(let ", ", ", "; ");


            genExpr(deep, forStmt.getCondition());
            if (forStmt.getInitializer().isEmpty())
                write(0, ";)");
            else
                mkString(forStmt.getUpdate(), init ->
                    genStmt(0, init), "; ", ", ", ")");

            genBlock(deep, forStmt.getStatement());
        } else if (stmt instanceof EnhancedForLoopTree forEach) {
            write(deep, "for(");
            var varDecl = forEach.getVariable();
            write(0, "let ");
            write(0, varDecl.getName().toString());
            write(0, " in ");
            genExpr(deep, forEach.getExpression());
            write(0, ")");
            genBlock(deep, forEach.getStatement());
        } else if (stmt instanceof LabeledStatementTree label) {
            write(deep, label.getLabel().toString());
            write(0, ":\n");
            genStmt(deep, label.getStatement());
        } else if (stmt instanceof SwitchTree switchStmt) {
            write(deep, "switch");
            genExpr(deep, switchStmt.getExpression());
            write(0, " {\n");
            var cases = switchStmt.getCases();
            cases.forEach((caseStmt) -> {
                if (caseStmt.getExpressions().isEmpty()) {
                    write(deep + 2, "default:");
                } else {
                    write(deep + 2, "case");
                    mkString(caseStmt.getExpressions(), (caseExpr) ->
                        genExpr(deep + 2, caseExpr), " ", ", ", ":");
                }

                write(0, "\n");
                caseStmt.getStatements().forEach((blockStmt) -> {
                    genStmt(deep + 4, blockStmt);
                    write(0, "\n");
                });

            });
            write(deep, "}");
        } else if (stmt instanceof YieldTree yieldExpr) {
            write(deep, "return ");
            genExpr(deep, yieldExpr.getValue());
        }
    }
    // ASSERT
    // TRY
    // THROW

    void genMethod(int deep, ExecutableElement methodEl) {
        write(deep, methodEl.getModifiers()
            .contains(Modifier.STATIC) ? "static " : "");
        write(0, methodEl.getSimpleName().toString());

        mkString(methodEl.getParameters(),
            (param) -> write(0, param.getSimpleName().toString()), "(", ", ", ")");

        var statements = Trees.instance(env).getTree(methodEl).getBody().getStatements();
        mkString(statements, stmt -> {
            genStmt(deep + 2, stmt);
            write(0, "\n");
        }, " {\n", "", "  }\n");
    }

    public void genType(int deep, TypeElement typeEl) {

        if (typeEl.getKind() == ElementKind.INTERFACE)
            return;

        write(deep, "class ");
        write(deep, cu.getPackage().getPackageName().toString().replace(".", "$"));
        write(0, "$");
        write(0, typeEl.getSimpleName().toString());

        var decls = typeEl.getEnclosedElements().stream()
            .filter(el -> el.getKind() == ElementKind.METHOD).iterator();

        mkString(decls, (methodDecl) ->
            genMethod(deep + 2, (ExecutableElement) methodDecl), " {\n", "\n", "}");
    }
    // ENUM
}
