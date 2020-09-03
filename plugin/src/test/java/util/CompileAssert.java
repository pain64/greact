package util;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;

public class CompileAssert {
    public static void assertCompiled(String javaSrc, String jsDest) throws IOException {
        Assertions.assertEquals(jsDest,
            TestCompiler.compile("js.Test", javaSrc).values().iterator().next().getCharContent(true));
    }

    public static class CompileCase {
        final String fullQualified;
        final String javaSrc;
        final String jsDest;

        public CompileCase(String fullQualified, String javaSrc, String jsDest) {
            this.fullQualified = fullQualified;
            this.javaSrc = javaSrc;
            this.jsDest = jsDest;
        }
    }

    public static void assertCompiledMany(CompileCase... tests) throws IOException {
        for (var test : tests) {
            Assertions.assertEquals(test.jsDest,
                TestCompiler.compile(test.fullQualified, test.javaSrc)
                    .get(test.fullQualified).getCharContent(true));
        }
    }
}
