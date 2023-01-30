package jstack.ssql.schema;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Join {
    JoinMode mode();
    String table();
    String on();
}
