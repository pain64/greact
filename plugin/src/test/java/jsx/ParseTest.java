package jsx;

import com.over64.greact.jsx.Visitor;
import com.over64.greact.jsx.grammar.gen.JSXLexer;
import com.over64.greact.jsx.grammar.gen.JSXParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.over64.greact.jsx.Ast.*;

public class ParseTest {
    @Test void simple() {
        var lexer = new JSXLexer(CharStreams.fromString("""
            {123}
            <div id="root" of={123}>
              {123}
              <br>
              <input type="text" value={123} />
            </div>
            """));

        var tokens = new CommonTokenStream(lexer);
        var parser = new JSXParser(tokens);
        var tree = parser.root();
        var ast = new Visitor().visitRoot(tree);

        Assertions.assertEquals(ast,
            new Root(List.of(
                new Template("123"),
                new Element("div",
                    List.of(
                        new Attr("id", new AttrString("root")),
                        new Attr("of", new Template("123"))),
                    List.of(
                        new Template("123"),
                        new Element("br", List.of(), List.of()),
                        new Element("input",
                            List.of(
                                new Attr("type", new AttrString("text")),
                                new Attr("value", new Template("123"))),
                            List.of()))))));
    }
}
