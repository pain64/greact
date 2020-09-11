#!/bin/sh

java -Xmx500M -cp '/usr/local/lib/antlr-4.8-complete.jar:$CLASSPATH' org.antlr.v4.Tool  -Dlanguage=Java -lib main/gramma -o "../lib/antlr4/js" -visitor -Xexact-output-dir main/gramma/js/ECMAScript.g4
java -Xmx500M -cp '/usr/local/lib/antlr-4.8-complete.jar:$CLASSPATH' org.antlr.v4.Tool  -Dlanguage=Java -lib main/gramma -o "../lib/antlr4/html" -visitor -Xexact-output-dir main/gramma/html/HTMLLexer.g4
java -Xmx500M -cp '/usr/local/lib/antlr-4.8-complete.jar:$CLASSPATH' org.antlr.v4.Tool  -Dlanguage=Java -lib main/gramma -o "../lib/antlr4/html" -visitor -Xexact-output-dir main/gramma/html/HTMLParser.g4