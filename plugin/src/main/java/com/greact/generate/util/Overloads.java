package com.greact.generate.util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Iterator;

public class Overloads {
    public static record Info(int n, boolean isOverloaded, boolean isStatic) {
    }

    public static Info nextMethod(Iterator<ExecutableElement> methods,
                                  ExecutableElement findFor, int i) {
        if (!methods.hasNext()) throw new RuntimeException("unreachable");

        var method = methods.next();

        if (findFor == method) {
            var isOverloaded = i != 0 || methods.hasNext();
            return new Info(i, isOverloaded,
                method.getModifiers().contains(Modifier.STATIC));
        }

        return nextMethod(methods, findFor, i + 1);
    }

    public static Info findMethod(TypeElement typeEl, ExecutableElement findFor) {
        var group = typeEl.getEnclosedElements().stream()
            .filter(m -> m instanceof ExecutableElement &&
                m.getSimpleName().equals(findFor.getSimpleName()))
            .map(m -> ((ExecutableElement) m));

        return nextMethod(group.iterator(), findFor, 0);
    }
}
