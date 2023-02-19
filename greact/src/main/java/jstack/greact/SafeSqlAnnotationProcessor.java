package jstack.greact;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.main.JavaCompiler;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes({
    "jstack.ssql.schema.Fury", "jstack.ssql.schema.FuryComplete"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class SafeSqlAnnotationProcessor extends AbstractProcessor {
//    Context context;
//    JavacProcessingEnvironment env;

    @Override public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
//        env = (JavacProcessingEnvironment) processingEnv;
//        context = ((JavacProcessingEnvironment) processingEnv).getContext();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        for (TypeElement annotation : set) {
            Set<? extends Element> annotatedElements
                = roundEnv.getElementsAnnotatedWith(annotation);
            var xxx = (Symbol.ClassSymbol) annotatedElements.iterator().next();

            if (annotation.getQualifiedName().toString().equals("jstack.ssql.schema.Fury")) {
                JavaFileObject builderFile = null;
                try {
                    builderFile = processingEnv.getFiler()
                        .createSourceFile("fury.omsu.tpayload.js.ResourcesPage", xxx);
                    try (var out = new PrintWriter(builderFile.openWriter())) {
                        out.write("""
                            package fury.omsu.tpayload.js;
                            import omsu.tpayload.js.domain.Term;
                            import omsu.tpayload.js.domain.ResourceState;
                            import java.math.BigDecimal;
                            
                            public class ResourcesPage {
                                    public record Resource /* (Term term, ResourceState state) {} */ (
                                         String discipline, String studyForm, String express,
                                         String studyLength, String studyLevel, String disciplineIndex,
                                         String groupCodes, String typeExpress,
                                         BigDecimal contingentQtyOrig, BigDecimal contingentQtyDest,
                                         BigDecimal payloadOrig, BigDecimal payloadDest,
                                         long id, Term term, String course, ResourceState state
                                     ) { }
                            }
                            """);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else { // jstack.ssql.schema.FuryComplete
//                var trees = Trees.instance(env);
//                var typeTree = (JCTree) trees.getTree(xxx);
//                var attr = Attr.instance(context);
//
//                var names = Names.instance(context);
//                var symtab = Symtab.instance(context);
////                var compiler = JavaCompiler.instance(context);
////                // env.getFiler().getGeneratedClasses()
////                var ast = compiler.parse(
////                    env.getFiler().getGeneratedSourceFileObjects().iterator().next()
////                );
////
////                Enter.instance(context).visitTopLevel(ast);
//
//                long t1 = System.nanoTime();
////            var copied = new TreeCopier<>(TreeMaker.instance(context))
////                .copy(typeTree);
//                attr.attribType(typeTree, xxx);
//                long t2 = System.nanoTime();
//                System.out.println("###TIME FOR INFERENCE: " + (t2 - t1) + "ns");
          }
//
//            //roundEnv.
//
//
////            var names = Names.instance(context);
////            var types = Types.instance(context);
////            var symtab = Symtab.instance(context);
//
//
//
//            // attr.attribType((JCTree) trees.getTree(xxx), xxx);
////            var maker = TreeMaker.instance(context);
////
////            Symbol.ClassSymbol clInt = symtab.enterClass(symtab.java_base, names.fromString("java.lang.Integer"));
////
////            var tree = (JCTree.JCClassDecl) trees.getTree(xxx);
////            var field = new Symbol.MethodSymbol(Flags.PUBLIC, names.fromString("foobar"), clInt.type, xxx);
////            tree.defs = tree.defs.append(
////                maker.MethodDef(field, maker.Block(Flags.BLOCK, List.nil()))
////            );
////
////            xxx.members_field.enterIfAbsent(field);
//
//            System.out.println("##### I'm called!!! " + xxx.getClass());

            // …
        }

        return true;
    }
}
