package com.greact;

import java.util.List;
import java.util.Optional;

public class ShimLibrary {

    static List<String> loadUser(java.util.Optional<Integer> x) {
        return null;
    }

    interface Function<T, R, E extends Exception> {
        R apply(T value) throws E;
    }

    static class Option<T> {
        T value;
        final java.util.Optional<T> asJava;

        static <T> Option<T> of(T value) {
            return new Option<>(value);
        }

        Option(T value) {
            this.value = value;
            this.asJava = (java.util.Optional<T>) (Object) this;
        }

        <U, E extends Exception> Option<U> map(Function<T, U, E> mapper) throws E {
            return new Option<>(mapper.apply(this.value));
        }
    }

    static class Array<T> {

    }

    <T> Option<T> rpc(java.util.Optional<T> f) {
        return null;
    }

    static Optional<String> javaOption() {
        return null;
    }

    <T> Array<T> rpc(java.util.List<T> f) {
        return null;
    }

    // E leaks in this case
    // not leaks if lambda
    <T> T rpc(T f) {
        return null;
    }


    final String searchPackage;
    public final String libraryPackage;

    //    <T> Optional<T> asJs(java.util.Optional<T> j) {
//        return (Optional<T>) ((Object) j);
//    }

    interface RpcFunction1<T1, R, E extends Exception> {
        R call(T1 x) throws E;
    }

    <T1, R, E extends Exception> R remote(RpcFunction1<T1, R, E> f) {
        return null;
    }

    @interface async {
    }

    @interface await {
    }

    @interface rpc {
    }

    interface F<V, E extends Exception> {
        V supply() throws E;
    }


    <V, E extends Exception> PromiseE<V, E> promise(F<V, E> f) {
        return null;
    }
    static class PromiseE<V, E extends Exception> {
        @async V await() throws E {
            return null;
        }
    }

    class G extends Exception {

    }


    @async public ShimLibrary(String searchPackage, String libraryPackage) throws G {
        var yy = promise(() -> ShimLibrary.loadUser(Option.of(1).asJava));
        var cc = ShimLibrary.loadUser(Option.of(1).asJava);
        var opt = rpc(ShimLibrary.javaOption());

        var zz = yy.await();

        var ff = promise(() -> {
            if (true) throw new G();
            return 42;
        }).await();


//        remote(ShimLibrary::loadUser)
//            .apply(Option.of(1).asJava);
//        RpcFunction1<java.util.Optional<String>, List<String>, RuntimeException> name = ShimLibrary::loadUser;
//
//        var yy = rpc(ShimLibrary.loadUser(Option.of(1).asJava));
//        var ll = rpc(new ArrayList<>());
//        var oo = rpc(java.util.Optional.of("12"));
//        var xx = rpc("123");

//        var z = new Optional<>(1)
//            .map(x -> x + 1);
//
//        var zz = new Optional<>(true)
//            .map(x -> {
//                if(!x) throw new Exception("oops");
//                return !x;
//            });


        this.searchPackage = searchPackage;
        this.libraryPackage = libraryPackage;
    }

    String trimPrefix(String packageName) {
        return packageName.substring(libraryPackage.length());
    }

    public Class resolve(String packageName, String className) throws ClassNotFoundException {
        return Class.forName(searchPackage + trimPrefix(packageName) + "." + className);
    }
}
