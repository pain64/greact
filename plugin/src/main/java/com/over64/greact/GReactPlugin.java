package com.over64.greact;

import com.sun.source.tree.Tree;
import com.sun.source.util.*;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

import java.util.List;
import java.util.stream.Collectors;


public class GReactPlugin implements Plugin {

    public static final String NAME = "GReact";

    @Override
    public String getName() {
        return NAME;
    }

    boolean isPathEquals(JCTree tree, String[] path, int i) {
        if (i >= path.length) return false;

        var forCompare = path[path.length - i - 1];

        if (tree instanceof JCTree.JCFieldAccess field)
            return field.name.toString().equals(forCompare) &&
                isPathEquals(field.selected, path, i + 1);

        if (tree instanceof JCTree.JCIdent ident)
            return ident.name.toString().equals(forCompare) &&
                i == path.length - 1;

        throw new RuntimeException("unreachable");
    }

    boolean hasImport(JCTree.JCCompilationUnit cu, boolean isStatic, String[] path) {
        return cu.getImports().stream().anyMatch(imp ->
            imp.isStatic() == isStatic &&
                isPathEquals(imp.getQualifiedIdentifier(), path, 0));
    }

    boolean isRenderCall(JCTree.JCCompilationUnit cu,
                         JCTree.JCClassDecl classDecl,
                         JCTree.JCMethodInvocation invocation) {

        if (invocation.meth instanceof JCTree.JCIdent ident)
            return ident.getName().toString().equals("render") &&
                hasImport(cu, true, new String[]{"com", "over64", "greact", "GReact", "render"}) &&
                classDecl.getMembers().stream().noneMatch(member ->
                    member instanceof JCTree.JCMethodDecl m &&
                        m.getName().toString().equals("render"));

        else if (invocation.meth instanceof JCTree.JCFieldAccess field) {
            if (isPathEquals(field, new String[]{"GReact", "render"}, 0))
                return hasImport(cu, false, new String[]{"com", "over64", "greact", "GReact"});

            return isPathEquals(field, new String[]{"com", "over64", "greact", "GReact", "render"}, 0);
        }

        throw new RuntimeException("unreachable");
    }

    RuntimeException failShowUsage() {
        return new RuntimeException("""
            expected to call GReact render function as:
              render(dom, template, component_class...);
            for example:
              render(dom, "<H1>hello</H1>", H1.class);""");
    }

    Name importPathEntryName(JCTree entry) {
        if (entry instanceof JCTree.JCFieldAccess field) return field.name;
        if (entry instanceof JCTree.JCIdent ident) return ident.name;
        throw new RuntimeException("unreachable");
    }

    Pair<String, List<Symbol.ClassSymbol>> getRenderCallArgs(
        JCTree.JCCompilationUnit cu, Symtab symtab, Names names,
        JCTree.JCMethodInvocation invocation) {

        if (invocation.getArguments().length() < 2) throw failShowUsage();
        var templateArg = invocation.getArguments().get(1);

        final String template;
        if (templateArg instanceof JCTree.JCLiteral lit)
            if (lit.getKind() == Tree.Kind.STRING_LITERAL)
                template = (String) lit.value;
            else throw failShowUsage();
        else throw failShowUsage();

        var componentClasses =
            invocation.getArguments().stream().skip(2).map(expr -> {
                if (expr instanceof JCTree.JCFieldAccess field)
                    if (field.name.toString().equals("class"))
                        if (field.selected instanceof JCTree.JCIdent ident) {
                            var className = cu.getImports().stream()
                                .filter(imp -> importPathEntryName(imp.qualid)
                                    .equals(ident.name))
                                .map(imp -> names.fromString(imp.qualid.toString()))
                                .findAny()
                                .orElseGet(() -> names.fromString(cu.getPackageName().toString() + "." + ident.name));

                            return symtab.enterClass(symtab.unnamedModule, className);

                            // FIXME: must I check?
//                            if (entered.type.tsym.kind == Kinds.Kind.ERR)
//                                throw new RuntimeException("Cannot find class for name: " + className);
//                            return entered;
                        }

                throw failShowUsage();
            }).collect(Collectors.toList());

        return new Pair<>(template, componentClasses);
    }

    void jsxToJava(Pair<String, List<Symbol.ClassSymbol>> args) {

    }

    @Override
    public void init(JavacTask task, String... strings) {
        var context = ((BasicJavacTask) task).getContext();

        task.addTaskListener(new TaskListener() {

            @Override
            public void finished(TaskEvent e) {
                // THE PLAN
                //   1. Ищем точку входа для модификации
                //     1.1 render
                //       1.1.1 ищем import static over64.greact.GReact.render
                //       1.1.2 проверяем, что нет локального метода render
                //     1.2 Greact.render
                //     1.2.2 ищем import over64.greact.GReact
                //     1.3 com.over64.greact.GReact.render (profit)
                //   2. Достаем параметры вызова render
                //     2.1 строковый литерал с шаблоном (param1)
                //     2.2 varargs список классов, делаем enterClass
                //   3. JSX
                //     3.1 получаем ast
                //     3.2 jsx ast -> java ast
                //       3.2.1. парсим Template используя java parser
                //       3.2.2. сопоставляем аттрибуты тегов аргументам конструктора
                //       3.2.3. генерируем new, учитывая позиции аттрибутов
                //     3.3 полученный код складываем в локальную лямбду
                //     3.4 подменяем render на вызов лямбды
                //   5. делаем debug out модифицированного CU в файл
                //     5.1 в тесте проверяем, что модифицированный файл верен
                //   5. Подключаем плагин в sample проект
                //   6. Тестируем Demo

                var input = "(e) -> e";
                var parser = ParserFactory.instance(context)
                    .newParser(input, false, true, true);
                var expr = parser.parseExpression();
                var consumed = parser.getEndPos(expr);
                if (consumed != input.length())
                    throw new RuntimeException("jsx template parse error: " + input + " at " + consumed);

                var env = JavacProcessingEnvironment.instance(context);
                var symTab = Symtab.instance(context);
                var names = Names.instance(context);

                if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();
                    var x = 1;

                    for (var typeDecl : cu.getTypeDecls()) {
                        typeDecl.accept(new TreeScanner() {
//                            @Override
//                            public void visitApply(JCTree.JCMethodInvocation that) {
//                                var y = 1;
//                                if (isRenderCall(cu, (JCTree.JCClassDecl) typeDecl, that)) {
//                                    var args = getRenderCallArgs(cu, symTab, names, that);
//
//
//                                    var x = 1;
//                                }
//
////                                symTab.getAllClasses().forEach(c -> {
////                                    var x = 1;
////                                });
//
//                                // Trees.instance(env).getTree(symTab.enterClass(symTab.unnamedModule, Names.instance(context).fromString("js.H1"))).defs
//                                // var x = 1;
//                                super.visitApply(that);
//                            }

                            @Override public void visitIdent(JCTree.JCIdent tree) {

                                super.visitIdent(tree);
                            }
                        });

                    }
                    var maker = TreeMaker.instance(context);
                }
            }
        });
    }
}