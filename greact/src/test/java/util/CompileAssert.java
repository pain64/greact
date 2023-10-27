package util;

import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompileAssert {
    public static void assertCompiled(String javaSrc, String jsDest) throws IOException {
        Assertions.assertEquals(jsDest,
            TestCompiler.compile(List.of(new StringSourceFile("js.Test", javaSrc)))
                .values().iterator().next().getCharContent(true));
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

        compilationUnits.add(
            new StringSourceFile(
                "util.TestServer",
                Files.readString(new File("src/test/java/util/TestServer.java").toPath().toAbsolutePath())
            )
        );
        var res = TestCompiler.compile(compilationUnits);

        for (var t : tests) {
            var jsOut = res.get(t.fullQualified + ".java.patch");
            Assertions.assertEquals(t.jsDest, jsOut.getCharContent(true));
        }
    }
}
