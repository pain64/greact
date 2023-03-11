package jstack.ssql;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

public interface Mapper<C, F, CI, FI> {
    String className(C symbol);
    String fieldName(F field);
    Stream<F> readFields(C symbol);

    @Nullable <A extends Annotation> A classAnnotation(C symbol, Class<A> annotationClass);
    @Nullable <A extends Annotation> A fieldAnnotation(F field, Class<A> annotationClass);

    CI mapClass(C klass);
    FI mapField(F field);
}
