lexer grammar  JSXLexer;

HTML_COMMENT             : '<!--' .*? '-->'                        ;
HTML_CONDITIONAL_COMMENT : '<![' .*? ']>'                          ;
DTD                      : '<!' .*? '>'                            ;
SEA_WS                   : (' '|'\t'|'\r'? '\n')+ -> skip          ;
HTML_TEXT                : ~[<{}]+                                 ;
JSX_NODE_OPEN            : '{'               -> pushMode(JSX_NODE) ;
SCRIPT_OPEN              : '<script' .*? '>' -> pushMode(SCRIPT)   ;
STYLE_OPEN               : '<style' .*? '>'  -> pushMode(STYLE)    ;
TAG_OPEN                 : '<'               -> pushMode(TAG)      ;

mode JSX_NODE;
JSX_NODE_CLOSE : '}' -> popMode ;
JSX_NODE_BODY  : ~[}]+          ;


mode TAG;
TAG_CLOSE       : '>' -> popMode                  ;
TAG_SLASH_CLOSE : '/>' -> popMode                 ;
TAG_SLASH       : '/'                             ;
TAG_EQUALS      : '=' -> pushMode(ATTVALUE)       ;
ID        : ID_NameStartChar ID_NameChar* ;
TAG_WHITESPACE  : [ \t\r\n] -> skip               ;


fragment ID_NameChar
    : ID_NameStartChar
    | '-'
    | '_'
    | '.'
    | [0-9]
    |   '\u00B7'
    |   '\u0300'..'\u036F'
    |   '\u203F'..'\u2040'
    ;

fragment ID_NameStartChar
    :   [:a-zA-Z]
    |   '\u2070'..'\u218F'
    |   '\u2C00'..'\u2FEF'
    |   '\u3001'..'\uD7FF'
    |   '\uF900'..'\uFDCF'
    |   '\uFDF0'..'\uFFFD'
    ;


mode SCRIPT;
SCRIPT_BODY       : .*? '</script>' -> popMode ;
SCRIPT_SHORT_BODY : .*? '</>' -> popMode       ;

mode STYLE;
STYLE_BODY       : .*? '</style>' -> popMode ;
STYLE_SHORT_BODY : .*? '</>' -> popMode      ;

mode ATTVALUE;
ATTR_STRING_OPEN : [ ]* '"' -> pushMode(ATTR_STRING) ;
JSX_ATTR_OPEN    : [ ]* '{' -> pushMode(JSX_ATTR)    ;

mode ATTR_STRING;
ATTR_STRING_BODY  : ~[<"]*                      ;
ATTR_STRING_CLOSE : '"'    -> popMode, popMode  ;

mode JSX_ATTR;
JSX_ATTR_CLOSE : '}' -> popMode, popMode ;
JSX_ATTR_BODY  : ~[}]+                   ;