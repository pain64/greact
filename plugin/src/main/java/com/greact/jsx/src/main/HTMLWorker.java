package main;

import antlr4.html.HTMLParser;
import antlr4.html.HTMLParserBaseListener;

public class HTMLWorker extends HTMLParserBaseListener {
    public void enterHtmlDocument(HTMLParser.HtmlDocumentContext ctx) {
        System.out.println(ctx);
        System.out.println("Entering: " + ctx.getText());
    }

    public void exitHtmlDocument(HTMLParser.HtmlDocumentContext ctx) {
        System.out.println("Exiting:" + ctx.getText());
    }
}