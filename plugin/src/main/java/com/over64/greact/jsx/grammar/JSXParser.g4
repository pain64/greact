parser grammar JSXParser;
options { tokenVocab=JSXLexer; }

root    : node* ;
node    : comment | text | jsxNode | element ;
element : tag | script | style ;

tag  : TAG_OPEN open=ID attr* (TAG_SLASH_CLOSE | (TAG_CLOSE (node* TAG_OPEN TAG_SLASH close=ID TAG_CLOSE)?)) ;
attr : ID (TAG_EQUALS ((ATTR_STRING_OPEN ATTR_STRING_BODY ATTR_STRING_CLOSE) | (JSX_ATTR_OPEN JSX_ATTR_BODY JSX_ATTR_CLOSE)))? ;

text    : HTML_TEXT ;
jsxNode : JSX_NODE_OPEN JSX_NODE_BODY JSX_NODE_CLOSE ;

comment : HTML_COMMENT
        | HTML_CONDITIONAL_COMMENT ;

script : SCRIPT_OPEN ( SCRIPT_BODY | SCRIPT_SHORT_BODY) ;
style  : STYLE_OPEN  ( STYLE_BODY | STYLE_SHORT_BODY)   ;