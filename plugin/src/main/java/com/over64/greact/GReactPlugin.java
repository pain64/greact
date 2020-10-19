package com.over64.greact;

import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.*;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;
import com.sun.tools.javac.parser.JavacParser;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.stream.Collectors;


public class GReactPlugin implements Plugin {

    public static final String NAME = "GReact";

    @Override
    public String getName() {
        return NAME;
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

                if (e.getKind() == TaskEvent.Kind.ENTER) {
                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();

                    for (var typeDecl : cu.getTypeDecls()) {
                        typeDecl.accept(new TreeScanner() {
                            @Override
                            public void visitApply(JCTree.JCMethodInvocation that) {
                                symTab.getAllClasses().forEach(c -> {
                                    var x = 1;
                                });
                                // Trees.instance(env).getTree(symTab.enterClass(symTab.unnamedModule, Names.instance(context).fromString("js.H1"))).defs
                                var x = 1;
                                super.visitApply(that);
                            }
                        });

                    }
                    var maker = TreeMaker.instance(context);
                    var names = Names.instance(context);
                }
            }
        });
    }
}