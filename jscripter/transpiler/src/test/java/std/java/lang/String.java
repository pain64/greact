package std.java.lang;

import com.greact.model.JSExpression;
import com.greact.model.Static;

public class String {
    @Static int compareTo(java.lang.String anotherString) {
        return JSExpression.of(
            "this > anotherString ? 1 : this < anotherString ? -1 : 0");
    }
}
