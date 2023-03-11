package jstack.ssql.dialect;

import jstack.ssql.RW;

import java.lang.annotation.Repeatable;

@Repeatable(Bindings.class)
public @interface Bind {
    Class<?> klass();
    String sqlType();
    Class<? extends RW> using();
}
