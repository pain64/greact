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
            genExpr(deep + 2, assign.getExpression());
        } else if (expr instanceof IdentifierTree id) {
            write(0, id.getName().toString());
        } else if (expr instanceof ConditionalExpressionTree ternary) {
            genExpr(deep + 2, ternary.getCondition());
            write(0, " ? ");
            genExpr(deep + 2, ternary.getTrueExpression());
            write(0, " : ");
            genExpr(deep + 2, ternary.getFalseExpression());
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
            genExpr(deep + 2, unary.getExpression());
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

            genExpr(deep + 2, binary.getLeftOperand());
            write(0, " ");
            write(0, op);
            write(0, " ");
            genExpr(deep + 2, binary.getRightOperand());
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
            genExpr(deep + 2, compoundAssign.getExpression());
        } else if (expr instanceof NewArrayTree newArray) {
            var init = newArray.getInitializers();
            if (init == null) init = Collections.emptyList();
            mkString(init, e -> genExpr(deep + 2, e), "[", ", ", "]");
        }

        // ARRAY_ACCESS
        // MEMBER_SELECT
        // TYPE_CAST
        // LAMBDA_EXPRESSION
        // PARENTHESIZED
        // INSTANCE_OF
        // SWITCH_EXPRESSION
        // ...
        // METHOD_INVOCATION :deal with overload
        // MEMBER_REFERENCE  :deal with overload
        // NEW_CLASS
    }

    void genStmt(int deep, StatementTree stmt) {
        switch (stmt.getKind()) {
            case RETURN -> {
                write(0, "return ");
                genExpr(deep, ((ReturnTree) stmt).getExpression());
            }
            case VARIABLE -> {
                var varDecl = (VariableTree) stmt;
                write(0, "let ");
                write(0, varDecl.getName().toString());
                write(0, " = ");

                var initializer = varDecl.getInitializer();
                if (initializer != null)
                    genExpr(deep + 2, varDecl.getInitializer());
                else
                    write(0, "null");
            }
            case EXPRESSION_STATEMENT -> genExpr(deep, ((ExpressionStatementTree) stmt).getExpression());
        }
    }
    // ASSERT
    // BLOCK
    // BREAK
    // CONTINUE
    // DO_WHILE_LOOP
    // ENHANCED_FOR_LOOP
    // FOR_LOOP
    // IF
    // LABELED_STATEMENT
    // SWITCH
    // THROW
    // TRY
    // WHILE_LOOP

    void genMethod(int deep, ExecutableElement methodEl) {
        write(deep, methodEl.getModifiers()
            .contains(Modifier.STATIC) ? "static " : "");
        write(0, methodEl.getSimpleName().toString());

        mkString(methodEl.getParameters(),
            (param) -> write(0, param.getSimpleName().toString()), "(", ", ", ")");

        var statements = Trees.instance(env).getTree(methodEl).getBody().getStatements();
        mkString(statements, stmt -> {
            write(deep + 2, "");
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
