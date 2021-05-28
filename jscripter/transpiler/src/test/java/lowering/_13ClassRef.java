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
                      static A = class extends Object {
                        constructor(x1, x2) {
                          let __init__ = () => {
                            this.x1 = 0
                            this.x2 = 0
                          };
                          super();
                          __init__();
                          this.x1 = x1;
                          this.x2 = x2;
                        }
                      }
                      
                      static __reflect1(obj) {
                        obj.__class__ = () => ({
                          name: () => 'js$Test.A',
                          fields: () => [
                            {
                              name: () => 'x1',
                              __class__: () => ({
                                  name: () => 'java.lang.Long',
                                  fields: () => []
                              })
                            },
                            {
                              name: () => 'x2',
                              __class__: () => ({
                                  name: () => 'java.lang.Integer',
                                  fields: () => []
                              })
                            }
                          ]
                        })
                        return obj;
                      }
                                        
                      constructor() {
                        super();
                      }
                                        
                      bar(obj) {
                        let ref = obj.__class_ref__;
                      }
                                        
                      baz() {
                        this.bar(__reflect1(new js$Test.A(1, 1)));
                      }
                    }""");
    }}