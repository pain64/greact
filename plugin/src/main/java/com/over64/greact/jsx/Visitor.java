package com.over64.greact.jsx;

import com.over64.greact.jsx.Ast.*;
import com.over64.greact.jsx.grammar.gen.JSXParser;
import com.over64.greact.jsx.grammar.gen.JSXParserVisitor;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

import java.util.List;
import java.util.stream.Collectors;

public class Visitor extends AbstractParseTreeVisitor<Ast> implements JSXParserVisitor<Ast> {

    @Override public Ast visitRoot(JSXParser.RootContext ctx) {
        return new Root(ctx.node().stream()
            .map(nctx -> (Node) visitNode(nctx))
            .collect(Collectors.toList()));
    }

    @Override public Ast visitNode(JSXParser.NodeContext ctx) {
        return visitChildren(ctx);
    }

    @Override public Ast visitElement(JSXParser.ElementContext ctx) {
        return visitChildren(ctx);
    }

    @Override public Ast visitTag(JSXParser.TagContext ctx) {
        var openTagName = ctx.open.getText();

        var attributes = ctx.attr().stream()
            .map(attrCtx -> (Attr) visitAttr(attrCtx))
            .collect(Collectors.toList());

        if (ctx.TAG_SLASH_CLOSE() != null || ctx.close == null)
            return new Element(openTagName, attributes, List.of());


        var closeTagName = ctx.close.getText();
        if (!openTagName.equals(closeTagName))
            throw new RuntimeException("open and close tag name does not matches");

        var children = ctx.node().stream()
            .map(nctx -> (Node) visitNode(nctx))
            .collect(Collectors.toList());


        return new Element(openTagName, attributes, children);
    }

    @Override public Ast visitAttr(JSXParser.AttrContext ctx) {
        var body = ctx.ATTR_STRING_BODY() != null
            ? new AttrString(ctx.ATTR_STRING_BODY().getText())
            : new Template(ctx.JSX_ATTR_BODY().getText());

        return new Attr(ctx.ID().getText(), body);
    }
    @Override public Ast visitText(JSXParser.TextContext ctx) {
        return new Text(ctx.getText());
    }

    @Override public Ast visitJsxNode(JSXParser.JsxNodeContext ctx) {
        return new Template(ctx.JSX_NODE_BODY().getText());
    }

    @Override public Ast visitComment(JSXParser.CommentContext ctx) {
        var body = ctx.HTML_COMMENT() != null
            ? ctx.HTML_COMMENT().getText()
            : ctx.HTML_CONDITIONAL_COMMENT().getText();
        return new Comment(body);
    }

    @Override public Ast visitScript(JSXParser.ScriptContext ctx) {
        var body = ctx.SCRIPT_BODY() != null
            ? ctx.SCRIPT_BODY().getText()
            : ctx.SCRIPT_SHORT_BODY().getText();

        return new Element("script", List.of(), List.of(new Text(body)));
    }

    @Override public Ast visitStyle(JSXParser.StyleContext ctx) {
        var body = ctx.STYLE_BODY() != null
            ? ctx.STYLE_BODY().getText()
            : ctx.STYLE_SHORT_BODY().getText();

        return new Element("style", List.of(), List.of(new Text(body)));
    }
}
