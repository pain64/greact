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
                  _bar(obj) {
                    const ref = obj.__class__;
                  }
                  _baz() {
                    this._bar((() => {
                      const __obj = new js$Test.A(1, 1);
                      __obj.__class__ = ({
                        _name: () => 'js.Test.A',
                        _params: () => [
                        ],
                        _fields: () => [
                          {
                            _name: () => 'x1',
                            ___class__: () => ({
                              _name: () => 'long',
                              _params: () => [
                              ],
                              _fields: () => [
                              ]
                            })
                          },
                          {
                            _name: () => 'x2',
                            ___class__: () => ({
                              _name: () => 'int',
                              _params: () => [
                              ],
                              _fields: () => [
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
                  _bar(obj) {
                    const ref = obj.__class__;
                  }
                  _baz() {
                    this._bar((() => {
                      const __obj = [];
                      __obj.__class__ = ({
                        _name: () => 'Array',
                        _params: () => [
                          {
                            _name: () => 'java.lang.Integer',
                            _params: () => [
                            ],
                            _fields: () => [
                            ]
                          }
                        ],
                        _fields: () => [
                        ]
                      });
                      return __obj;
                    })());
                  }
                }
                """);
    }
}