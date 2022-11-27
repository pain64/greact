package jstack.jscripter.transpiler.generate2;

import jstack.jscripter.transpiler.generate.util.CompileException;
import jstack.jscripter.transpiler.generate.util.CompileException.ERROR;
import jstack.jscripter.transpiler.generate.util.Overloads;
import jstack.jscripter.transpiler.generate.util.Overloads.OverloadTable;
import jstack.jscripter.transpiler.generate2.lookahead.HasAsyncCalls;
import jstack.jscripter.transpiler.generate2.lookahead.HasSelfConstructorCall;
import jstack.jscripter.transpiler.model.Static;
import jstack.jscripter.transpiler.model.async;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

abstract class ClassBodyGen extends StatementGen {
    @Override public void visitVarDef(JCTree.JCVariableDecl varDef) {
        var isEnclosingClassIsNested = varDef.sym.owner.owner.getKind().isClass();
        if (varDef.sym.isStatic() && !isEnclosingClassIsNested) {
            out.write("static ");
            out.write(varDef.getName());
            out.write(" = ");

            if (varDef.init != null) varDef.init.accept(this);
            else out.write("null");
            out.writeLn(";");
        } else if (!(varDef.sym.owner instanceof Symbol.ClassSymbol)) {
            // skip class fields & delegate to StatementGen
            super.visitVarDef(varDef);
        } else if (isEnclosingClassIsNested && varDef.sym.isStatic()) {
            if (varDef.sym.owner.isEnum())
                out.writeRightName(varDef.sym);
            else {
                out.write("static ");
                out.write(varDef.sym.getSimpleName());
            }
            out.write(" = ");

            if (varDef.init != null) varDef.init.accept(this);
            else out.write("null");
            out.writeLn(";");
        }
    }

    void initField(JCTree.JCVariableDecl varDef) {
        out.write("this.");
        out.write(varDef.getName());
        out.write(" = ");

        if (varDef.getInitializer() != null)
            varDef.getInitializer().accept(this);
        else if (varDef.getType().type.isIntegral())
            out.write("0");
        else
            out.write("null");

        out.writeLn(";");
    }

    void initRecordFields(JCTree.JCMethodDecl method) {
        if (((Symbol.ClassSymbol) method.sym.owner).isRecord())
            method.params.forEach(varDef -> {
                out.write("this.");
                out.write(varDef.getName());
                out.write(" = ");
                out.write(varDef.getName());
                out.writeLn(";");
            });
    }

    void writeMethodStatements(JCTree.JCMethodDecl method, boolean hasInit) {
        var statements = method.sym.isAbstract() && !method.sym.isDefault()
            ? com.sun.tools.javac.util.List.<JCTree.JCStatement>nil()
            : method.body.stats;

        withStaticMethodCall(method.sym.isStatic(),
            () -> {
                withAsyncContext(method.sym.getAnnotation(async.class) != null, () -> {
                    if (!statements.isEmpty()) // super constructor invocation
                        statements.get(0).accept(this);

                    if (hasInit) out.writeLn("__init__();");
                    initRecordFields(method);

                    statements.stream().skip(1).forEach(stmt -> stmt.accept(this));
                });
            });
    }

