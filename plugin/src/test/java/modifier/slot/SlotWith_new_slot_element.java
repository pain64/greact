package modifier.slot;

import org.junit.jupiter.api.Test;
import util.CompileAssert;
import util.CompileAssert.CompileCase;

import java.io.IOException;

import static util.CompileAssert.*;

public class SlotWith_new_slot_element {
    @Test void testSlotWith_new_slot_element() throws IOException {
        assertCompiledMany(
            new CompileCase("js.Decorator",
                """
                    package com.over64.greact.sample;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.model.components.Component;
                                        
                    class Decorator implements Component<div> {
                        Component<h1> forDecorate = () -> null;
                                    
                        @Override public div mount() {
                            return new div() {{
                                style.border = "1px red solid";
                                new slot(forDecorate);
                            }};
                        }
                    }
                    """,
                """
                    """),
            new CompileCase("js.Demo",
                """
                    package com.over64.greact.sample;
                     import com.over64.greact.dom.HTMLNativeElements.*;
                     import com.over64.greact.model.components.Component;
                     
                     public class Demo implements Component<div> {                     
                         @Override public div mount() {
                             return new div() {{
                                 new Decorator() {{
                                     forDecorate = () -> new h1("decorated text!");
                                 }};
                             }};
                         }
                     }""",
                ""));
    }
}
