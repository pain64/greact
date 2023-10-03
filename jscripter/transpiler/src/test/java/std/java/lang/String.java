package std.java.lang;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Static;

public class String {
    @Static int compareTo(java.lang.String anotherString) {
        return JSExpression.of(
            "this > :1 ? 1 : this < :1 ? -1 : 0", anotherString);
    }
    @Static public boolean equals(Object other) {
        return JSExpression.of("this == :1", other);
    }
}
