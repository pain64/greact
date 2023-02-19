package jstack.ssql.schema;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Fury {
    boolean keep() default false;
}
