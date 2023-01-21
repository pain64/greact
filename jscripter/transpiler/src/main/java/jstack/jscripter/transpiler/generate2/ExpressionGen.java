package jstack.jscripter.transpiler.generate2;

import jstack.jscripter.transpiler.generate.util.CompileException;
import jstack.jscripter.transpiler.generate.util.Overloads;
import jstack.jscripter.transpiler.generate2.lookahead.HasAsyncCalls;
import jstack.jscripter.transpiler.model.ClassRef;
import jstack.jscripter.transpiler.model.ErasedInterface;
import jstack.jscripter.transpiler.model.JSNativeAPI;
import jstack.jscripter.transpiler.model.async;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Arrays;
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
        return tsym.getQualifiedName().equals(super.names.fromString("jstack.jscripter.transpiler.model.MemberRef"));
    }

    boolean isNotNativeSymbol(Symbol.TypeSymbol tsym) {
        return !tsym.getQualifiedName().equals(super.names.fromString("java.lang.Object")) &&
            !tsym.getQualifiedName().equals(super.names.fromString("java.lang.String")) && // smart check native?
            !tsym.getQualifiedName().equals(super.names.fromString("java.lang.Integer"));
    }

    void reflectWriteClassMembers(Type classType) {
        if (classType.tsym instanceof Symbol.ClassSymbol classSym) {
            out.writeCBOpen(false);
            out.write("_name: () => '");
            out.write("" + classSym.className());
            out.writeLn("',");

            out.writeLn("_params: () => [");
            out.deepIn();
            if (classType instanceof Type.ArrayType arrayType) {
                reflectWriteClassMembers(arrayType.elemtype);
                out.writeLn("");
            }
            out.deepOut();
            out.writeLn("],");

            out.writeLn("_fields: () => [");
            out.deepIn();
            for (var i = 0; i < classSym.getRecordComponents().length(); i++) {
                var comp = classSym.getRecordComponents().get(i);

                out.writeCBOpen(false);
                if (comp.type.tsym instanceof Symbol.ClassSymbol) {
                    out.write("_name: () => '");
                    out.write(comp.name);
                    out.writeLn("',");
                    out.write("___class__: () => (");
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
                out.write(id.getName());
            else {
                if (equalsNameAndString(varSym.getQualifiedName(), "this"))
                    out.write("this");
                else if (id.sym.getModifiers().contains(Modifier.STATIC))
                    out.writeRightName(id.sym);
                else {
                    var index = -1;
                    for (var i = classDefs.size() - 1; i >= 0; i--)
                        if (classDefs.get(i).sym == id.sym.owner) {
                            index = i;
                            break;
                        }

                    out.write("this");
                    if (index != classDefs.size() - 1 && index != -1)
                        out.write(String.valueOf(index));
                    out.write(".");
                    out.write(id.getName());
                }
            }
        } else if (id.sym instanceof Symbol.ClassSymbol) {
            var owner = id.sym.owner;
            if (owner != null) {
                out.replaceSymbolAndWrite(owner.getQualifiedName(), '.', '_');
                var delim = id.sym.isStatic() ? "." : "_";
                out.write(delim);
                out.write(id.name);
            } else
                out.replaceSymbolAndWrite(id.sym.getQualifiedName(), '.', '_');
        } else if (id.sym instanceof Symbol.MethodSymbol) {
            var isStatic_ = id.sym.isStatic();

            if (id.sym.getKind() == ElementKind.CONSTRUCTOR) out.write("super");
            else if (isStatic_) {
                if (id.sym.owner != super.classDefs.lastElement().sym) { // import static symbol
                    out.replaceSymbolAndWrite(id.sym.owner.getQualifiedName(), '.', '_');
                    out.write("._");
                } else if (isStaticMethodCall) out.write("this._");
                else out.write("this.constructor._");
                out.write(id.name);
            } else {
                out.write("this.");
                if ((id.sym.flags_field & Flags.NATIVE) == 0) out.write("_");
                out.write(id.name);
            }
            // withStaticMethodCall(isStatic_, () -> ); // TODO: Make this
        } else
            out.write(id.name);
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
            case EQUAL_TO -> "===";
            case NOT_EQUAL_TO -> "!==";
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
        out.write(select.selected.type instanceof Type.PackageType ? "_" : ".");
        out.write(select.name);
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

            out.write("{_memberNames: () => ");
            // FIXME: use mkString
            out.write(fields.stream().map(f -> "'" + f + "'").collect(Collectors.joining(", ", "[", "]")));
            out.write(", _value: (v) => v.");
            // FIXME: use mkString
            out.write(String.join(".", fields));
            out.write(", _className: () => '");
            out.write(((Symbol.ClassSymbol) lmb.type.allparams().get(1).tsym).className());
            out.write("'}");
        } else {
            var invokeMethod = ((Symbol.ClassSymbol) lmb.type.tsym)
                .members_field.getSymbols().iterator().next();
//          var invokeMethod = lmb.type.tsym.getEnclosedElements().stream()
//                .filter(el -> el instanceof Symbol.MethodSymbol && !((Symbol.MethodSymbol) el).isDefault())
//                .findFirst().get(); // FIXME

            var isAsync = invokeMethod.getAnnotation(async.class) != null;
            var visitor = new HasAsyncCalls(super.stdShim, super.types);
            lmb.body.accept(visitor);
            if (isAsync && visitor.hasAsyncCalls) out.write("async ");

            out.mkString(lmb.params, arg ->
                out.write(arg.getName()), "(", ", ", ") => ");

            super.lambdaAsyncInference.put(lmb, visitor.hasAsyncCalls);
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

        if (methodOwnerSym.fullname.equals(names.fromString("jstack.jscripter.transpiler.model.JSExpression"))) {
            if (methodSym.name.equals(names.fromString("ofAsync")) && !isAsyncContext) {
                var line = super.cu.getLineMap().getLineNumber(call.pos);
                var col = super.cu.getLineMap().getColumnNumber(call.pos);
                throw new CompileException(CompileException.ERROR.ASYNC_INVOCATION_NOT_ALLOWED,
                    """
                        At %s:%d:%d
                        cannot invoke async method in this context due to:
                        - enclosing method does not declared as @async (OR)
                        - enclosing lambda interface does not declared as @async (OR)
                        - class init block (OR)
                        """.formatted(super.cu.sourcefile.getName(), line, col)
                );
            }

            var unescaped = call.getArguments().get(0).toString()
                .replace("\\n", "\n")
                .replace("\\'", "'");
            out.write(unescaped.substring(1, unescaped.length() - 1));
        } else if (methodOwnerSym.fullname.equals(names.fromString("jstack.jscripter.transpiler.model.ClassRef")) &&
            methodSym.name.equals(names.fromString("of"))) {
            call.args.head.accept(this);
            out.write(".__class__");
        } else {
            // FIXME: одинаковый код с HasAsyncCallsVisitor
            var shimmedType = super.stdShim.findShimmedType(methodOwnerSym.type);
            var targetMethod = shimmedType != null
                ? super.stdShim.findShimmedMethod(shimmedType, methodSym)
                : methodSym;
            var info = shimmedType != null
                ? Overloads.methodInfo(super.types, (TypeElement) shimmedType.tsym, targetMethod)
                : Overloads.methodInfo(super.types, (TypeElement) methodOwnerSym.type.tsym, methodSym);

            final boolean isAsync;
            if (call.meth instanceof JCTree.JCFieldAccess fa &&
                fa.selected instanceof JCTree.JCParens parens) {

                if (parens.expr instanceof JCTree.JCLambda lmb) {
                    var visitor = new HasAsyncCalls(super.stdShim, super.types);
                    lmb.body.accept(visitor);
                    isAsync = visitor.hasAsyncCalls;
                } else if (parens.expr instanceof JCTree.JCAssign assign &&
                    assign.rhs instanceof JCTree.JCLambda lmb) {
                    var visitor = new HasAsyncCalls(super.stdShim, super.types);
                    lmb.body.accept(visitor);
                    isAsync = visitor.hasAsyncCalls;
                } else isAsync = info.isAsync();
            } else isAsync = info.isAsync();

            if (isAsync && !isAsyncContext) {
                var line = super.cu.getLineMap().getLineNumber(call.pos);
                var col = super.cu.getLineMap().getColumnNumber(call.pos);
                throw new CompileException(CompileException.ERROR.ASYNC_INVOCATION_NOT_ALLOWED,
                    """
                        At %s:%d:%d for %s
                        cannot invoke async method in this context due to:
                        - enclosing method does not declared as @async (OR)
                        - enclosing lambda interface does not declared as @async (OR)
                        - class init block (OR)
                        """.formatted(super.cu.sourcefile.getName(), line, col, call.toString())
                );
            }

            if (isAsync) out.write("(await ");
            if (call.meth instanceof JCTree.JCIdent id) { // call local or static import
                if (id.name.equals(names.fromString("this"))) {
                    out.write("$over = ");
                    out.write(String.valueOf(info.n()));
                    out.writeLn(";");
                    out.mkString(call.args, arg -> arg.accept(this), "__args = [", ", ", "]");
                    out.writeLn(";");
                    out.write("continue __cons");
                    return;
                } else
                    id.accept(this);
                out.write("(");
            } else if (call.meth instanceof JCTree.JCFieldAccess prop) {
                if (info.mode() == Overloads.Mode.INSTANCE) {
                    var isNotOverEquals = methodOwnerSym.members_field.findFirst(
                        EQUALS_METHOD_NAME, m -> m.getMetadata() != null
                    ) == null;

                    if (equalsNameAndString(methodSym.getQualifiedName(), "equals") && isNotOverEquals)
                        out.write(prop.toString().replace(".equals", " == ")); // TODO
                    else {
                        if (methodOwnerSym.type.tsym.getAnnotation(FunctionalInterface.class) != null) {
                            prop.selected.accept(this);
                        } else {
                            prop.selected.accept(this);
                            out.write(".");

                            if (!isRecordAccessor &&
                                (prop.sym.flags_field & Flags.NATIVE) == 0 &&
                                isNotNativeSymbol(prop.sym.owner.type.tsym)
                            ) out.write("_");

                            out.write(prop.name);
                        }
                    }
                    if (!isRecordAccessor) out.write("(");
                } else {
                    var onType = shimmedType != null ? shimmedType : methodOwnerSym.type;
                    if (onType.tsym.isStatic() &&
                        onType.tsym.owner instanceof Symbol.ClassSymbol owner) {
                        // static inner class
                        out.replaceSymbolAndWrite(owner.getQualifiedName(), '.', '_');
                        out.write(".");
                        out.write(onType.tsym.name);
                    } else
                        out.replaceSymbolAndWrite(onType.tsym.getQualifiedName(), '.', '_');
                    out.write("._");
                    out.write(prop.name);

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
            if (isAsync) out.write(")");
        }
    }

    @Override public void visitReference(JCTree.JCMemberReference ref) {
        if (isMemberRefSymbol(ref.type.tsym)) {
            if (ref.expr.type.tsym instanceof Symbol.ClassSymbol) {
                //    if (!clSymbol.isRecord()) throw memberRefUsedIncorrect();
            } else throw memberRefUsedIncorrect();

            out.write("{_memberNames: () => ['");
            out.write(ref.name);
            out.write("'], _value: (v) => v.");
            out.write(ref.name);
            out.write(", _className: () => '");
            out.write(((Symbol.ClassSymbol) ref.type.allparams().get(1).tsym).className());
            out.write("'}");
        } else {
            var tSym = TreeInfo.symbol(ref.expr);
            var mSym = TreeInfo.symbol(ref);
            var info = Overloads.methodInfo(types, (TypeElement) tSym.type.asElement(), (ExecutableElement) mSym);
            boolean flagNew = true; // FIXME: сделать иммутабельным

            if (info.mode() == Overloads.Mode.STATIC) {
                out.replaceSymbolAndWrite(ref.expr.type.tsym.getQualifiedName(), '.', '_');

                out.write("._");
                out.write(ref.name);
                out.write(".bind(");

                out.replaceSymbolAndWrite(ref.expr.type.tsym.getQualifiedName(), '.', '_');
            } else if (ref.toString().endsWith("new")) {
                out.write("((x) => new ");
                out.replaceSymbolAndWrite(tSym.owner.getQualifiedName(), '.', '_');
                out.write(".");
                out.write(tSym.type.tsym.getSimpleName());
                out.write("(");
                if (info.isOverloaded()) {
                    out.write(String.valueOf(info.n()));
                    out.write(", ");
                }
                out.write("x)");
                flagNew = false;
            } else {
                if (ref.expr instanceof JCTree.JCIdent ident && ident.sym instanceof Symbol.ClassSymbol) {
                    out.write("((self) => self._");
                    out.write(ref.name);
                    out.write("()");
                } else {
                    ref.expr.accept(this);
                    out.write("._");
                    out.write(ref.name);
                    out.write(".bind(");
                    ref.expr.accept(this);
                }
            }

            if (flagNew && info.isOverloaded()) {
                out.write(", ");
                out.write(String.valueOf(info.n()));
                out.write(")");
            } else out.write(")");
        }
    }

    @Override public void visitTypeTest(JCTree.JCInstanceOf instanceOf) {
        var type = instanceOf.getType();

        if (type.type.tsym.getAnnotation(ErasedInterface.class) != null)
            throw new CompileException(CompileException.ERROR.INSTANCE_OF_NOT_APPLICABLE_TO_ERASED_INTERFACE,
                """
                    Cannot apply instanceof operator to @ErasedInterface
                    """);

        // FIXME: disable for arrays (aka x instanceof String[])
        Consumer<Runnable> checkGen = switch (type.type.tsym.getQualifiedName().toString()) {
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
                if (type.type.isInterface()) { // (n => (typeof n.__iface_instance__ !== "undefined" && n.__iface_instance__(_InterfaceC)))(c)
                    out.write("(n => (typeof n.__iface_instance__ !== \"undefined\" && n.__iface_instance__( ");
                    out.writeRightName(TreeInfo.symbol(type));
                    out.write(")))(");
                    eGen.run();
                    out.write(")");
                } else {
                    eGen.run();
                    out.write(" instanceof ");
                    if (type.type.tsym.getAnnotation(JSNativeAPI.class) == null)
                        out.writeRightName(TreeInfo.symbol(type));
                    else out.write(type.type.tsym.name);
                }
            };
        };

        var pattern = instanceOf.getPattern();
        if (pattern == null) checkGen.accept(() -> instanceOf.expr.accept(this));
        else {
            // FIXME:
            //  1. before method body gen
            //    - find all insanceof
            //    - write all pattern vars at function begin
            var name = ((JCTree.JCBindingPattern) pattern).var.name;
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
        if (newClass.clazz.type.tsym.getAnnotation(FunctionalInterface.class) != null)
            throw new CompileException(CompileException.ERROR.CANNOT_BE_CREATED_VIA_NEW,
                "Instance of interface marked as @FunctionalInterface cannot be created via new");

        if (newClass.def != null) {
            out.write("(this");
            out.write(String.valueOf(classDefs.size() - 1));
            out.write(" =>");
            out.writeCBOpen(true);
            out.write("return ");
        }

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

        if (newClass.def != null) {
            out.writeNL();
            out.writeCBEnd(false);
            out.write(")(this)");
        }
    }

    public boolean equalsNameAndString(Name name, String string) {
        var thisBytes = (byte[]) Output.STRING_VALUE_HANDLE.get(string);

        return name.getByteLength() == thisBytes.length &&
            Arrays.equals(name.getByteArray(), name.getByteOffset(),
                name.getByteOffset() + name.getByteLength(),
                thisBytes, 0, name.getByteLength());
    }
}