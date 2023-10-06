package ui.analyze;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import jstack.greact.ViewEntryFinder;
import org.junit.jupiter.api.Test;
import util.AnalyzeAssertionsCompiler.CompilerAssertion;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static util.AnalyzeAssertionsCompiler.withAssert;

public class FindViewEntriesTest {
    static class HasViewsAssert extends CompilerAssertion<String[]> {
        @Override public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, String[] expected) {
            var classEntries = new ViewEntryFinder(ctx).find(cu);
            var hasViews = classEntries.stream()
                .flatMap(ce -> ce.viewHolders().stream())
                .map(vh -> vh.view().toString())
                .collect(Collectors.toList());

            assertLinesMatch(Arrays.asList(expected), hasViews);
        }
    }

    @Test void check_new_class_for_non_component_not_affected() {
        withAssert(HasViewsAssert.class, """
                class A {
                    void bar() { new String("hello"); }
                }""",
            new String[]{});
    }

    @Test void at_return_in_mount_method() {
        withAssert(HasViewsAssert.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    @Override public div render() {
                        return new div();
                    }
                }""",
            new String[]{"new div()"});
    }

    @Test void at_component_lambda() {
        withAssert(HasViewsAssert.class, """
                import jstack.greact.html.*;
                class A {
                    Component0<div> slot = () -> new div();
                }""",
            new String[]{"new div()"});
    }

    @Test void at_component_lambda_in_return() {
        withAssert(HasViewsAssert.class, """
                import jstack.greact.html.*;
                class A {
                    Component0<div> slot = () ->  {
                        return new div();
                    };
                }""",
            new String[]{"new div()"});
    }

    @Test void at_component_lambda_in_nested() {
        withAssert(HasViewsAssert.class, """
                import jstack.greact.html.*;
                class A {
                    Component0<div> slot = () ->  {
                        Component0<h1> nestedSlot = () -> new h1();
                        return new div();
                    };
                }""",
            new String[]{"new h1()", "new div()"});
    }

    @Test void at_component_in_inner_class() {
        withAssert(HasViewsAssert.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    static class B implements Component0<h1> {
                        @Override public h1 render() {
                            return new h1();
                        }
                    }
                    @Override public div render() {
                        return new div();
                    }
                }""",
            new String[]{"new div()", "new h1()"});
    }

    @Test void at_component_returns_component() {
        withAssert(HasViewsAssert.class, """
                import jstack.greact.html.*;
                class A implements Component0<h1> {
                    static class B implements Component0<h1> {
                        @Override public h1 render() {
                            return new h1();
                        }
                    }
                    @Override public B render() {
                        return new B();
                    }
                }""",
            new String[]{"new B()", "new h1()"});
    }

    @Test void via_superclass_implements_component() {
        withAssert(HasViewsAssert.class, """
                import jstack.greact.html.*;
                class A {
                    abstract static class B implements Component0<h1> {}
                    
                    class C extends B {
                        @Override public h1 render() {
                            return new h1();
                        }
                    }
                }""",
            new String[]{"new h1()"});
    }

    @Test void at_component_in_anon_inner_class_override() {
        /*
         * Do not do like this. It's violates idea of components, but it will be compiled
         * FIXME: deny this ???
         */
        withAssert(HasViewsAssert.class, """
                import jstack.greact.html.*;
                class A implements Component0<h1> {
                    static class B implements Component0<h1> {
                        @Override public h1 render() {
                            return new h1();
                        }
                    }
                    @Override public B render() {
                        return new B() {
                            @Override public h1 render() {
                                return new h1("overridden");
                            }
                        };
                    }
                }""",
            new String[]{
                """
                    new h1("overridden")""",
                """
                    new B(){
                       \s
                        () {
                            super();
                        }
                       \s
                        @Override
                        public h1 render() {
                            return new h1("overridden");
                        }
                    }""",
                """
                    new h1()"""
            });
    }

    @Test void nested_components_creation_should_work() {
        withAssert(HasViewsAssert.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    @Override public div render() {
                        return new div() {{
                            new h1();
                        }};
                    }
                }""",
            new String[]{
                """
                    new div(){
                       \s
                        () {
                            super();
                        }
                        {
                            new h1();
                        }
                    }"""
            });
    }
}
