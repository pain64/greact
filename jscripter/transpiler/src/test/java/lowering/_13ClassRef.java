package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _13ClassRef {
    @Test void classRefForRecord() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.ClassRef;
                import com.greact.model.ClassRef.Reflexive;
                                    
                class Test {
                  record A(long x1, int x2){}
                  <T> void bar(@Reflexive T obj) { var ref = ClassRef.of(obj); }
                  void baz() { bar(new A(1, 1)); }
                }
                """,
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  static A = class extends Object {
                    constructor(x1, x2) {
                      super();
                      this.x1 = x1;
                      this.x2 = x2;
                    }
                  }
                  bar(obj) {
                    let ref = obj.__class__;
                  }
                  baz() {
                    this.bar((() => {
                      let __obj = new js$Test.A(1, 1);
                      __obj.__class__ = ({
                        name: () => 'js.Test.A',
                        params: () => [
                        ],
                        fields: () => [
                          {
                            name: () => 'x1',
                            __class__: () => ({
                              name: () => 'long',
                              params: () => [
                              ],
                              fields: () => [
                              ]
                            })
                          },
                          {
                            name: () => 'x2',
                            __class__: () => ({
                              name: () => 'int',
                              params: () => [
                              ],
                              fields: () => [
                              ]
                            })
                          }
                        ]
                      });
                      return __obj;
                    })());
                  }
                }
                """);
    }

    @Test void classRefGeneric() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.ClassRef;
                import com.greact.model.ClassRef.Reflexive;
                                
                class Test {
                  <T> void bar(@Reflexive T[] obj) { var ref = ClassRef.of(obj); }
                  void baz() { bar(new Integer[]{}); }
                }
                """,
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  bar(obj) {
                    let ref = obj.__class__;
                  }
                  baz() {
                    this.bar((() => {
                      let __obj = [];
                      __obj.__class__ = ({
                        name: () => 'Array',
                        params: () => [
                          {
                            name: () => 'java.lang.Integer',
                            params: () => [
                            ],
                            fields: () => [
                            ]
                          }
                        ],
                        fields: () => [
                        ]
                      });
                      return __obj;
                    })());
                  }
                }
                """);
    }
}