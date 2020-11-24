package com.greact.generate.util;

import com.greact.model.Static;
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
import java.util.stream.Collectors;

public class Overloads {
    static class Entry {
        int n = -1;
        Entry overrides = null;
        ExecutableElement method;

        Entry(ExecutableElement method) {
            this.method = method;
        }
    }

    static record Section(TypeElement klass,
                          List<Entry> entries) {
    }

    static void pushSupertype(List<Section> sections, TypeElement klass, Name methodName) {
        var entries = klass.getEnclosedElements().stream()
            .filter(el -> el instanceof ExecutableElement
                && el.getSimpleName().equals(methodName))
            .map(el -> new Entry((ExecutableElement) el))
            .collect(Collectors.toList());

        sections.add(new Section(klass, entries));

        var superType = ((Type) klass.getSuperclass()).tsym;
        if (superType != null)
            pushSupertype(sections, (TypeElement) superType.type.asElement(), methodName);
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
        pushSupertype(sections, klass, methodName);
        linkOverrides(types, sections, 0);
        enumerate(sections);
        return sections;
    }


    public static record OverloadTable(
        boolean isOverloaded,
        boolean hasInSuper,
        List<Pair<Integer, ExecutableElement>> staticMethods,
        List<Pair<Integer, ExecutableElement>> methods) {
    }

    public static OverloadTable table(Types types, TypeElement klass, Name methodName) {
        var sections = buildSections(types, klass, methodName);

        var isOverloaded = sections.stream()
            .anyMatch(section -> section.entries.size() > 1);
        var hasInSuper = sections.stream().skip(1).anyMatch(section -> !section.entries.isEmpty());
        var partitioned = sections.get(0).entries.stream()
            .map(entry -> new Pair<>(entry.n, entry.method))
            .collect(Collectors.partitioningBy(p -> p.snd.getModifiers().contains(Modifier.STATIC)));

        return new OverloadTable(isOverloaded, hasInSuper,
            partitioned.get(true), partitioned.get(false));
    }


    public enum Mode {INSTANCE, STATIC, AS_STATIC}
    public static record Info(int n, boolean isOverloaded, Mode mode) {
    }

    public static Info methodInfo(Types types, TypeElement klass, ExecutableElement method) {

        var sections = buildSections(types, klass, method.getSimpleName());

        var isOverloaded = sections.stream()
            .anyMatch(section -> section.entries.size() > 1);

        var found = sections.get(0).entries.stream()
            .filter(entry -> entry.method == method)
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException("unreachable"));

        var mode = found.method.getModifiers().contains(Modifier.STATIC) ? Mode.STATIC
            : found.method.getAnnotation(Static.class) != null ? Mode.AS_STATIC
            : Mode.INSTANCE;

        return new Info(found.n, isOverloaded, mode);
    }
}