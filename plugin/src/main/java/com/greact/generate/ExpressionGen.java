package com.greact.generate;

import com.greact.generate.TypeGen.TContext;
import com.greact.generate.util.JSOut;
import com.sun.source.tree.*;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.type.ExecutableType;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExpressionGen {
    final JSOut out;
    final TContext ctx;
    final StatementGen stmtGen;

    public ExpressionGen(JSOut out, TContext ctx, StatementGen stmtGen) {
        this.out = out;
        this.ctx = ctx;
        this.stmtGen = stmtGen;
    }

    void expr(int deep, ExpressionTree expr) {
        if (expr instanceof LiteralTree) {
            var value = ((LiteralTree) expr).getValue();
            switch (expr.getKind()) {
                case CHAR_LITERAL, STRING_LITERAL -> {
                    out.write(0, "'");
                    out.write(0, value.toString());
                    out.write(0, "'");
                }
                case NULL_LITERAL -> out.write(0, "null");
                default -> out.write(0, value.toString());

            }
        } else if (expr instanceof AssignmentTree assign) {
            out.write(0, assign.getVariable().toString());
            out.write(0, " = ");
            expr(deep, assign.getExpression());
        } else if (expr instanceof IdentifierTree id) {
            out.write(0, id.getName().toString());
        } else if (expr instanceof ConditionalExpressionTree ternary) {
            expr(deep, ternary.getCondition());
            out.write(0, " ? ");
            expr(deep, ternary.getTrueExpression());
            out.write(0, " : ");
            expr(deep, ternary.getFalseExpression());
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

            if (opAndIsPrefix.snd) out.write(0, opAndIsPrefix.fst);
            expr(deep, unary.getExpression());
            if (!opAndIsPrefix.snd) out.write(0, opAndIsPrefix.fst);
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

            expr(deep, binary.getLeftOperand());
            out.write(0, " ");
            out.write(0, op);
            out.write(0, " ");
            expr(deep, binary.getRightOperand());
        } else if (expr instanceof CompoundAssignmentTree compoundAssign) {
            out.write(0, compoundAssign.getVariable().toString());
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
            out.write(0, " ");
            out.write(0, op);
            out.write(0, "= ");
            expr(deep, compoundAssign.getExpression());
        } else if (expr instanceof NewArrayTree newArray) {
            var init = newArray.getInitializers();
            if (init == null) init = Collections.emptyList();
            out.mkString(init, e -> expr(deep, e), "[", ", ", "]");
        } else if (expr instanceof ArrayAccessTree accessTree) {
            expr(deep, accessTree.getExpression());
            out.write(0, "[");
            out.write(0, accessTree.getIndex().toString());
            out.write(0, "]");
        } else if (expr instanceof MemberSelectTree memberSelect) {
            expr(deep, memberSelect.getExpression());
            out.write(0, ".");
            out.write(0, memberSelect.getIdentifier().toString());
        } else if (expr instanceof TypeCastTree cast) {
            expr(deep, cast.getExpression()); // erased
        } else if (expr instanceof ParenthesizedTree parens) {
            out.write(0, "(");
            expr(deep, parens.getExpression());
            out.write(0, ")");
        } else if (expr instanceof LambdaExpressionTree lambda) {
            out.mkString(lambda.getParameters(), (arg) ->
                out.write(0, arg.getName().toString()), "(", ", ", ") =>");
            stmtGen.block(deep, lambda.getBody());
        } else if (expr instanceof SwitchExpressionTree switchExpr) {
            out.write(0, "(() => {\n");
            out.write(deep + 2, "switch");
            expr(deep + 2, switchExpr.getExpression());
            out.write(0, " {\n");
            var cases = switchExpr.getCases();
            cases.forEach((caseStmt) -> {
                if (caseStmt.getExpressions().isEmpty())
                    out.write(deep + 4, "default:\n");
                else
                    caseStmt.getExpressions().forEach((caseExpr) -> {
                        out.write(deep + 4, "case ");
                        expr(deep + 4, caseExpr);
                        out.write(0, ":\n");
                    });

                var body = caseStmt.getBody();

                if (body instanceof BlockTree block) {
                    block.getStatements().forEach((bStmt) -> {
                        stmtGen.stmt(deep + 6, bStmt);
                        out.write(0, "\n");
                    });
                } else if (body instanceof StatementTree stmt) {
                    stmtGen.stmt(deep + 6, stmt);
                } else if (body instanceof ExpressionTree caseResult) {
                    out.write(deep + 6, "return ");
                    expr(deep + 6, caseResult);
                    out.write(0, "\n");
                } else
                    throw new RuntimeException("unknown kind: " + body.getKind());
            });
            out.write(deep + 2, "}\n");
            out.write(deep, "})()");
        } else if (expr instanceof MethodInvocationTree call) {
            var select = call.getMethodSelect();
            var mType = (ExecutableType) ctx.trees().getTypeMirror(ctx.trees().getPath(ctx.cu(), select));

            var mInfo = ((Supplier<TypeGen.OverloadInfo>) () -> {
                // FIXME: on-demand static import, foreign module call
                if (select instanceof IdentifierTree ident) { // call local
                    var name = ident.getName().toString();
                    var info = ctx.findMethod(name, mType.getParameterTypes());
                    out.write(0, "this.");
                    if (info.mi().isStatic()) out.write(0, "constructor.");
                    out.write(0, name);
                    return info;
                } else if (select instanceof MemberSelectTree prop) {
                    expr(deep, prop);
                    return ctx.findMethod(prop.getIdentifier().toString(), mType.getParameterTypes());
                } else
                    throw new RuntimeException("unknown kind: " + select.getKind());
            }).get();

            if (mInfo.isOverloaded()) out.write(0, "$" + mInfo.n());
            out.mkString(call.getArguments(), (arg) ->
                expr(deep, arg), "(", ", ", ")");
        } else if (expr instanceof MemberReferenceTree memberRef) {
            var paramTypes = ((MethodSymbol) TreeInfo.symbol((JCTree) memberRef)).getParameters()
                .stream()
                .map(p -> ctx.trees().getTypeMirror(ctx.trees().getPath(p)))
                .collect(Collectors.toList());
            var overloadInfo = ctx.findMethod(memberRef.getName().toString(), paramTypes);
            var sym = TreeInfo.symbol((JCTree) memberRef.getQualifierExpression());

            if (sym instanceof ClassSymbol classSym) {
                out.write(0, classSym.packge().toString().replace(".", "$"));
                out.write(0, "$");
                expr(deep, memberRef.getQualifierExpression());
                out.write(0, ".");
                out.write(0, memberRef.getName().toString());
                if (overloadInfo.isOverloaded())
                    out.write(0, "$" + overloadInfo.n());
            } else if (sym instanceof VarSymbol) {
                expr(deep, memberRef.getQualifierExpression());
                out.write(0, ".");
                out.write(0, memberRef.getName().toString());
                if (overloadInfo.isOverloaded())
                    out.write(0, "$" + overloadInfo.n());
                out.write(0, ".bind(this)");
            } else
                throw new RuntimeException("unknown kind: " + sym.getKind());
        }
        // INSTANCE_OF
        // NEW_CLASS
    }
}
