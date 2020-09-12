package jsx;

import com.greact.jsx.ECMAWorker;
import com.greact.jsx.HTMLWorker;
import com.greact.jsx.lib.antlr4.html.HTMLLexer;
import com.greact.jsx.lib.antlr4.html.HTMLParser;
import com.greact.jsx.lib.antlr4.js.ECMAScriptLexer;
import com.greact.jsx.lib.antlr4.js.ECMAScriptParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

public class ParseTest {
    @Test void parseHtml() {
        var lexer = new HTMLLexer(CharStreams.fromString("<a src={var} />"));
        var tokens = new CommonTokenStream(lexer);
        var parser = new HTMLParser(tokens);
        var tree = parser.htmlDocument();
        var walker = new ParseTreeWalker();
        walker.walk(new HTMLWorker(), tree);
    }

    @Test void parseEcma() {
        var lexer = new ECMAScriptLexer(CharStreams.fromString("{x:1}"));
        var tokens = new CommonTokenStream(lexer);
        var parser = new ECMAScriptParser(tokens);
        var tree = parser.program();
        var walker = new ParseTreeWalker();
        walker.walk(new ECMAWorker(), tree);
    }
}
