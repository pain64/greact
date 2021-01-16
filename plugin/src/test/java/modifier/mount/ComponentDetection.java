package modifier.mount;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class ComponentDetection {
    @Test void implAtSuperclass() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    abstract class A implements Component0<h1> {
                    }""",
                """
                    package js;
                                        
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    abstract class A implements Component0<h1> {
                       \s
                        A() {
                            super();
                        }
                    }"""),
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo extends A {
                      int nUsers = 1;
                      
                      @Override public h1 mount() {                        
                        return new h1("hello, world");
                      }
                    }""",
                """
                    package js;
                     
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                     
                    class Demo extends A {
                       \s
                        Demo() {
                            super();
                        }
                        int nUsers = 1;
                       \s
                        @Override
                        public h1 mount() {
                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                $root.innerText = "hello, world";
                            });
                        }
                    }"""));

    }

    @Test void extendsAtImplementedInterface() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    interface A extends Component0<h1> {
                    }""",
                """
                    package js;
                                        
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    interface A extends Component0<h1> {
                    }"""),
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements A {
                      int nUsers = 1;
                      
                      @Override public h1 mount() {                        
                        return new h1("hello, world");
                      }
                    }""",
                """
                    package js;
                     
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                     
                    class Demo implements A {
                       \s
                        Demo() {
                            super();
                        }
                        int nUsers = 1;
                       \s
                        @Override
                        public h1 mount() {
                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                $root.innerText = "hello, world";
                            });
                        }
                    }"""));

    }
}
