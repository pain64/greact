package com.greact.generate;

import com.sun.source.tree.CompilationUnitTree;

import javax.lang.model.element.*;
import java.io.IOException;
import java.io.Writer;

public class JSGen {
    final Writer out;
    final CompilationUnitTree cu;

    void write(int sp, String text) {
        try {
            for (int i = 0; i < sp; i++)
                out.write(' ');
            out.write(text);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public JSGen(Writer out, CompilationUnitTree cu) {
        this.out = out;
        this.cu = cu;
    }

    void genField(int deep, VariableElement fieldEl) throws IOException {
        write(deep, fieldEl.getSimpleName() + "" + "\n");
    }

    void genMethod(int deep, ExecutableElement methodEl) throws IOException {
        write(deep, methodEl.getModifiers().contains(Modifier.STATIC) ?
            "static " : "");

        write(0, methodEl.getSimpleName() + "(");

        var isFirst = true;
        for (var param : methodEl.getParameters()) {
            write(0, param.getSimpleName().toString());

            if (isFirst) {
                write(0, ", ");
                isFirst = false;
            }

        }
        write(0, ") {\n");

        write(deep, "}\n");
    }

    public void genType(int deep, TypeElement typeEl) throws IOException {

        if (typeEl.getKind() == ElementKind.INTERFACE)
            return;

        write(deep, "class ");
        write(deep, cu.getPackage().getPackageName().toString().replace(".", "$"));
        write(0, "$");
        write(0, typeEl.getSimpleName().toString());

        write(deep, " {\n");

        var firstMethod = true;
        for (var declEl : typeEl.getEnclosedElements()) {
            if (declEl.getKind() == ElementKind.METHOD) {
                genMethod(deep + 2, (ExecutableElement) declEl);

                if (firstMethod) {
                    write(deep, "\n");
                    firstMethod = false;
                }
            }
        }

        write(deep, "}");
    }
}
