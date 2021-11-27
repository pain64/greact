package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _04Constructor {

    @Test
    void constructorSimple() throws IOException {
        assertCompiled(
                """
                    package js;
                    public class Test {
                      final String x;
                      final String y;
                      int z = 42;
                      int f; // default value is 0
                      
                      public Test(String x, String y) {
                        this.x = x;
                        this.y = y;
                      }
                      
                      public Test(String x) {
                        this.x = x;
                        this.y = "hello";
                      }
                    }""",
                """
                        class js$Test extends Object {
                          constructor($over, ...__args) {
                            let __init__ = () => {
                              this.x = null
                              this.y = null
                              this.z = 42
                              this.f = 0
                            };
                            switch($over) {
                              case 1:
                                var [x, y] = __args;
                                super();
                                __init__();
                                this.x = x;
                                this.y = y;
                                break
                              case 2:
                                var [x] = __args;
                                super();
                                __init__();
                                this.x = x;
                                this.y = 'hello';
                                break
                            }
                          }
                        }""");
    }

    @Test
    void initBlock() throws IOException {
        assertCompiled(
                """
                    package js;
                    public class Test {
                      final String x;
                      final String y;
                      
                      {
                        x = "hello";
                      }
                      
                      public Test(String y) {
                        this.y = y;
                      }
                    }""",
                """
                    class js$Test extends Object {
                      constructor(y) {
                        let __init__ = () => {
                          this.x = null
                          this.y = null
                          this.x = 'hello';
                        };
                        super();
                        __init__();
                        this.y = y;
                      }
                    }""");
    }

    @Test void constructorOverloaded() throws IOException {
        assertCompiledMany(
                new CompileAssert.CompileCase("js.A",
                        """
                            package js;
                            public class A {
                              A(int x, int y){
                                var some = x + y;
                              }
                              A(float z){
                                var some = z;
                              }
                            }""",
                        """
                            class js$A extends Object {
                              constructor($over, ...__args) {
                                switch($over) {
                                  case 1:
                                    var [x, y] = __args;
                                    super();
                                    let some = x + y;
                                    break
                                  case 2:
                                    var [z] = __args;
                                    super();
                                    let some = z;
                                    break
                                }
                              }
                            }"""),
                new CompileAssert.CompileCase("js.B",
                        """
                            package js;
                            public class B {
                              B() { new A(42); }
                            }""",
                        """
                            class js$B extends Object {
                              constructor() {
                                super();
                                new js$A(2, 42);
                              }
                            }""")
        );
    }

    @Test void equalsReplace() throws IOException {
        assertCompiledMany(
                new CompileAssert.CompileCase("js.A",
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
                new CompileAssert.CompileCase("js.B",
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
                new CompileAssert.CompileCase("js.C",
                        """
                            package js;
                            public class C extends B {
                              @Override
                              int foo(int x)              { return 5; };
                              int foo(double x, double y) {
                                var a = new A();
                                var b = new B();
                                var c = a.equals(b);
                                return 6;
                              };
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
                                        let a = new js$A();
                                        let b = new js$B();
                                        let c = a == (b);
                                        return 6
                                      default:
                                        return super.foo.apply(this, arguments)
                                    }
                                  }
                                }""")
        );
    }

    @Test void equalsReplace2() throws IOException {
        assertCompiledMany(
                new CompileAssert.CompileCase("js.A",
                        """
                            package js;
                            public class A {
                              void foo(boolean x) { /* NOP */ };
                              int  foo(long x)    { return 1;    };
                              int  foo(int x)     { return 2;    };
                              @Override
                              public boolean equals(Object o) {
                                return true;
                              }
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
                                  
                                  equals(o) {
                                    return true
                                  }
                                }"""),
                new CompileAssert.CompileCase("js.B",
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
                new CompileAssert.CompileCase("js.C",
                        """
                            package js;
                            public class C extends B {
                              @Override
                              int foo(int x)              { return 5; };
                              int foo(double x, double y) {
                                var a = new A();
                                var b = new B();
                                var c = a.equals(b);
                                return 6;
                              };
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
                                        let a = new js$A();
                                        let b = new js$B();
                                        let c = a.equals(b);
                                        return 6
                                      default:
                                        return super.foo.apply(this, arguments)
                                    }
                                  }
                                }""")
        );
    }
}