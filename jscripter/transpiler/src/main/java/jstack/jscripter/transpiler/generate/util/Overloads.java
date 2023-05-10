package jstack.jscripter.transpiler.generate.util;

import jstack.jscripter.transpiler.model.Static;
import jstack.jscripter.transpiler.model.Async;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Overloads {
    static class Entry {
        int n = -1;
        Entry overrides = null;
        ExecutableElement method;

        Entry(ExecutableElement method) {
            this.method = method;
        }
    }

    record Section(TypeElement klass, List<Entry> entries) { }

    static void pushSupertype(List<Section> sections, TypeElement klass,
                              Name methodName, boolean withInterfaces) {
        var entries = klass.getEnclosedElements().stream()
            .filter(el -> el instanceof ExecutableElement
                && el.getSimpleName().equals(methodName))
            .map(el -> new Entry((ExecutableElement) el))
            .collect(Collectors.toList());

        sections.add(new Section(klass, entries));

        if (withInterfaces)
            klass.getInterfaces().forEach(iface ->
                pushSupertype(sections, (TypeElement) ((Type) iface).asElement(),
                    methodName, withInterfaces));

        var superType = ((Type) klass.getSuperclass()).tsym;
        if (superType != null)
            pushSupertype(sections, (TypeElement) superType.type.asElement(),
                methodName, withInterfaces);
    }

    static void linkOverrides(Types types, List<Section> sections, int i) {
        var from = sections.get(i);
        for (var j = i + 1; j < sections.size(); j++) {
            var to = sections.get(j);

            for (var entryFrom : from.entries)
                for (var entryTo : to.entries)
                    if (((Symbol) entryFrom.method)
                        .overrides((Symbol) entryTo.method,
                            (Symbol.TypeSymbol) to.klass, types, true)) {
                        entryFrom.overrides = entryTo;
                    }
        }

        if (i < sections.size() - 1) linkOverrides(types, sections, i + 1);
    }

    static void enumerate(List<Section> sections) {
        var n = 0;
        for (var i = sections.size() - 1; i >= 0; i--) {
            var section = sections.get(i);
            for (var entry : section.entries)
                if (entry.overrides == null) entry.n = n++;
                else entry.n = entry.overrides.n;
        }
    }

    static List<Section> buildSections(Types types, TypeElement klass, Name methodName) {
        var sections = new ArrayList<Section>();
        // Что будем делать с интерфейсами и default методами?
        pushSupertype(sections, klass, methodName, true /* fixme ? */);
        linkOverrides(types, sections, 0);
        enumerate(sections);
        return sections;
    }

    static boolean isOverloaded(List<Section> sections) {
        return sections.stream()
            .anyMatch(section -> section.entries.size() > 1);
    }

    static Pair<Boolean, Boolean> isAsync(TypeElement klass, Name methodName) {
        var sections = new ArrayList<Section>();
        pushSupertype(sections, klass, methodName, true);

        Function<Stream<Section>, Boolean> isAsyncPartial = part ->
            part.anyMatch(section ->
                section.entries.stream()
                    .anyMatch(entry -> entry.method.getAnnotation(Async.class) != null));

        return new Pair<>(
            isAsyncPartial.apply(sections.stream().skip(1)), // in super
            isAsyncPartial.apply(sections.stream().limit(1))); // local
    }


    public record OverloadTable(
        boolean isOverloaded,
        boolean hasInSuper,
        boolean isAsyncInSuper,
        boolean isAsyncLocal,
        List<Pair<Integer, ExecutableElement>> staticMethods,
        List<Pair<Integer, ExecutableElement>> methods) {
    }

    public static OverloadTable table(Types types, TypeElement klass, Name methodName) {
        var sections = buildSections(types, klass, methodName);

        var hasInSuper = sections.stream().skip(1).anyMatch(section -> !section.entries.isEmpty());
        var partitioned = sections.get(0).entries.stream()
            .map(entry -> new Pair<>(entry.n, entry.method))
            .collect(Collectors.partitioningBy(p -> p.snd.getModifiers().contains(Modifier.STATIC)));
        var isAsyncTotally = isAsync(klass, methodName);

        return new OverloadTable(isOverloaded(sections), hasInSuper, isAsyncTotally.fst, isAsyncTotally.snd,
            partitioned.get(true), partitioned.get(false));
    }

    public enum Mode {INSTANCE, STATIC, AS_STATIC}
    public record Info(int n, boolean isOverloaded, Mode mode, boolean isAsync) {
    }

    public static Info methodInfo(Types types, TypeElement klass, ExecutableElement method) {

        var sections = buildSections(types, klass, method.getSimpleName());

        var found = sections.get(0).entries.stream()
            .filter(entry -> entry.method == method)
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException("unreachable: " + klass + ": " + method));

        var mode = found.method.getModifiers().contains(Modifier.STATIC) ? Mode.STATIC
            : found.method.getAnnotation(Static.class) != null ? Mode.AS_STATIC
            : Mode.INSTANCE;

        var isAsyncTotally = isAsync(klass, method.getSimpleName());

        return new Info(found.n, isOverloaded(sections), mode,
            isAsyncTotally.fst || isAsyncTotally.snd);
    }
}
