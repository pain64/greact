// Generated from /home/over/build/greact/plugin/src/main/java/com/over64/greact/jsx/grammar/JSXLexer.g4 by ANTLR 4.8
package com.over64.greact.jsx.grammar.gen;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JSXLexer extends Lexer {
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
		JSX_NODE=1, TAG=2, SCRIPT=3, STYLE=4, ATTVALUE=5, ATTR_STRING=6, JSX_ATTR=7;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "JSX_NODE", "TAG", "SCRIPT", "STYLE", "ATTVALUE", "ATTR_STRING", 
		"JSX_ATTR"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"HTML_COMMENT", "HTML_CONDITIONAL_COMMENT", "DTD", "SEA_WS", "HTML_TEXT", 
			"JSX_NODE_OPEN", "SCRIPT_OPEN", "STYLE_OPEN", "TAG_OPEN", "JSX_NODE_CLOSE", 
			"JSX_NODE_BODY", "TAG_CLOSE", "TAG_SLASH_CLOSE", "TAG_SLASH", "TAG_EQUALS", 
			"ID", "TAG_WHITESPACE", "ID_NameChar", "ID_NameStartChar", "SCRIPT_BODY", 
			"SCRIPT_SHORT_BODY", "STYLE_BODY", "STYLE_SHORT_BODY", "ATTR_STRING_OPEN", 
			"JSX_ATTR_OPEN", "ATTR_STRING_BODY", "ATTR_STRING_CLOSE", "JSX_ATTR_CLOSE", 
			"JSX_ATTR_BODY"
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


	public JSXLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JSXLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\35\u0134\b\1\b\1"+
		"\b\1\b\1\b\1\b\1\b\1\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t"+
		"\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t"+
		"\17\4\20\t\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t"+
		"\26\4\27\t\27\4\30\t\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t"+
		"\35\4\36\t\36\3\2\3\2\3\2\3\2\3\2\3\2\7\2K\n\2\f\2\16\2N\13\2\3\2\3\2"+
		"\3\2\3\2\3\3\3\3\3\3\3\3\3\3\7\3Y\n\3\f\3\16\3\\\13\3\3\3\3\3\3\3\3\4"+
		"\3\4\3\4\3\4\7\4e\n\4\f\4\16\4h\13\4\3\4\3\4\3\5\3\5\5\5n\n\5\3\5\6\5"+
		"q\n\5\r\5\16\5r\3\5\3\5\3\6\6\6x\n\6\r\6\16\6y\3\7\3\7\3\7\3\7\3\b\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\3\b\7\b\u0089\n\b\f\b\16\b\u008c\13\b\3\b\3\b"+
		"\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\7\t\u009a\n\t\f\t\16\t\u009d"+
		"\13\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\6\f\u00ac"+
		"\n\f\r\f\16\f\u00ad\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\17\3\17"+
		"\3\20\3\20\3\20\3\20\3\21\3\21\7\21\u00c1\n\21\f\21\16\21\u00c4\13\21"+
		"\3\22\3\22\3\22\3\22\3\23\3\23\5\23\u00cc\n\23\3\24\5\24\u00cf\n\24\3"+
		"\25\7\25\u00d2\n\25\f\25\16\25\u00d5\13\25\3\25\3\25\3\25\3\25\3\25\3"+
		"\25\3\25\3\25\3\25\3\25\3\25\3\25\3\26\7\26\u00e4\n\26\f\26\16\26\u00e7"+
		"\13\26\3\26\3\26\3\26\3\26\3\26\3\26\3\27\7\27\u00f0\n\27\f\27\16\27\u00f3"+
		"\13\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30\7\30"+
		"\u0101\n\30\f\30\16\30\u0104\13\30\3\30\3\30\3\30\3\30\3\30\3\30\3\31"+
		"\7\31\u010d\n\31\f\31\16\31\u0110\13\31\3\31\3\31\3\31\3\31\3\32\7\32"+
		"\u0117\n\32\f\32\16\32\u011a\13\32\3\32\3\32\3\32\3\32\3\33\7\33\u0121"+
		"\n\33\f\33\16\33\u0124\13\33\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3"+
		"\35\3\35\3\36\6\36\u0131\n\36\r\36\16\36\u0132\13LZf\u008a\u009b\u00d3"+
		"\u00e5\u00f1\u0102\2\37\n\3\f\4\16\5\20\6\22\7\24\b\26\t\30\n\32\13\34"+
		"\f\36\r \16\"\17$\20&\21(\22*\23,\2.\2\60\24\62\25\64\26\66\278\30:\31"+
		"<\32>\33@\34B\35\n\2\3\4\5\6\7\b\t\n\4\2\13\13\"\"\5\2>>}}\177\177\3\2"+
		"\177\177\5\2\13\f\17\17\"\"\b\2/\60\62;aa\u00b9\u00b9\u0302\u0371\u2041"+
		"\u2042\n\2<<C\\c|\u2072\u2191\u2c02\u2ff1\u3003\ud801\uf902\ufdd1\ufdf2"+
		"\uffff\3\2\"\"\4\2$$>>\2\u013e\2\n\3\2\2\2\2\f\3\2\2\2\2\16\3\2\2\2\2"+
		"\20\3\2\2\2\2\22\3\2\2\2\2\24\3\2\2\2\2\26\3\2\2\2\2\30\3\2\2\2\2\32\3"+
		"\2\2\2\3\34\3\2\2\2\3\36\3\2\2\2\4 \3\2\2\2\4\"\3\2\2\2\4$\3\2\2\2\4&"+
		"\3\2\2\2\4(\3\2\2\2\4*\3\2\2\2\5\60\3\2\2\2\5\62\3\2\2\2\6\64\3\2\2\2"+
		"\6\66\3\2\2\2\78\3\2\2\2\7:\3\2\2\2\b<\3\2\2\2\b>\3\2\2\2\t@\3\2\2\2\t"+
		"B\3\2\2\2\nD\3\2\2\2\fS\3\2\2\2\16`\3\2\2\2\20p\3\2\2\2\22w\3\2\2\2\24"+
		"{\3\2\2\2\26\177\3\2\2\2\30\u0091\3\2\2\2\32\u00a2\3\2\2\2\34\u00a6\3"+
		"\2\2\2\36\u00ab\3\2\2\2 \u00af\3\2\2\2\"\u00b3\3\2\2\2$\u00b8\3\2\2\2"+
		"&\u00ba\3\2\2\2(\u00be\3\2\2\2*\u00c5\3\2\2\2,\u00cb\3\2\2\2.\u00ce\3"+
		"\2\2\2\60\u00d3\3\2\2\2\62\u00e5\3\2\2\2\64\u00f1\3\2\2\2\66\u0102\3\2"+
		"\2\28\u010e\3\2\2\2:\u0118\3\2\2\2<\u0122\3\2\2\2>\u0125\3\2\2\2@\u012a"+
		"\3\2\2\2B\u0130\3\2\2\2DE\7>\2\2EF\7#\2\2FG\7/\2\2GH\7/\2\2HL\3\2\2\2"+
		"IK\13\2\2\2JI\3\2\2\2KN\3\2\2\2LM\3\2\2\2LJ\3\2\2\2MO\3\2\2\2NL\3\2\2"+
		"\2OP\7/\2\2PQ\7/\2\2QR\7@\2\2R\13\3\2\2\2ST\7>\2\2TU\7#\2\2UV\7]\2\2V"+
		"Z\3\2\2\2WY\13\2\2\2XW\3\2\2\2Y\\\3\2\2\2Z[\3\2\2\2ZX\3\2\2\2[]\3\2\2"+
		"\2\\Z\3\2\2\2]^\7_\2\2^_\7@\2\2_\r\3\2\2\2`a\7>\2\2ab\7#\2\2bf\3\2\2\2"+
		"ce\13\2\2\2dc\3\2\2\2eh\3\2\2\2fg\3\2\2\2fd\3\2\2\2gi\3\2\2\2hf\3\2\2"+
		"\2ij\7@\2\2j\17\3\2\2\2kq\t\2\2\2ln\7\17\2\2ml\3\2\2\2mn\3\2\2\2no\3\2"+
		"\2\2oq\7\f\2\2pk\3\2\2\2pm\3\2\2\2qr\3\2\2\2rp\3\2\2\2rs\3\2\2\2st\3\2"+
		"\2\2tu\b\5\2\2u\21\3\2\2\2vx\n\3\2\2wv\3\2\2\2xy\3\2\2\2yw\3\2\2\2yz\3"+
		"\2\2\2z\23\3\2\2\2{|\7}\2\2|}\3\2\2\2}~\b\7\3\2~\25\3\2\2\2\177\u0080"+
		"\7>\2\2\u0080\u0081\7u\2\2\u0081\u0082\7e\2\2\u0082\u0083\7t\2\2\u0083"+
		"\u0084\7k\2\2\u0084\u0085\7r\2\2\u0085\u0086\7v\2\2\u0086\u008a\3\2\2"+
		"\2\u0087\u0089\13\2\2\2\u0088\u0087\3\2\2\2\u0089\u008c\3\2\2\2\u008a"+
		"\u008b\3\2\2\2\u008a\u0088\3\2\2\2\u008b\u008d\3\2\2\2\u008c\u008a\3\2"+
		"\2\2\u008d\u008e\7@\2\2\u008e\u008f\3\2\2\2\u008f\u0090\b\b\4\2\u0090"+
		"\27\3\2\2\2\u0091\u0092\7>\2\2\u0092\u0093\7u\2\2\u0093\u0094\7v\2\2\u0094"+
		"\u0095\7{\2\2\u0095\u0096\7n\2\2\u0096\u0097\7g\2\2\u0097\u009b\3\2\2"+
		"\2\u0098\u009a\13\2\2\2\u0099\u0098\3\2\2\2\u009a\u009d\3\2\2\2\u009b"+
		"\u009c\3\2\2\2\u009b\u0099\3\2\2\2\u009c\u009e\3\2\2\2\u009d\u009b\3\2"+
		"\2\2\u009e\u009f\7@\2\2\u009f\u00a0\3\2\2\2\u00a0\u00a1\b\t\5\2\u00a1"+
		"\31\3\2\2\2\u00a2\u00a3\7>\2\2\u00a3\u00a4\3\2\2\2\u00a4\u00a5\b\n\6\2"+
		"\u00a5\33\3\2\2\2\u00a6\u00a7\7\177\2\2\u00a7\u00a8\3\2\2\2\u00a8\u00a9"+
		"\b\13\7\2\u00a9\35\3\2\2\2\u00aa\u00ac\n\4\2\2\u00ab\u00aa\3\2\2\2\u00ac"+
		"\u00ad\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae\37\3\2\2"+
		"\2\u00af\u00b0\7@\2\2\u00b0\u00b1\3\2\2\2\u00b1\u00b2\b\r\7\2\u00b2!\3"+
		"\2\2\2\u00b3\u00b4\7\61\2\2\u00b4\u00b5\7@\2\2\u00b5\u00b6\3\2\2\2\u00b6"+
		"\u00b7\b\16\7\2\u00b7#\3\2\2\2\u00b8\u00b9\7\61\2\2\u00b9%\3\2\2\2\u00ba"+
		"\u00bb\7?\2\2\u00bb\u00bc\3\2\2\2\u00bc\u00bd\b\20\b\2\u00bd\'\3\2\2\2"+
		"\u00be\u00c2\5.\24\2\u00bf\u00c1\5,\23\2\u00c0\u00bf\3\2\2\2\u00c1\u00c4"+
		"\3\2\2\2\u00c2\u00c0\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3)\3\2\2\2\u00c4"+
		"\u00c2\3\2\2\2\u00c5\u00c6\t\5\2\2\u00c6\u00c7\3\2\2\2\u00c7\u00c8\b\22"+
		"\2\2\u00c8+\3\2\2\2\u00c9\u00cc\5.\24\2\u00ca\u00cc\t\6\2\2\u00cb\u00c9"+
		"\3\2\2\2\u00cb\u00ca\3\2\2\2\u00cc-\3\2\2\2\u00cd\u00cf\t\7\2\2\u00ce"+
		"\u00cd\3\2\2\2\u00cf/\3\2\2\2\u00d0\u00d2\13\2\2\2\u00d1\u00d0\3\2\2\2"+
		"\u00d2\u00d5\3\2\2\2\u00d3\u00d4\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d4\u00d6"+
		"\3\2\2\2\u00d5\u00d3\3\2\2\2\u00d6\u00d7\7>\2\2\u00d7\u00d8\7\61\2\2\u00d8"+
		"\u00d9\7u\2\2\u00d9\u00da\7e\2\2\u00da\u00db\7t\2\2\u00db\u00dc\7k\2\2"+
		"\u00dc\u00dd\7r\2\2\u00dd\u00de\7v\2\2\u00de\u00df\7@\2\2\u00df\u00e0"+
		"\3\2\2\2\u00e0\u00e1\b\25\7\2\u00e1\61\3\2\2\2\u00e2\u00e4\13\2\2\2\u00e3"+
		"\u00e2\3\2\2\2\u00e4\u00e7\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e5\u00e3\3\2"+
		"\2\2\u00e6\u00e8\3\2\2\2\u00e7\u00e5\3\2\2\2\u00e8\u00e9\7>\2\2\u00e9"+
		"\u00ea\7\61\2\2\u00ea\u00eb\7@\2\2\u00eb\u00ec\3\2\2\2\u00ec\u00ed\b\26"+
		"\7\2\u00ed\63\3\2\2\2\u00ee\u00f0\13\2\2\2\u00ef\u00ee\3\2\2\2\u00f0\u00f3"+
		"\3\2\2\2\u00f1\u00f2\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f2\u00f4\3\2\2\2\u00f3"+
		"\u00f1\3\2\2\2\u00f4\u00f5\7>\2\2\u00f5\u00f6\7\61\2\2\u00f6\u00f7\7u"+
		"\2\2\u00f7\u00f8\7v\2\2\u00f8\u00f9\7{\2\2\u00f9\u00fa\7n\2\2\u00fa\u00fb"+
		"\7g\2\2\u00fb\u00fc\7@\2\2\u00fc\u00fd\3\2\2\2\u00fd\u00fe\b\27\7\2\u00fe"+
		"\65\3\2\2\2\u00ff\u0101\13\2\2\2\u0100\u00ff\3\2\2\2\u0101\u0104\3\2\2"+
		"\2\u0102\u0103\3\2\2\2\u0102\u0100\3\2\2\2\u0103\u0105\3\2\2\2\u0104\u0102"+
		"\3\2\2\2\u0105\u0106\7>\2\2\u0106\u0107\7\61\2\2\u0107\u0108\7@\2\2\u0108"+
		"\u0109\3\2\2\2\u0109\u010a\b\30\7\2\u010a\67\3\2\2\2\u010b\u010d\t\b\2"+
		"\2\u010c\u010b\3\2\2\2\u010d\u0110\3\2\2\2\u010e\u010c\3\2\2\2\u010e\u010f"+
		"\3\2\2\2\u010f\u0111\3\2\2\2\u0110\u010e\3\2\2\2\u0111\u0112\7$\2\2\u0112"+
		"\u0113\3\2\2\2\u0113\u0114\b\31\t\2\u01149\3\2\2\2\u0115\u0117\t\b\2\2"+
		"\u0116\u0115\3\2\2\2\u0117\u011a\3\2\2\2\u0118\u0116\3\2\2\2\u0118\u0119"+
		"\3\2\2\2\u0119\u011b\3\2\2\2\u011a\u0118\3\2\2\2\u011b\u011c\7}\2\2\u011c"+
		"\u011d\3\2\2\2\u011d\u011e\b\32\n\2\u011e;\3\2\2\2\u011f\u0121\n\t\2\2"+
		"\u0120\u011f\3\2\2\2\u0121\u0124\3\2\2\2\u0122\u0120\3\2\2\2\u0122\u0123"+
		"\3\2\2\2\u0123=\3\2\2\2\u0124\u0122\3\2\2\2\u0125\u0126\7$\2\2\u0126\u0127"+
		"\3\2\2\2\u0127\u0128\b\34\7\2\u0128\u0129\b\34\7\2\u0129?\3\2\2\2\u012a"+
		"\u012b\7\177\2\2\u012b\u012c\3\2\2\2\u012c\u012d\b\35\7\2\u012d\u012e"+
		"\b\35\7\2\u012eA\3\2\2\2\u012f\u0131\n\4\2\2\u0130\u012f\3\2\2\2\u0131"+
		"\u0132\3\2\2\2\u0132\u0130\3\2\2\2\u0132\u0133\3\2\2\2\u0133C\3\2\2\2"+
		"\37\2\3\4\5\6\7\b\tLZfmpry\u008a\u009b\u00ad\u00c2\u00cb\u00ce\u00d3\u00e5"+
		"\u00f1\u0102\u010e\u0118\u0122\u0132\13\b\2\2\7\3\2\7\5\2\7\6\2\7\4\2"+
		"\6\2\2\7\7\2\7\b\2\7\t\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}