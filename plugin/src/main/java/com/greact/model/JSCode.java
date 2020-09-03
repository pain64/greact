package com.greact.model;

public @interface JSCode {
    String code();

    boolean isStatic() default false;
}
