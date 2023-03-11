package jstack.ssql.dialect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) public @interface Bindings {
    Bind[] value();
}
