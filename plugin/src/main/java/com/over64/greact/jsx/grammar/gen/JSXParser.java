// Generated from /home/over/build/greact/plugin/src/main/java/com/over64/greact/jsx/grammar/JSXParser.g4 by ANTLR 4.8
package com.over64.greact.jsx.grammar.gen;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JSXParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		HTML_COMMENT=1, HTML_CONDITIONAL_COMMENT=2, DTD=3, SEA_WS=4, HTML_TEXT=5, 
		JSX_NODE_OPEN=6, SCRIPT_OPEN=7, STYLE_OPEN=8, TAG_OPEN=9, JSX_NODE_CLOSE=10, 
		JSX_NODE_BODY=11, TAG_CLOSE=12, TAG_SLASH_CLOSE=13, TAG_SLASH=14, TAG_EQUALS=15, 
		ID=16, TAG_WHITESPACE=17, SCRIPT_BODY=18, SCRIPT_SHORT_BODY=19, STYLE_BODY=20, 
		STYLE_SHORT_BODY=21, ATTR_STRING_OPEN=22, JSX_ATTR_OPEN=23, ATTR_STRING_BODY=24, 
		ATTR_STRING_CLOSE=25, JSX_ATTR_CLOSE=26, JSX_ATTR_BODY=27;
	public static final int
		RULE_root = 0, RULE_node = 1, RULE_element = 2, RULE_tag = 3, RULE_attr = 4, 
		RULE_text = 5, RULE_jsxNode = 6, RULE_comment = 7, RULE_script = 8, RULE_style = 9;
	private static String[] makeRuleNames() {
		return new String[] {
			"root", "node", "element", "tag", "attr", "text", "jsxNode", "comment", 
			"script", "style"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, "'{'", null, null, "'<'", null, null, 
			"'>'", "'/>'", "'/'", "'='", null, null, null, null, null, null, null, 
			null, null, "'\"'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "HTML_COMMENT", "HTML_CONDITIONAL_COMMENT", "DTD", "SEA_WS", "HTML_TEXT", 
			"JSX_NODE_OPEN", "SCRIPT_OPEN", "STYLE_OPEN", "TAG_OPEN", "JSX_NODE_CLOSE", 
			"JSX_NODE_BODY", "TAG_CLOSE", "TAG_SLASH_CLOSE", "TAG_SLASH", "TAG_EQUALS", 
			"ID", "TAG_WHITESPACE", "SCRIPT_BODY", "SCRIPT_SHORT_BODY", "STYLE_BODY", 
			"STYLE_SHORT_BODY", "ATTR_STRING_OPEN", "JSX_ATTR_OPEN", "ATTR_STRING_BODY", 
			"ATTR_STRING_CLOSE", "JSX_ATTR_CLOSE", "JSX_ATTR_BODY"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "JSXParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public JSXParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class RootContext extends ParserRuleContext {
		public List<NodeContext> node() {
			return getRuleContexts(NodeContext.class);
		}
		public NodeContext node(int i) {
			return getRuleContext(NodeContext.class,i);
		}
		public RootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_root; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitRoot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootContext root() throws RecognitionException {
		RootContext _localctx = new RootContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_root);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(23);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HTML_COMMENT) | (1L << HTML_CONDITIONAL_COMMENT) | (1L << HTML_TEXT) | (1L << JSX_NODE_OPEN) | (1L << SCRIPT_OPEN) | (1L << STYLE_OPEN) | (1L << TAG_OPEN))) != 0)) {
				{
				{
				setState(20);
				node();
				}
				}
				setState(25);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeContext extends ParserRuleContext {
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TextContext text() {
			return getRuleContext(TextContext.class,0);
		}
		public JsxNodeContext jsxNode() {
			return getRuleContext(JsxNodeContext.class,0);
		}
		public ElementContext element() {
			return getRuleContext(ElementContext.class,0);
		}
		public NodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_node; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitNode(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NodeContext node() throws RecognitionException {
		NodeContext _localctx = new NodeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_node);
		try {
			setState(30);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case HTML_COMMENT:
			case HTML_CONDITIONAL_COMMENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(26);
				comment();
				}
				break;
			case HTML_TEXT:
				enterOuterAlt(_localctx, 2);
				{
				setState(27);
				text();
				}
				break;
			case JSX_NODE_OPEN:
				enterOuterAlt(_localctx, 3);
				{
				setState(28);
				jsxNode();
				}
				break;
			case SCRIPT_OPEN:
			case STYLE_OPEN:
			case TAG_OPEN:
				enterOuterAlt(_localctx, 4);
				{
				setState(29);
				element();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementContext extends ParserRuleContext {
		public TagContext tag() {
			return getRuleContext(TagContext.class,0);
		}
		public ScriptContext script() {
			return getRuleContext(ScriptContext.class,0);
		}
		public StyleContext style() {
			return getRuleContext(StyleContext.class,0);
		}
		public ElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_element; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementContext element() throws RecognitionException {
		ElementContext _localctx = new ElementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_element);
		try {
			setState(35);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TAG_OPEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(32);
				tag();
				}
				break;
			case SCRIPT_OPEN:
				enterOuterAlt(_localctx, 2);
				{
				setState(33);
				script();
				}
				break;
			case STYLE_OPEN:
				enterOuterAlt(_localctx, 3);
				{
				setState(34);
				style();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TagContext extends ParserRuleContext {
		public Token open;
		public Token close;
		public List<TerminalNode> TAG_OPEN() { return getTokens(JSXParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(JSXParser.TAG_OPEN, i);
		}
		public List<TerminalNode> ID() { return getTokens(JSXParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(JSXParser.ID, i);
		}
		public TerminalNode TAG_SLASH_CLOSE() { return getToken(JSXParser.TAG_SLASH_CLOSE, 0); }
		public List<AttrContext> attr() {
			return getRuleContexts(AttrContext.class);
		}
		public AttrContext attr(int i) {
			return getRuleContext(AttrContext.class,i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(JSXParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(JSXParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(JSXParser.TAG_SLASH, 0); }
		public List<NodeContext> node() {
			return getRuleContexts(NodeContext.class);
		}
		public NodeContext node(int i) {
			return getRuleContext(NodeContext.class,i);
		}
		public TagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tag; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagContext tag() throws RecognitionException {
		TagContext _localctx = new TagContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tag);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			match(TAG_OPEN);
			setState(38);
			((TagContext)_localctx).open = match(ID);
			setState(42);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(39);
				attr();
				}
				}
				setState(44);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(59);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TAG_SLASH_CLOSE:
				{
				setState(45);
				match(TAG_SLASH_CLOSE);
				}
				break;
			case TAG_CLOSE:
				{
				{
				setState(46);
				match(TAG_CLOSE);
				setState(57);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
				case 1:
					{
					setState(50);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(47);
							node();
							}
							} 
						}
						setState(52);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
					}
					setState(53);
					match(TAG_OPEN);
					setState(54);
					match(TAG_SLASH);
					setState(55);
					((TagContext)_localctx).close = match(ID);
					setState(56);
					match(TAG_CLOSE);
					}
					break;
				}
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttrContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(JSXParser.ID, 0); }
		public TerminalNode TAG_EQUALS() { return getToken(JSXParser.TAG_EQUALS, 0); }
		public TerminalNode ATTR_STRING_OPEN() { return getToken(JSXParser.ATTR_STRING_OPEN, 0); }
		public TerminalNode ATTR_STRING_BODY() { return getToken(JSXParser.ATTR_STRING_BODY, 0); }
		public TerminalNode ATTR_STRING_CLOSE() { return getToken(JSXParser.ATTR_STRING_CLOSE, 0); }
		public TerminalNode JSX_ATTR_OPEN() { return getToken(JSXParser.JSX_ATTR_OPEN, 0); }
		public TerminalNode JSX_ATTR_BODY() { return getToken(JSXParser.JSX_ATTR_BODY, 0); }
		public TerminalNode JSX_ATTR_CLOSE() { return getToken(JSXParser.JSX_ATTR_CLOSE, 0); }
		public AttrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitAttr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttrContext attr() throws RecognitionException {
		AttrContext _localctx = new AttrContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_attr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			match(ID);
			setState(71);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TAG_EQUALS) {
				{
				setState(62);
				match(TAG_EQUALS);
				setState(69);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ATTR_STRING_OPEN:
					{
					{
					setState(63);
					match(ATTR_STRING_OPEN);
					setState(64);
					match(ATTR_STRING_BODY);
					setState(65);
					match(ATTR_STRING_CLOSE);
					}
					}
					break;
				case JSX_ATTR_OPEN:
					{
					{
					setState(66);
					match(JSX_ATTR_OPEN);
					setState(67);
					match(JSX_ATTR_BODY);
					setState(68);
					match(JSX_ATTR_CLOSE);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TextContext extends ParserRuleContext {
		public TerminalNode HTML_TEXT() { return getToken(JSXParser.HTML_TEXT, 0); }
		public TextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_text; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitText(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TextContext text() throws RecognitionException {
		TextContext _localctx = new TextContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_text);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			match(HTML_TEXT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JsxNodeContext extends ParserRuleContext {
		public TerminalNode JSX_NODE_OPEN() { return getToken(JSXParser.JSX_NODE_OPEN, 0); }
		public TerminalNode JSX_NODE_BODY() { return getToken(JSXParser.JSX_NODE_BODY, 0); }
		public TerminalNode JSX_NODE_CLOSE() { return getToken(JSXParser.JSX_NODE_CLOSE, 0); }
		public JsxNodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsxNode; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitJsxNode(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JsxNodeContext jsxNode() throws RecognitionException {
		JsxNodeContext _localctx = new JsxNodeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_jsxNode);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			match(JSX_NODE_OPEN);
			setState(76);
			match(JSX_NODE_BODY);
			setState(77);
			match(JSX_NODE_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommentContext extends ParserRuleContext {
		public TerminalNode HTML_COMMENT() { return getToken(JSXParser.HTML_COMMENT, 0); }
		public TerminalNode HTML_CONDITIONAL_COMMENT() { return getToken(JSXParser.HTML_CONDITIONAL_COMMENT, 0); }
		public CommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_comment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			_la = _input.LA(1);
			if ( !(_la==HTML_COMMENT || _la==HTML_CONDITIONAL_COMMENT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScriptContext extends ParserRuleContext {
		public TerminalNode SCRIPT_OPEN() { return getToken(JSXParser.SCRIPT_OPEN, 0); }
		public TerminalNode SCRIPT_BODY() { return getToken(JSXParser.SCRIPT_BODY, 0); }
		public TerminalNode SCRIPT_SHORT_BODY() { return getToken(JSXParser.SCRIPT_SHORT_BODY, 0); }
		public ScriptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_script; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitScript(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScriptContext script() throws RecognitionException {
		ScriptContext _localctx = new ScriptContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_script);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			match(SCRIPT_OPEN);
			setState(82);
			_la = _input.LA(1);
			if ( !(_la==SCRIPT_BODY || _la==SCRIPT_SHORT_BODY) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StyleContext extends ParserRuleContext {
		public TerminalNode STYLE_OPEN() { return getToken(JSXParser.STYLE_OPEN, 0); }
		public TerminalNode STYLE_BODY() { return getToken(JSXParser.STYLE_BODY, 0); }
		public TerminalNode STYLE_SHORT_BODY() { return getToken(JSXParser.STYLE_SHORT_BODY, 0); }
		public StyleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_style; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JSXParserVisitor ) return ((JSXParserVisitor<? extends T>)visitor).visitStyle(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StyleContext style() throws RecognitionException {
		StyleContext _localctx = new StyleContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_style);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			match(STYLE_OPEN);
			setState(85);
			_la = _input.LA(1);
			if ( !(_la==STYLE_BODY || _la==STYLE_SHORT_BODY) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\35Z\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\3"+
		"\2\7\2\30\n\2\f\2\16\2\33\13\2\3\3\3\3\3\3\3\3\5\3!\n\3\3\4\3\4\3\4\5"+
		"\4&\n\4\3\5\3\5\3\5\7\5+\n\5\f\5\16\5.\13\5\3\5\3\5\3\5\7\5\63\n\5\f\5"+
		"\16\5\66\13\5\3\5\3\5\3\5\3\5\5\5<\n\5\5\5>\n\5\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\5\6H\n\6\5\6J\n\6\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3"+
		"\n\3\13\3\13\3\13\3\13\2\2\f\2\4\6\b\n\f\16\20\22\24\2\5\3\2\3\4\3\2\24"+
		"\25\3\2\26\27\2[\2\31\3\2\2\2\4 \3\2\2\2\6%\3\2\2\2\b\'\3\2\2\2\n?\3\2"+
		"\2\2\fK\3\2\2\2\16M\3\2\2\2\20Q\3\2\2\2\22S\3\2\2\2\24V\3\2\2\2\26\30"+
		"\5\4\3\2\27\26\3\2\2\2\30\33\3\2\2\2\31\27\3\2\2\2\31\32\3\2\2\2\32\3"+
		"\3\2\2\2\33\31\3\2\2\2\34!\5\20\t\2\35!\5\f\7\2\36!\5\16\b\2\37!\5\6\4"+
		"\2 \34\3\2\2\2 \35\3\2\2\2 \36\3\2\2\2 \37\3\2\2\2!\5\3\2\2\2\"&\5\b\5"+
		"\2#&\5\22\n\2$&\5\24\13\2%\"\3\2\2\2%#\3\2\2\2%$\3\2\2\2&\7\3\2\2\2\'"+
		"(\7\13\2\2(,\7\22\2\2)+\5\n\6\2*)\3\2\2\2+.\3\2\2\2,*\3\2\2\2,-\3\2\2"+
		"\2-=\3\2\2\2.,\3\2\2\2/>\7\17\2\2\60;\7\16\2\2\61\63\5\4\3\2\62\61\3\2"+
		"\2\2\63\66\3\2\2\2\64\62\3\2\2\2\64\65\3\2\2\2\65\67\3\2\2\2\66\64\3\2"+
		"\2\2\678\7\13\2\289\7\20\2\29:\7\22\2\2:<\7\16\2\2;\64\3\2\2\2;<\3\2\2"+
		"\2<>\3\2\2\2=/\3\2\2\2=\60\3\2\2\2>\t\3\2\2\2?I\7\22\2\2@G\7\21\2\2AB"+
		"\7\30\2\2BC\7\32\2\2CH\7\33\2\2DE\7\31\2\2EF\7\35\2\2FH\7\34\2\2GA\3\2"+
		"\2\2GD\3\2\2\2HJ\3\2\2\2I@\3\2\2\2IJ\3\2\2\2J\13\3\2\2\2KL\7\7\2\2L\r"+
		"\3\2\2\2MN\7\b\2\2NO\7\r\2\2OP\7\f\2\2P\17\3\2\2\2QR\t\2\2\2R\21\3\2\2"+
		"\2ST\7\t\2\2TU\t\3\2\2U\23\3\2\2\2VW\7\n\2\2WX\t\4\2\2X\25\3\2\2\2\13"+
		"\31 %,\64;=GI";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}