package com.greact.model;

/**
 * Special!
 *
 * @param <T> dangling, but will be checked by JScripter
 * @param <V> type of field value
 */
public interface MemberRef<T, V> {
    default String[] memberNames() {return new String[]{"must be autogenerated"};}
    default FieldRef[] path() {
        throw new RuntimeException("must be generated by compiler");
    }

    class FieldRef {
        String name;
        String className;
    }

    V value(T t);
}