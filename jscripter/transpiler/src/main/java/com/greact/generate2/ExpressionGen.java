package com.greact.generate2;

import com.greact.generate.util.CompileException;
import com.greact.generate.util.Overloads;
import com.greact.model.ClassRef;
import com.greact.model.async;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Collectors;

abstract class ExpressionGen extends VisitorWithContext {
    CompileException memberRefUsedIncorrect() {
        return new CompileException(CompileException.ERROR.MEMBER_REF_USED_INCORRECT, """
            MemberRef<T> usage:
              for record fields:
                record X(long field) {}
                MemberRef<X, Long> ref = X::field;
              for class fields:
                class X { long field; }
                MemberRef<X, Long> ref = x -> x.field;
              for nested fields (class or record):
                record X { long b; }
                record Y { long a; }
                MemberRef<Y, Long> ref = y -> y.a.b
                """);
    }

    java.util.List<String> memberRefExtractFields(java.util.List<String> acc, JCTree tree) {
        if (tree instanceof JCTree.JCIdent) {
            return acc;
        } else if (tree instanceof JCTree.JCFieldAccess access) {
            acc.add(access.name.toString());
            return memberRefExtractFields(acc, access.selected);
        } else if (tree instanceof JCTree.JCMethodInvocation invoke) {
            if (invoke.meth instanceof JCTree.JCFieldAccess access)
                if (access.sym.owner instanceof Symbol.ClassSymbol classSymbol)
                    if (classSymbol.getRecordComponents().stream().anyMatch(comp -> comp.accessor == access.sym)) {
                        acc.add(access.name.toString());
                        return memberRefExtractFields(acc, access.selected);
                    } else throw memberRefUsedIncorrect();
                else throw memberRefUsedIncorrect();
            else throw memberRefUsedIncorrect();
        }

        throw memberRefUsedIncorrect();
    }

    boolean isMemberRefSymbol(Symbol.TypeSymbol tsym) {
        return tsym.getQualifiedName().equals(super.names.fromString("com.greact.model.MemberRef"));
    }

    void reflectWriteClassMembers(Type classType) {
        if (classType.tsym instanceof Symbol.ClassSymbol classSym) {
            out.writeCBOpen(false);
            out.write("name: () => '");
            out.write("" + classSym.className());
            out.writeLn("',");

            out.writeLn("params: () => [");
            out.deepIn();
            if (classType instanceof Type.ArrayType arrayType) {
                reflectWriteClassMembers(arrayType.elemtype);
                out.writeLn("");
            }
            out.deepOut();
            out.writeLn("],");

            out.writeLn("fields: () => [");
            out.deepIn();
            for (var i = 0; i < classSym.getRecordComponents().length(); i++) {
                var comp = classSym.getRecordComponents().get(i);

                out.writeCBOpen(false);
                if (comp.type.tsym instanceof Symbol.ClassSymbol) {
                    out.write("name: () => '");
                    out.write(comp.name.toString());
                    out.writeLn("',");
                    out.write("__class__: () => (");
                    reflectWriteClassMembers(comp.type);
                    out.writeLn(")");
                }
                out.writeCBEnd(false);
                if (i != classSym.getRecordComponents().length() - 1)
                    out.write(",");
                out.writeNL();
            }

            out.deepOut();
            out.writeLn("]");
            out.writeCBEnd(false);
        }
    }

    @Override public void visitLiteral(JCTree.JCLiteral lit) {
        var value = lit.getValue();
        switch (lit.getKind()) {
            case CHAR_LITERAL, STRING_LITERAL -> {
                out.write("'");
                out.write(value.toString().replace("\n", "\\n"));
                out.write("'");
            }
            case NULL_LITERAL -> out.write("null");
            default -> out.write(value.toString());
        }
    }

    @Override public void visitAssign(JCTree.JCAssign assign) {
        assign.lhs.accept(this);
        out.write(" = ");
        assign.rhs.accept(this);
    }

