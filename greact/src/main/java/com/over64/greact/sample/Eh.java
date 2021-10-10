package com.over64.greact.sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class Eh {

    @FunctionalInterface
    interface WhichThrows1<A1, R, E extends Exception> {
        R apply(A1 a1) throws E;
    }

    @FunctionalInterface
    interface WhichThrows2<A1, A2, R, E extends Exception> {
        R apply(A1 a1, A2 a2) throws E;
    }

    @FunctionalInterface
    interface Hidden<R, E extends Exception> {
        R hide() throws E;
    }

    static class HiddenException extends RuntimeException {
        public HiddenException(Exception parent) {
            super(parent);
        }
    }

    static <R, E extends Exception> R applied(Hidden<R, E> f) {
        try {
            return f.hide();
        } catch (Exception ex) {
            throw new HiddenException(ex);
        }
    }

    static <A1, R, E extends Exception> R hide(WhichThrows1<A1, R, E> f, A1 a1) {
        return applied(() -> f.apply(a1));
    }

    static <A1, A2, R, E extends Exception> R hide(WhichThrows2<A1, A2, R, E> f, A1 a1, A2 a2) {
        return applied(() -> f.apply(a1, a2));
    }

    void printUnhidden(Exception ex) {

    }

    public static class ExInt extends Exception {
        public final int value;

        public ExInt(int value) {
            this.value = value;
        }
    }

    public static class ExString extends Exception {
        public final String value;

        public ExString(String value) {
            this.value = value;
        }

        public ExString(String value, Exception cause) {
            this.value = value;
        }
    }


    public static class ExInvalidY extends ExInt {
        public ExInvalidY(int y) { super(y); }
    }

    public static class ExNoUserWithId extends ExInt {
        public ExNoUserWithId(int id) { super(id);}
    }

    public static class ExNoUserWithName extends ExString {
        public ExNoUserWithName(String name) { super(name);}
    }

    int mulIf(int x, int y) throws ExInvalidY {
        if (y < 100) throw new ExInvalidY(y);
        return x * y;
    }

    static <R, E1 extends Exception, E2 extends Exception> R mapErr(
        Hidden<R, E1> f, Function<E1, E2> conv) throws E2 {
        return null;
    }

    public static class ExDbWrongLine extends ExString {
        public ExDbWrongLine(String line, Exception cause) { super(line, cause);}
    }

    static class Option<T> {
        static None<?> theNone = new None<>();

        static <T> Option<T> none() { return (Option<T>) theNone; }

        static <T> Option<T> of(T value) {
            if (value == null) return none();
            return new Some<>(value);
        }
    }

    static class None<T> extends Option<T> {
    }

    static class Some<T> extends Option<T> {
        public final T val;

        Some(T val) {this.val = val;}
    }

    record User(int id, String name) {}


    interface UserResolver {
        Option<User> resolve(List<Path> files, int id);
    }

    @interface Ex {
        Class<? extends Exception>[] value();
    }

    static class DefaultResolver implements UserResolver {
        User parseLine(String line) throws ExDbWrongLine {
            var parsed = line.split(" => ");
            if (parsed.length != 2)
                throw new ExDbWrongLine(line, new Exception("expected format: id => name"));

            var id = Eh.mapErr(() -> Integer.parseInt(parsed[0]),
                pe -> new ExDbWrongLine(line, pe));

            if (parsed[1].isBlank())
                throw new ExDbWrongLine(line, new Exception("user name must not be blank"));

            return new User(id, parsed[1]);
        }

        Option<User> findUser(int id, Path file) throws IOException, ExDbWrongLine {
            for (var line : Files.readString(file).split("\n")) {
                var idAndName = parseLine(line);
                if (idAndName.id == id)
                    return Option.of(idAndName);
            }

            return Option.none();
        }

        Option<User> findUser(int id, List<Path> files) throws IOException, ExDbWrongLine {
            for (var file : files)
                if (findUser(id, file) instanceof Some<User> some) return some;
            return Option.none();
        }

        @Override
        @Ex({IOException.class, ExDbWrongLine.class})
        public Option<User> resolve(List<Path> files, int id) {
            return hide(this::findUser, id, files);
        }
    }

    static <R, E extends Exception> R thrown(E ex) throws E {
        throw ex;
    }

    User resolveFn(UserResolver resolver, List<Path> files, int id) throws ExNoUserWithId {
        var u1 = resolver.resolve(files, id) instanceof Some<User> __
            ? __.val : thrown(new ExNoUserWithId(id));

        return resolver.resolve(files, id) instanceof Some<User> __
            ? __.val : thrown(new ExNoUserWithId(id));
    }


    void main() throws IOException, ExDbWrongLine, ExNoUserWithId {
        var user = resolveFn(new DefaultResolver(), List.of(), 42);
        System.out.println(user.name);
    }
}