    void visitGroup(OverloadTable table, boolean isStatic,
                    List<Pair<Integer, JCTree.JCMethodDecl>> methods) {

        if (methods.isEmpty()) return;
        if (methods.stream().allMatch(m -> m.snd.sym.getModifiers().contains(Modifier.NATIVE)))
            return;
        if (methods.stream().allMatch(m -> m.snd.sym.isAbstract() && !m.snd.sym.isDefault())) // isAbstract для defoult методов возвращает true
            return;

        var method_ = methods.get(0);
        var name = method_.snd.name;

        var isConstructor = method_.snd.sym.isConstructor();
        var isAsStatic = methods.stream().anyMatch(m -> m.snd.sym.getAnnotation(Static.class) != null);

        if (isStatic || isAsStatic) out.write("static ");

        var anyMethodHasAsyncCalls = false;
        for (var method : methods) {
            var isAsync = method.snd.sym.getAnnotation(async.class) != null;
            if (isAsync && isConstructor) {
                var line = super.cu.getLineMap().getLineNumber(method.snd.pos);
                var col = super.cu.getLineMap().getColumnNumber(method.snd.pos);
                throw new CompileException(ERROR.CANNOT_BE_DECLARED_AS_ASYNC, """
                    At %s:%d:%d
                    constructor cannot be declared as @async"""
                    .formatted(super.cu.sourcefile.getName(), line, col));
            }

            var visitor = new HasAsyncCalls(super.stdShim, super.types);
            method.snd.body.accept(visitor);
            if (!visitor.hasAsyncCalls && isAsync) {
                var line = super.cu.getLineMap().getLineNumber(method.snd.pos);
                var col = super.cu.getLineMap().getColumnNumber(method.snd.pos);
                throw new CompileException(ERROR.CANNOT_BE_DECLARED_AS_ASYNC, """
                    At %s:%d:%d
                    method cannot be declared as @async due to:
                    - is not abstract or interface or native (OR)
                    - does not makes async calls"""
                    .formatted(super.cu.sourcefile.getName(), line, col));
            }
            if (visitor.hasAsyncCalls) anyMethodHasAsyncCalls = true;
        }

        if (anyMethodHasAsyncCalls) out.write("async ");


        if (isConstructor)
            out.write("constructor");
        else {
            out.write("_");
            out.write(name);
        }

        var params = methods.stream()
            .map(m -> m.snd.getParameters())
            .max(Comparator.comparingInt(List::size))
            .orElseThrow(() -> new IllegalStateException("unreachable"));

        if (!table.isOverloaded())
            out.mkString(params, param -> {
                if ((param.sym.flags_field & Flags.VARARGS) != 0)
                    out.write("...");
                out.write(param.name);
            }, "(", ", ", ")");
        else
            out.write("($over, ...__args)");

        out.writeCBOpen(true);

        final boolean hasConstructorSelfCall;
        if (isConstructor) {
            var visitor = new HasSelfConstructorCall(super.names);
            for (var method : methods) method.snd.accept(visitor);
            hasConstructorSelfCall = visitor.hasSelfConstructorCall;
        } else hasConstructorSelfCall = false;

        final boolean hasInit;
        if (isConstructor) {
            if (hasConstructorSelfCall) {
                out.write("__cons: while(1)");
                out.writeCBOpen(true);
            }

            var fields = classDefs.lastElement().sym.members_field
                .getSymbols(sym -> {
                    if (sym.getKind() != ElementKind.FIELD) return false;
                    var varSym = (Symbol.VarSymbol) sym;
                    return !varSym.getModifiers().contains(Modifier.STATIC);
                });

            var fieldsList = new ArrayList<Symbol>();
            for (var field : fields) fieldsList.add(field);

//            var fields = classDefs.lastElement().sym.getEnclosedElements().stream()
//                .filter(el -> el.getKind() == ElementKind.FIELD)
//                .map(el -> (VariableElement) el)
//                .filter(el -> !el.getModifiers().contains(Modifier.STATIC)).iterator();

            var initBlock = classDefs.lastElement().defs.stream()
                .filter(def -> def instanceof JCTree.JCBlock)
                .map(def -> (JCTree.JCBlock) def)
                .findFirst();

            hasInit = (!fieldsList.isEmpty() && !classDefs.lastElement().sym.isRecord())
                || initBlock.isPresent();

            if (hasInit) {
                withAsyncContext(false, () -> { // constructor cannot be async in JS
                    out.write("const __init__ = () =>");
                    out.writeCBOpen(true);
                    for (var i = fieldsList.size() - 1; i >= 0; i--) {
                        var field = fieldsList.get(i);
                        var varDef = (JCTree.JCVariableDecl) trees.getTree(field);
                        initField(varDef);
                    }
//                    fields.forEachRemaining(field -> {
//                        var varDef = (JCTree.JCVariableDecl) trees.getTree(field);
//                        initField(varDef);
//                    });
                    initBlock.ifPresent(block -> block.stats.forEach(stmt -> stmt.accept(this)));
                    out.writeCBEnd(false);
                    out.writeLn(";");
                });
            }
        } else hasInit = false;

        if (table.isOverloaded()) {
            var isFirst = true;
            for (var m : methods) {
                if (isFirst) out.write("if($over === ");
                else out.write(" else if($over === ");
                isFirst = false;

                out.write(String.valueOf(m.fst));
                out.write(")");
                out.writeCBOpen(true);

                if (!m.snd.params.isEmpty()) {
                    out.mkString(m.snd.params, varDef -> {
                        if ((varDef.sym.flags_field & Flags.VARARGS) != 0)
                            out.write("...");
                        out.write(varDef.name);
                    }, "const [", ", ", "] = __args;");
                    out.writeNL();
                }
                writeMethodStatements(m.snd, hasInit);
                out.writeCBEnd(false);
            }

            if (!isConstructor && table.hasInSuper()) {
                out.writeLn(" else");
                out.deepIn();
                out.write("return super._");
                out.write(name);
                out.writeLn(".apply(this, arguments);");
                out.deepOut();
            } else out.writeNL();
        } else
            writeMethodStatements(methods.get(0).snd, hasInit);

        if (hasConstructorSelfCall) {
            out.writeLn("break;");
            out.writeCBEnd(true);
        }
        out.writeCBEnd(true);
    }

    List<Pair<Integer, JCTree.JCMethodDecl>> mapMethods(
        List<JCTree.JCMethodDecl> group, List<Pair<Integer, ExecutableElement>> methods) {

        return methods.stream().map(p ->
            new Pair<>(p.fst, group.stream().filter(m -> m.sym == p.snd)
                .findFirst().orElseThrow())).toList();
    }


    @Override public void visitMethodDef(JCTree.JCMethodDecl methodDef) {
        var group = groups.get(methodDef.getName());
        if (group == null) return; // @DoNotTranspile method
        if (methodDef != group.get(0)) return;

        var table = Overloads.table(types, classDefs.lastElement().sym, methodDef.name);
        var nonStaticMethods = mapMethods(group, table.methods());
        var staticMethods = mapMethods(group, table.staticMethods());

        visitGroup(table, true, staticMethods);
        visitGroup(table, false, nonStaticMethods);
    }
}
