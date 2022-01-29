package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiledMany;

public class _07StaticImport {
    @Test void importStaticClassField() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A {
                      static String s = "";
                    }""",
                """
                    class js_A extends Object {
                      constructor() {
                        super();
                      }
                      static s = '';
                    }
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    import static js.A.s;
                    class B {
                      String ss = s + s;
                    }""",
                """
                    class js_B extends Object {
                      constructor() {
                        const __init__ = () => {
                          this.ss = js_A.s + js_A.s;
                        };
                        super();
                        __init__();
                      }
                    }
                    """));
    }

    @Test void callFullQualifiedStatic() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A {
                      String foo() { return "xxx"; }
                    }""",
                """
                    class js_A extends Object {
                      constructor() {
                        super();
                      }
                      _foo() {
                        return 'xxx';
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.C",
                """
                    package js;
                    class C {
                      String ss = js.B.a.foo();
                    }""",
                """
                    class js_C extends Object {
                      constructor() {
                        const __init__ = () => {
                          this.ss = js_B.a._foo();
                        };
                        super();
                        __init__();
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    class B {
                      static A a = new A();
                    }""",
                """
                    class js_B extends Object {
                      constructor() {
                        super();
                      }
                      static a = new js_A();
                    }
                    """));
    }
}
