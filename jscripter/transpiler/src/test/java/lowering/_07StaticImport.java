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
                    class js$A extends Object {
                      static s = ''
                      
                      constructor() {
                        super();
                      }
                    }"""),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    import static js.A.s;
                    class B {
                      String ss = s + s;
                    }""",
                """
                    class js$B extends Object {
                      constructor() {
                        super();
                        this.ss = js$A.s + js$A.s
                      }
                    }"""));
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
                    class js$A extends Object {                      
                      constructor() {
                        super();
                      }
                      
                      foo() {
                        return 'xxx'
                      }
                    }"""),
            new CompileAssert.CompileCase("js.C",
                """
                    package js;
                    class C {
                      String ss = js.B.a.foo();
                    }""",
                """
                    class js$C extends Object {
                      constructor() {
                        super();
                        this.ss = js$B.a.foo()
                      }
                    }"""),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    class B {
                      static A a = new A();
                    }""",
                """
                    class js$B extends Object {
                      static a = new js$A()
                      
                      constructor() {
                        super();
                      }
                    }"""));
    }
}