    @Override public void visitAssignop(JCTree.JCAssignOp compoundAssign) {
        compoundAssign.lhs.accept(this);
        var op = switch (compoundAssign.getKind()) {
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
            default -> throw new RuntimeException("unknown kind: " + compoundAssign.getKind());
        };
        out.write(" ");
        out.write(op);
        out.write("= ");
        compoundAssign.rhs.accept(this);
    }

    @Override public void visitIdent(JCTree.JCIdent id) {
        if (id.sym instanceof Symbol.VarSymbol varSym) {
            if (varSym.owner instanceof Symbol.MethodSymbol)
                out.write(id.getName().toString());
            else {
                if (id.getName().toString().equals("this"))
                    out.write("this");
                else if (id.sym.getModifiers().contains(Modifier.STATIC)) {
                    var owner = (Symbol.ClassSymbol) id.sym.owner;
                    var fullName = owner.fullname.toString().replace(".", "$");
                    out.write(fullName);
                    out.write(".");
                    out.write(id.getName().toString());
                } else {
                    out.write("this.");
                    out.write(id.getName().toString());
                }
            }
        } else if (id.sym instanceof Symbol.ClassSymbol) {
            var owner = id.sym.owner;
            if (owner != null) {
                out.write(owner.toString().replace(".", "$"));
                var delim = id.sym.isStatic() ? "." : "$";
                out.write(delim);
                out.write(id.name.toString());
            } else
                out.write(id.sym.toString().replace(".", "$"));
        } else {
            // FIXME: это в каком случае???
            if (id.sym.getModifiers().contains(Modifier.STATIC)) {
                var owner = (Symbol.ClassSymbol) id.sym.owner;
                var fullName = owner.fullname.toString().replace(".", "$");
                out.write(fullName);
                out.write(".");
            }

            out.write(id.getName().toString());
        }
    }

    @Override public void visitConditional(JCTree.JCConditional ternary) {
        ternary.cond.accept(this);
        out.write(" ? ");
        ternary.truepart.accept(this);
        out.write(" : ");
        ternary.falsepart.accept(this);
    }

    @Override public void visitUnary(JCTree.JCUnary unary) {
        var opAndIsPrefix = switch (unary.getKind()) {
            case POSTFIX_INCREMENT -> Pair.of("++", false);
            case POSTFIX_DECREMENT -> Pair.of("--", false);
            case PREFIX_INCREMENT -> Pair.of("++", true);
            case PREFIX_DECREMENT -> Pair.of("--", true);
            case UNARY_PLUS -> Pair.of("+", true);
            case UNARY_MINUS -> Pair.of("-", true);
            case BITWISE_COMPLEMENT -> Pair.of("~", true);
            case LOGICAL_COMPLEMENT -> Pair.of("!", true);
            default -> throw new RuntimeException("Unknown kind " + unary.getKind());
        };

        if (opAndIsPrefix.snd) out.write(opAndIsPrefix.fst);
        unary.arg.accept(this);
        if (!opAndIsPrefix.snd) out.write(opAndIsPrefix.fst);
    }

    @Override public void visitBinary(JCTree.JCBinary binary) {
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

        if (binary.type.isIntegral() && op.equals("/")) {
            out.write("Math.floor(");
            binary.lhs.accept(this);
            out.write(" / ");
            binary.rhs.accept(this);
            out.write(")");
        } else {
            binary.lhs.accept(this);
            out.write(" ");
            out.write(op);
            out.write(" ");
            binary.rhs.accept(this);
        }
    }

    @Override public void visitNewArray(JCTree.JCNewArray newArray) {
        var init = newArray.getInitializers();
        if (init == null) init = List.nil();
        out.mkString(init, e -> e.accept(this), "[", ", ", "]");
    }

    @Override public void visitIndexed(JCTree.JCArrayAccess access) {
        access.indexed.accept(this);
        out.write("[");
        access.index.accept(this);
        out.write("]");
    }

