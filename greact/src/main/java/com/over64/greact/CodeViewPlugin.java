package com.over64.greact;

import com.over64.greact.dom.CodeView;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.io.IOException;

public class CodeViewPlugin {
    public CodeViewPlugin(Context context) {
        this.util = new Util(context);
        this.constructorName = Names.instance(context).fromString("<init>");
        this.maker = TreeMaker.instance(context);
        this.symbols = new Symbols();
    }

    class Symbols {
        Symbol.ClassSymbol clCodeView = util.lookupClass(CodeView.class);
        Symbol.ClassSymbol clString = util.lookupClass(String.class);
        Symbol.MethodSymbol constructor2 = (Symbol.MethodSymbol)
            clCodeView.getEnclosedElements().stream()
                .filter(member -> member.getSimpleName() == constructorName)
                .skip(1)
                .findFirst().orElseThrow(() -> new RuntimeException("unreachable"));
    }

    final Util util;
    final Name constructorName;
    final TreeMaker maker;
    final Symbols symbols;

    String whitespacePrefix(CharSequence code, int start) {
        int i = 0;
        while(start - i - 1 >= 0 && code.charAt(start - i - 1) == ' ') i++;
        return " ".repeat(i);
    }

    public void apply(JCTree.JCCompilationUnit cu) {
        cu.accept(new TreeScanner() {
            @Override public void visitNewClass(JCTree.JCNewClass newClass) {
                if (newClass.type.tsym == symbols.clCodeView) {
                    var viewCompExpression = newClass.args.head;
                    final String code;

                    if (viewCompExpression instanceof JCTree.JCLambda lambda)
                        if (lambda.body instanceof JCTree.JCNewClass compExpr) {
                            var exprForCode = ((JCTree.JCBlock) compExpr.def.defs.last()).stats.head;

                            try {
                                var cuCode = cu.getSourceFile().getCharContent(true);
                                var prefix = whitespacePrefix(cuCode, exprForCode.getStartPosition());

                                code = cuCode.subSequence(
                                    exprForCode.getStartPosition(),
                                    exprForCode.getEndPosition(cu.endPositions))
                                    .toString()
                                    .replaceAll("\n" + prefix, "\n");

                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else throw new RuntimeException(
                            "expected new class as CodeView component lambda body but has: " +
                                lambda.body);
                    else throw new RuntimeException(
                        "expected lambda component as first arg for CodeView but has: " +
                            viewCompExpression);

                    newClass.constructor = symbols.constructor2;
                    newClass.constructorType = symbols.constructor2.type;
                    newClass.args = newClass.args.append(maker.Literal(code).setType(symbols.clString.type));
                }

                super.visitNewClass(newClass);
            }
        });

    }
}
