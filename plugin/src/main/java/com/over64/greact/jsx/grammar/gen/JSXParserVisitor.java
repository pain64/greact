// Generated from /home/over/build/greact/plugin/src/main/java/com/over64/greact/jsx/grammar/JSXParser.g4 by ANTLR 4.8
package com.over64.greact.jsx.grammar.gen;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JSXParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JSXParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JSXParser#root}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot(JSXParser.RootContext ctx);
	/**
	 * Visit a parse tree produced by {@link JSXParser#node}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNode(JSXParser.NodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link JSXParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement(JSXParser.ElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JSXParser#tag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTag(JSXParser.TagContext ctx);
	/**
	 * Visit a parse tree produced by {@link JSXParser#attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttr(JSXParser.AttrContext ctx);
	/**
	 * Visit a parse tree produced by {@link JSXParser#text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitText(JSXParser.TextContext ctx);
	/**
	 * Visit a parse tree produced by {@link JSXParser#jsxNode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsxNode(JSXParser.JsxNodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link JSXParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(JSXParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link JSXParser#script}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScript(JSXParser.ScriptContext ctx);
	/**
	 * Visit a parse tree produced by {@link JSXParser#style}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStyle(JSXParser.StyleContext ctx);
}