    @Override public void visitSelect(JCTree.JCFieldAccess select) {
        select.selected.accept(this);
        out.write(select.selected.type instanceof Type.PackageType ? "$" : ".");
        out.write(select.name.toString());
    }

    @Override public void visitTypeCast(JCTree.JCTypeCast cast) {
        cast.expr.accept(this);
    }

    @Override public void visitParens(JCTree.JCParens parens) {
        out.write("(");
        parens.expr.accept(this);
        out.write(")");
    }

    @Override public void visitLambda(JCTree.JCLambda lmb) {
        if (isMemberRefSymbol(lmb.type.tsym)) {
            var fields = memberRefExtractFields(new ArrayList<>(), lmb.body);
            Collections.reverse(fields);

            out.write("{memberNames: () => ");
            // FIXME: use mkString
            out.write(fields.stream().map(f -> "'" + f + "'").collect(Collectors.joining(", ", "[", "]")));
            out.write(", value: (v) => v.");
            // FIXME: use mkString
            out.write(String.join(".", fields));
            out.write(", className: () => '");
            out.write(((Symbol.ClassSymbol) lmb.type.allparams().get(1).tsym).className());
            out.write("'}");
        } else {
            var invokeMethod = lmb.type.tsym.getEnclosedElements().stream()
                .filter(el -> el instanceof Symbol.MethodSymbol && !((Symbol.MethodSymbol) el).isDefault())
                .findFirst().get(); // FIXME

            var isAsync = invokeMethod.getAnnotation(async.class) != null;
            if (isAsync) out.write("async ");

            out.mkString(lmb.params, arg ->
                out.write(arg.getName().toString()), "(", ", ", ") => ");

            withAsyncContext(isAsync, () -> lmb.body.accept(this));
        }
    }

    @Override public void visitSwitchExpression(JCTree.JCSwitchExpression swe) {
        out.write("(() =>");
        out.writeCBOpen(true);
        out.write("switch");
        swe.selector.accept(this);
        out.writeCBOpen(true);

        swe.cases.forEach(caseStmt -> caseStmt.accept(this));

        out.writeCBEnd(true);
        out.writeCBEnd(false);
        out.write(")()");
    }

