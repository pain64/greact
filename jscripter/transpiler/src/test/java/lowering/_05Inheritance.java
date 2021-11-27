package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;
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
                    class js$A extends Object {
                      constructor() {
                        super();
                      }
                    
                      foo($over, ...__args) {
                        switch($over) {
                          case 0:
                            var [x] = __args;
                            break
                          case 1:
                            var [x] = __args;
                            return 1
                          case 2:
                            var [x] = __args;
                            return 2
                        }
                      }
                    }"""),
            new CompileCase("js.B",
                """
                    package js;
                    public class B extends A {
                      @Override
                      int foo(long x)  { return 3; };
                      int foo(float x) { return 4; }; 
                    }""",
                """
                        class js$B extends js$A {
                          constructor() {
                            super();
                          }
                                             
                          foo($over, ...__args) {
                            switch($over) {
                              case 1:
                                var [x] = __args;
                                return 3
                              case 3:
                                var [x] = __args;
                                return 4
                              default:
                                return super.foo.apply(this, arguments)
                            }
                          }
                        }"""),
            new CompileCase("js.C",
                """
                    package js;
                    public class C extends B {
                      @Override
                      int foo(int x)              { return 5; };
                      int foo(double x, double y) { return 6; };
                    }""",
                    """
                            class js$C extends js$B {
                              constructor() {
                                super();
                              }
                                                  
                              foo($over, ...__args) {
                                switch($over) {
                                  case 2:
                                    var [x] = __args;
                                    return 5
                                  case 4:
                                    var [x, y] = __args;
                                    return 6
                                  default:
                                    return super.foo.apply(this, arguments)
                                }
                              }
                            }""")
        );
    }
}
