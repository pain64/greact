package com.greact.jsx;


import com.greact.jsx.lib.antlr4.html.HTMLParser;
import com.greact.jsx.lib.antlr4.html.HTMLParserBaseListener;

public class HTMLWorker extends HTMLParserBaseListener {
    public void enterHtmlDocument(HTMLParser.HtmlDocumentContext ctx) {
        System.out.println(ctx);
        System.out.println("Entering: " + ctx.getText());
    }

    public void exitHtmlDocument(HTMLParser.HtmlDocumentContext ctx) {
        System.out.println("Exiting:" + ctx.getText());
    }
}