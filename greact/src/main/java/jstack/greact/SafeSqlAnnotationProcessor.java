package jstack.greact;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.processing.JavacRoundEnvironment;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({"jstack.ssql.AutoDto"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class SafeSqlAnnotationProcessor extends AbstractProcessor {

    @Override public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {

        try {
            var processingEnvField = JavacRoundEnvironment.class.getDeclaredField("processingEnv");
            processingEnvField.setAccessible(true);
            var processingEnv = (JavacProcessingEnvironment) processingEnvField.get(roundEnv);
            var context = processingEnv.getContext();

            var classAndKey = GReactPlugin.cachedClass();
            var instance = context.get(classAndKey.snd);
            var method = instance.getClass().getMethod(
                "doAnnotationProcessing", Set.class, RoundEnvironment.class
            );
            return (boolean) method.invoke(instance, set, roundEnv);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
