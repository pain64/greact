package com.greact.generate;

import com.sun.source.tree.CompilationUnitTree;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.io.Writer;

public class JSGen {
    final Writer out;
    final CompilationUnitTree cu;

    public JSGen(Writer out, CompilationUnitTree cu) {
        this.out = out;
        this.cu = cu;
    }

    void genField(VariableElement fieldEl) throws IOException {
        out.write("  field " + fieldEl.getSimpleName() + "\n");
    }

    void genMethod(ExecutableElement methodEl) throws IOException {
        out.write("  method " + methodEl.getSimpleName() + "\n");
    }
    public void genType(TypeElement typeEl) throws IOException {

        if (typeEl.getKind() == ElementKind.INTERFACE)
            return;

        out.write("class ");
        out.write(cu.getPackage().getPackageName().toString().replace(".", "$"));
        out.write("$");
        out.write(typeEl.getSimpleName().toString());
        out.write(" {\n");

        for(var declEl: typeEl.getEnclosedElements()) {
            switch (declEl.getKind()) {
                case FIELD -> genField((VariableElement) declEl);
                case METHOD -> genMethod((ExecutableElement) declEl);
            }
        }

        out.write("}");
    }
}
