package jstack.jscripter.transpiler.model;

public interface Require {
    @interface CSS {
        String[] value();
    }

    @interface JS {
        String[] value();
    }
}