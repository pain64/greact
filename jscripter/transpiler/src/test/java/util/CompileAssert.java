package util;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CompileAssert {
    public static void assertCompiled(String javaSrc, String jsDest) throws IOException {
        assertCompiledMany(new CompileCase("js.Test", javaSrc, jsDest));
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
        var compilationUnits = Arrays.stream(tests)
            .map(t -> new StringSourceFile(t.fullQualified, t.javaSrc))
            .collect(Collectors.toList());


        var res = TestCompiler.compile(compilationUnits);

        for (var t : tests) {
            var jsOut = res.get(t.fullQualified + ".js");
            Assertions.assertEquals(t.jsDest, jsOut.getCharContent(true));
        }
    }
}
