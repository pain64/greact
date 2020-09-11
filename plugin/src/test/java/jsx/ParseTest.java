package jsx;

import com.greact.jsx.ECMAWorker;
import com.greact.jsx.HTMLWorker;
import com.greact.jsx.lib.antlr4.html.HTMLLexer;
import com.greact.jsx.lib.antlr4.html.HTMLParser;
import com.greact.jsx.lib.antlr4.js.ECMAScriptLexer;
import com.greact.jsx.lib.antlr4.js.ECMAScriptParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

public class ParseTest {
    @Test void parseHtml() {
        HTMLLexer lexer = new HTMLLexer(CharStreams.fromString("<a src={var} />"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        HTMLParser parser = new HTMLParser(tokens);
        ParseTree tree = parser.htmlDocument();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new HTMLWorker(), tree);
    }

    @Test void parseEcma() {
        ECMAScriptLexer lexer = new ECMAScriptLexer(CharStreams.fromString("{x:1}"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ECMAScriptParser parser = new ECMAScriptParser(tokens);
        ParseTree tree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ECMAWorker(), tree);
    }
}
