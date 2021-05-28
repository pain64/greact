package com.greact.model;

public interface ClassRef {
    @interface Reflexive {}

    interface FieldRef {
        String name();
        ClassRef __class__();
    }

    String name();
    FieldRef[] fields();

    static ClassRef of(@SuppressWarnings("unused") Object obj) {
        throw new RuntimeException("must be implemented on compile time");
    }
}
