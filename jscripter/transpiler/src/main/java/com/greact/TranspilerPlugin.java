package com.greact;

import com.greact.generate.TypeGen;
import com.greact.generate.util.JSOut;
import com.greact.generate.util.JavaStdShim;
import com.sun.source.util.*;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import org.apache.commons.cli.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;


public class TranspilerPlugin implements Plugin {

    public static final String NAME = "jScripter";
    static final String[] DEFAULT_STD_CONVERSION_CLASS =
        {"org", "over64", "jscripter", "StdTypeConversion"};

    String jsCodePackage = null;
    String[] stdConversionClass = null;

    @Override
    public String getName() {
        return NAME;
    }


    JCTree.JCFieldAccess rec(TreeMaker maker, Names names, String[] nodes, int i) {
        var prev = i == 1
            ? maker.Ident(names.fromString(nodes[i - 1]))
            : rec(maker, names, nodes, i - 1);

        return maker.Select(prev, names.fromString(nodes[i]));
    }

    JCTree.JCImport buildImport(TreeMaker maker, Names names, String[] paths) {
        return maker.Import(rec(maker, names, paths, paths.length - 1), false);
    }

    public Symbol.ClassSymbol lookupClass(String className, Context context) {
        return Symtab.instance(context).enterClass(
            Symtab.instance(context).unnamedModule,
            Names.instance(context).fromString(className));
    }

    @Override
    public void init(JavacTask task, String... args) {
        var options = new Options()
            .addOption(new Option(null, "js-src-package", true, "java to javascript source package") {{
                setRequired(true);
            }})
            .addOption(new Option(null, "std-conv-class", true, "java standard library type conversion"));

        try {
            var cmd = new DefaultParser().parse(options, args);
            jsCodePackage = cmd.getOptionValue("js-src-package");
            stdConversionClass = Optional.ofNullable(cmd.getOptionValue("std-conv-class"))
                .map(opt -> opt.split("\\."))
                .orElse(DEFAULT_STD_CONVERSION_CLASS);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("jScripter plugin", options);

            System.exit(1);
        }

        var context = ((BasicJavacTask) task).getContext();
        var symTab = Symtab.instance(context);
        var env = JavacProcessingEnvironment.instance(context);


        task.addTaskListener(new TaskListener() {
            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {

                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();
                    var trees = Trees.instance(env);


                    var types = Types.instance(context);
                    // FIXME: NPE
                    var pkg = cu.getPackage().getPackageName().toString();
                    if (!pkg.startsWith(jsCodePackage)) return;

                    var shimConversions =
                            lookupClass(String.join(".", stdConversionClass), context).getEnclosedElements().stream()
                            .filter(el -> el instanceof ExecutableElement && el.getKind() != ElementKind.CONSTRUCTOR)
                            .map(el -> (Symbol.MethodSymbol) el)
                            .collect(Collectors.toMap(
                                m -> ((Symbol.ClassSymbol) m.getParameters().get(0).type.tsym).fullname,
                                Symbol.MethodSymbol::getReturnType));

                    // Types.instance(context).isSameType(intType.tsym.getEnclosedElements().get(1).type, ((Symbol
                    // .MethodSymbol) sym).type)


                    System.out.println("after analyze for: " + e + "cu: " + cu);

                    try {
                        var jsFile = env.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                            cu.getPackageName().toString(),
                            e.getTypeElement().getSimpleName() + ".js");

                        var depFile = env.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                            cu.getPackageName().toString(),
                            e.getTypeElement().getSimpleName() + ".js.dep");

                        try (var writer = jsFile.openWriter();
                             var depWriter = depFile.openWriter()) {

                            for (var typeDecl : cu.getTypeDecls()) {
                                var out = new JSOut(writer);
                                new TypeGen(out, cu, env, context, new JavaStdShim(types,
                                    shimConversions)).type(0, typeDecl);
                                for (var type : out.dependsOn) {
                                    depWriter.write(type);
                                    depWriter.write(10); // \n
                                }
                            }

                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

//            @Override
//            public void finished(TaskEvent e) {
//                if (e.getKind() != TaskEvent.Kind.ANALYZE)
//                    return;
//
//

//                System.out.println("after analyze for: " + e);
//                e.getCompilationUnit().getImports().forEach(i -> {
//                    //ImportTree
//                });

            // System.out.println("finish analyze for: " + e);

//                Element el = null;
//                var jctree = env.getElementUtils().getTree(el);
//                el.getKind()
//                jctree.

//                var cu = e.getCompilationUnit();
//                var docTree = DocTrees.instance(task);
//                var pkgName = Optional.ofNullable(cu.getPackageName())
//                    .map(Objects::toString).orElse("");
//
//                cu.accept(new TreeScanner<Void, Void>() {
//                    @Override
//                    public Void visitClass(ClassTree classNode, Void aVoid) {
//                        var compAnnotation =
//                            classNode.getModifiers().getAnnotations().stream()
//                                .filter(a -> Component.class.getSimpleName()
//                                    .equals(a.getAnnotationType().toString()))
//                                .findFirst();
//
//                        if (compAnnotation.isPresent()) {
//                            System.out.println("instrument for: " +
//                                pkgName + "." + classNode.getSimpleName());
//
//
////                            Log.instance(((BasicJavacTask) task).getContext())
////                                .error(JCDiagnostic.DiagnosticFlag.API, pos,  CompilerProperties.Errors
// .ProcMessager(msg.toString()));
//
//                            env.getMessager().printMessage(Diagnostic.Kind.ERROR, "oops");
//
//                            classNode.getMembers().forEach(member -> {
//                                var doc = docTree.getDocCommentTree(docTree.getPath(cu, member));
//                                if (doc != null)
//                                    System.out.println(doc.toString());
//
//                            });
//                        }
//
//                        return aVoid; // super.visitClass(classNode, aVoid);
//                    }
//                }, null);
//            }
        });
    }
}