    @Override public void visitApply(JCTree.JCMethodInvocation call) {
        var methodSym = (Symbol.MethodSymbol) TreeInfo.symbol(call.meth);
        var methodOwnerSym = (Symbol.ClassSymbol) methodSym.owner;

        boolean isRecordAccessor = methodOwnerSym.isRecord() && methodOwnerSym.getRecordComponents().stream()
            .anyMatch(rc -> rc.getAccessor() == methodSym);

        if (methodOwnerSym.fullname.equals(names.fromString("com.greact.model.JSExpression")) &&
            methodSym.name.equals(names.fromString("of"))) {

            var unescaped = call.getArguments().get(0).toString()
                .replace("\\n", "\n")
                .replace("\\'", "'");
            out.write(unescaped.substring(1, unescaped.length() - 1));
        } else if (methodOwnerSym.fullname.equals(names.fromString("com.greact.model.ClassRef")) &&
            methodSym.name.equals(names.fromString("of"))) {
            call.args.head.accept(this);
            out.write(".__class__");
        } else {
            var shimmedType = super.stdShim.findShimmedType(methodOwnerSym.type);
            var targetMethod = shimmedType != null
                ? super.stdShim.findShimmedMethod(shimmedType, methodSym)
                : methodSym;

            var info = shimmedType != null
                ? Overloads.methodInfo(super.types, (TypeElement) shimmedType.tsym, targetMethod)
                : Overloads.methodInfo(super.types, (TypeElement) methodOwnerSym.type.tsym, methodSym);

            if (info.isAsync() && !isAsyncContext) {
                var line = super.cu.getLineMap().getLineNumber(call.pos);
                var col = super.cu.getLineMap().getColumnNumber(call.pos);
                throw new CompileException(CompileException.ERROR.MUST_BE_DECLARED_AS_ASYNC,
                    """
                        %s      at %s:%d:%d
                        method which calls @async method must be defined as @async""".formatted(
                        call, super.cu.sourcefile.getName(), line, col)
                );
            }

            if (info.isAsync()) out.write("(await ");

            // FIXME: on-demand static import, foreign module call
            if (call.meth instanceof JCTree.JCIdent id) { // call local
                var name = id.name.toString();

                if (id.sym.isStatic() && id.sym.owner != super.classDef.sym) { // import static call
                    out.write(id.sym.owner.toString().replace(".", "$"));
                    out.write(".");
                } else {
                    if (!name.equals("super")) out.write("this.");
                    if (info.mode() == Overloads.Mode.STATIC) out.write("constructor.");
                }
                out.write(name);
                out.write("(");
            } else if (call.meth instanceof JCTree.JCFieldAccess prop) {
                if (info.mode() == Overloads.Mode.INSTANCE) {
                    boolean isNotOverEquals = true; // FIXME: сделать immutable
                    for (var enclosedElement : methodOwnerSym.getEnclosedElements()) {
                        // FIXME: написать нормально
                        if (enclosedElement.name.toString().equals("equals") && !(enclosedElement.getMetadata() == null))
                            isNotOverEquals = false;
                    }
                    if (methodSym.name.toString().equals("equals") && isNotOverEquals)
                        out.write(prop.toString().replace(".equals", " == "));
                    else {
                        if (methodOwnerSym.type.tsym.getAnnotation(FunctionalInterface.class) != null)
                            prop.selected.accept(this);
                        else
                            prop.accept(this);
                    }
                    // FIXME: это тоже странно
                    if (!isRecordAccessor) out.write("(");
                } else {
                    var onType = shimmedType != null ? shimmedType : methodOwnerSym.type;
                    out.write(onType.tsym.toString().replace(".", "$"));
                    out.write(".");
                    out.write(prop.name.toString());

                    if (info.mode() == Overloads.Mode.AS_STATIC) {
                        out.write(".call(");
                        prop.selected.accept(this);
                        out.write(", ");
                    } else
                        out.write("(");
                }
            } else
                throw new RuntimeException("unknown kind: " + call.meth.getKind());

            if (info.isOverloaded()) {
                out.write("" + info.n());
                out.write(", ");
            }

            for (var i = 0; i < call.args.size(); i++) {
                var param = targetMethod.isVarArgs() && i >= targetMethod.params.length()
                    ? targetMethod.params.last()
                    : targetMethod.params.get(i);
                var isReflexive = param.getAnnotation(ClassRef.Reflexive.class) != null;
                var arg = (JCTree.JCExpression) call.getArguments().get(i);

                if (isReflexive) {
                    out.write("(() =>");
                    out.writeCBOpen(true);
                    out.write("const __obj = ");
                }

                arg.accept(this);

                if (isReflexive) {
                    out.writeLn(";");
                    if (arg.type.tsym instanceof Symbol.ClassSymbol) {
                        out.write("__obj.__class__ = (");
                        reflectWriteClassMembers(arg.type);
                        out.writeLn(");");
                        out.writeLn("return __obj;");
                    }
                    out.writeCBEnd(false);
                    out.write(")()");
                }
                if (i != call.getArguments().size() - 1) out.write(", ");
            }

            if (!isRecordAccessor) out.write(")");
            if (info.isAsync()) out.write(")");
        }
    }

