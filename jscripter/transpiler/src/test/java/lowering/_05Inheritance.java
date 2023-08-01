package lowering;

import jstack.jscripter.transpiler.generate.util.CompileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.CompileAssert.CompileCase;

import java.io.IOException;

import static util.CompileAssert.assertCompiledMany;

public class _05Inheritance {
    @Test void callOverloadedMany() throws IOException {
        assertCompiledMany(
            new CompileCase("js.A",
                """
                    package js;
                    public class A {
                      void foo(boolean x) { /* NOP */ };
                      int  foo(long x)    { return 1;    };
                      int  foo(int x)     { return 2;    };
                    }""",
                """
                    class js_A {
                      constructor() {
                      }
                      _foo($over, ...__args) {
                        if($over === 0) {
                          const [x] = __args;
                        } else if($over === 1) {
                          const [x] = __args;
                          return 1;
                        } else if($over === 2) {
                          const [x] = __args;
                          return 2;
                        }
                      }
                    }
                    """),
            new CompileCase("js.B",
                """
                    package js;
                    public class B extends A {
                      @Override
                      int foo(long x)  { return 3; };
                      int foo(float x) { return 4; }; 
                    }""",
                """
                    class js_B extends js_A {
                      constructor() {
                        super();
                      }
                      _foo($over, ...__args) {
                        if($over === 1) {
                          const [x] = __args;
                          return 3;
                        } else if($over === 3) {
                          const [x] = __args;
                          return 4;
                        } else
                          return super._foo.apply(this, arguments);
                      }
                    }
                    """),
            new CompileCase("js.C",
                """
                    package js;
                    public class C extends B {
                      @Override
                      int foo(int x)              { return 5; };
                      int foo(double x, double y) { return 6; };
                    }""",
                """
                    class js_C extends js_B {
                      constructor() {
                        super();
                      }
                      _foo($over, ...__args) {
                        if($over === 2) {
                          const [x] = __args;
                          return 5;
                        } else if($over === 4) {
                          const [x, y] = __args;
                          return 6;
                        } else
                          return super._foo.apply(this, arguments);
                      }
                    }
                    """)
        );
    }

//    @Test
//    void prohibitionOfInheritanceForJSNativeAPI() {
//        try {
//            assertCompiledMany(
//                new CompileCase("js.A",
//                    """
//                        package js;
//                        import jstack.jscripter.transpiler.model.JSNativeAPI;
//
//                        @JSNativeAPI
//                        public class A { }""",
//                    """
//                        """),
//                new CompileCase("js.Test",
//                    """
//                        package js;
//
//                        public class Test extends A { }""",
//                    """
//                        """));
//        } catch (Exception ex) {
//            var ce = (CompileException) ex.getCause();
//            Assertions.assertSame(CompileException.ERROR.PROHIBITION_OF_INHERITANCE_FOR_JS_NATIVE_API, ce.error);
//        }
//    }
}
