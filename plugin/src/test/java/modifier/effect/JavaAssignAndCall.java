package modifier.effect;

import org.junit.jupiter.api.Test;

public class JavaAssignAndCall {
    Runnable xxx;

    @Test void testIt() {
        (xxx = () -> {
            System.out.println("hello, world!");
        }).run();

        xxx.run();
    }
}