    @Override public void visitReference(JCTree.JCMemberReference ref) {
        if (isMemberRefSymbol(ref.type.tsym)) {
            if (ref.expr.type.tsym instanceof Symbol.ClassSymbol) {
                //    if (!clSymbol.isRecord()) throw memberRefUsedIncorrect();
            } else throw memberRefUsedIncorrect();

            out.write("{memberNames: () => ['");
            out.write(ref.name.toString());
            out.write("'], value: (v) => v.");
            out.write(ref.name.toString());
            out.write(", className: () => '");
            out.write(((Symbol.ClassSymbol) ref.type.allparams().get(1).tsym).className());
            out.write("'}");
        } else {
            var tSym = TreeInfo.symbol(ref.expr);
            var mSym = TreeInfo.symbol(ref);
            var info = Overloads.methodInfo(types, (TypeElement) tSym.type.asElement(), (ExecutableElement) mSym);
            boolean flagNew = true; // FIXME: сделать иммутабельным

            if (info.mode() == Overloads.Mode.STATIC) {
                var fullClassName = tSym.packge().toString().replace(".", "$") +
                    "$" + ref.expr;
                out.write(fullClassName);
                out.write(".");
                out.write(ref.name.toString());
                out.write(".bind(");
                out.write(fullClassName);
            } else if (ref.toString().endsWith("new")) {
                // FIXME: piece of shit
                var stringBuilder = new StringBuilder(tSym.toString().replace(".", "$"));
                stringBuilder.setCharAt(stringBuilder.lastIndexOf("$"), '.');
                out.write("((x) => new ");
                out.write(stringBuilder.toString());
                out.write("(");
                if(info.isOverloaded()) {
                    out.write(String.valueOf(info.n()));
                    out.write(", ");
                }
                out.write("x)");
                flagNew = false;
            } else {
                if (ref.expr instanceof JCTree.JCIdent ident && ident.sym instanceof Symbol.ClassSymbol) {
                    out.write("((self) => self.");
                    out.write(ref.name.toString());
                    out.write("()");
                } else {
                    ref.expr.accept(this);
                    out.write(".");
                    out.write(ref.name.toString());
                    out.write(".bind(");
                    ref.expr.accept(this);
                }
            }

            if (flagNew && info.isOverloaded()) out.write(", " + info.n() + ")");
            else out.write(")");
        }
    }

    @Override public void visitTypeTest(JCTree.JCInstanceOf instanceOf) {
        var ofType = TreeInfo.symbol(instanceOf.getType())
            .getQualifiedName().toString();

        // FIXME: disable for arrays (aka x instanceof String[])
        Consumer<Runnable> checkGen = switch (ofType) {
            case "java.lang.String" -> eGen -> {
                out.write("(($x) => {return typeof $x === 'string' || $x instanceof String})(");
                eGen.run();
                out.write(")");
            };
            case "java.lang.Integer", "java.lang.Long", "java.lang.Float" -> eGen -> {
                out.write("typeof ");
                eGen.run();
                out.write(" == 'number'");
            };
            default -> eGen -> {
                eGen.run();
                out.write(" instanceof ");
                out.write(ofType.replace(".", "$"));
            };
        };

        var pattern = instanceOf.getPattern();
        if (pattern == null) checkGen.accept(() -> instanceOf.expr.accept(this));
        else {
            // FIXME:
            //  1. before method body gen
            //    - find all insanceof
            //    - write all pattern vars at function begin
            var name = ((JCTree.JCBindingPattern) pattern).var.name.toString();
            out.write("(");
            out.write(name);
            out.write(" = ");
            instanceOf.expr.accept(this);
            out.write(", ");
            checkGen.accept(() -> out.write(name));
            out.write(")");
        }
    }

    @Override public void visitTypeApply(JCTree.JCTypeApply ta) {
        ta.clazz.accept(this);
    }

    @Override public void visitNewClass(JCTree.JCNewClass newClass) {
        out.write("new ");
        var info = Overloads.methodInfo(
            super.types,
            (TypeElement) (newClass.type.tsym),
            (ExecutableElement) newClass.constructor);


        if (newClass.def != null) newClass.def.accept(this);
        else newClass.clazz.accept(this);

        out.write("(");
        if (info.isOverloaded()) {
            out.write("" + info.n());
            out.write(", ");
        }

        out.mkString(newClass.args, arg -> arg.accept(this), "", ", ", ")");
    }
}