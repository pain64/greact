package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

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
                    class js_A extends Object {
                      constructor(age) {
                        super();
                        this.age = age;
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    import com.greact.model.MemberRef;
                    class B {
                      MemberRef<A, Long> ref1 = A::age;
                      MemberRef<A, Long> ref3 = a -> a.age();
                    }""",
                """
                    class js_B extends Object {
                      constructor() {
                        const __init__ = () => {
                          this.ref1 = {_memberNames: () => ['age'], _value: (v) => v.age, _className: () => 'java.lang.Long'};
                          this.ref3 = {_memberNames: () => ['age'], _value: (v) => v.age, _className: () => 'java.lang.Long'};
                        };
                        super();
                        __init__();
                      }
                    }
                    """));
    }

    @Test void memberRefForClass() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A { long age; }
                    """,
                """
                    class js_A extends Object {
                      constructor() {
                        const __init__ = () => {
                          this.age = 0;
                        };
                        super();
                        __init__();
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    public class B { A a; }
                    """,
                """
                    class js_B extends Object {
                      constructor() {
                        const __init__ = () => {
                          this.a = null;
                        };
                        super();
                        __init__();
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.C",
                """
                    package js;
                    import com.greact.model.MemberRef;
                    class C {
                      MemberRef<A, Long> ref1 = a -> a.age;
                      MemberRef<B, Long> ref2 = b -> b.a.age;
                    }""",
                """
                    class js_C extends Object {
                      constructor() {
                        const __init__ = () => {
                          this.ref1 = {_memberNames: () => ['age'], _value: (v) => v.age, _className: () => 'java.lang.Long'};
                          this.ref2 = {_memberNames: () => ['a', 'age'], _value: (v) => v.a.age, _className: () => 'java.lang.Long'};
                        };
                        super();
                        __init__();
                      }
                    }
                    """));
    }
}
