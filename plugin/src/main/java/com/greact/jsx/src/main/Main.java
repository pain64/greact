package main;

import antlr4.html.HTMLLexer;
import antlr4.html.HTMLParser;
import antlr4.js.ECMAScriptLexer;
import antlr4.js.ECMAScriptParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Main {
    public static void html() {
        HTMLLexer lexer = new HTMLLexer(CharStreams.fromString("<a src={var} />"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        HTMLParser parser = new HTMLParser(tokens);
        ParseTree tree = parser.htmlDocument();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new HTMLWorker(), tree);
    }

    public static void ecma() {
        ECMAScriptLexer lexer = new ECMAScriptLexer(CharStreams.fromString("{x:1}"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ECMAScriptParser parser = new ECMAScriptParser(tokens);
        ParseTree tree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ECMAWorker(), tree);
    }

    public static void main(String[] args) throws Exception {
        ecma();
        html();
    }
}