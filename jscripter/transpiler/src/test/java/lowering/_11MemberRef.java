package lowering;

import com.greact.generate.util.CompileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _11MemberRef {

    @Test void memberRefForRecord() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public record A(long age){}
                    """,
                """
                    class js$A extends Object {
                      constructor(age) {
                        let __init__ = () => {
                          this.age = 0
                        };
                        super();
                        __init__();
                        this.age = age;
                      }
                    }"""),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    import com.greact.model.MemberRef;
                    class B {
                      MemberRef<A, Long> ref = A::age;
                    }""",
                """
                    class js$B extends Object {
                      constructor() {
                        let __init__ = () => {
                          this.ref = {memberName: () => 'age', value: (v) => v.age}
                        };
                        super();
                        __init__();
                      }
                    }"""));
    }

    @Test void memberRefForClassIncorrect() {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.MemberRef;
                    class Test {
                      static class B { public static long age(B b) { return 0; } };
                      MemberRef<B, Long> ref = B::age;
                    }
                    """,
                "");
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(ce.error, CompileException.ERROR.MEMBER_REF_USED_INCORRECT);
        }
    }

    @Test void memberRefForClass() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A { long age; }
                    """,
                """
                    class js$A extends Object {
                      constructor() {
                        let __init__ = () => {
                          this.age = 0
                        };
                        super();
                        __init__();
                      }
                    }"""),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    import com.greact.model.MemberRef;
                    class B {
                      MemberRef<A, Long> ref = a -> a.age;
                    }""",
                """
                    class js$B extends Object {
                      constructor() {
                        let __init__ = () => {
                          this.ref = {memberName: () => 'age', value: (v) => v.age}
                        };
                        super();
                        __init__();
                      }
                    }"""));
    }

    @Test void memberRefForRecordIncorrect() {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.MemberRef;
                    class Test {
                      record B(long age) {};
                      MemberRef<B, Long> ref = b -> b.age;
                    }
                    """,
                "");
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(ce.error, CompileException.ERROR.MEMBER_REF_USED_INCORRECT);
        }
    }
}
