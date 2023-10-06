package jstack.greact.processors;

import jstack.greact.AstProcessor;
import jstack.greact.GReactCompileException;
import jstack.greact.Util;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import jstack.greact.uikit.CodeView;

import java.io.IOException;

public class CodeViewAstProcessor implements AstProcessor {
    private Util util;
    private Name constructorName;

    class Symbols {
        Symbol.ClassSymbol clCodeView = util.lookupClass(CodeView.class);
        Symbol.ClassSymbol clString = util.lookupClass(String.class);
        Symbol.MethodSymbol constructor2 = (Symbol.MethodSymbol)
            clCodeView.getEnclosedElements().stream()
                .filter(member -> member.getSimpleName() == constructorName)
                .skip(1)
                .findFirst().orElseThrow(() -> new RuntimeException("unreachable"));
    }


    TreeMaker maker;
    Symbols symbols;

    static class CodeViewMisuseException extends GReactCompileException {
        public CodeViewMisuseException(JCTree tree, String message) {
            super(tree, message);
        }
    }

    String whitespacePrefix(CharSequence code, int start) {
        int i = 0;
        while (start - i - 1 >= 0 && code.charAt(start - i - 1) == ' ') i++;
        return " ".repeat(i);
    }

    @Override
    public void init(Context context) {
        this.util = new Util(context);
        this.constructorName = Names.instance(context).fromString("<init>");
        this.maker = TreeMaker.instance(context);
        this.symbols = new Symbols();
    }

    @Override
    public void apply(JCTree.JCCompilationUnit cu) {
        cu.accept(new TreeScanner() {
            @Override public void visitNewClass(JCTree.JCNewClass newClass) {
                if (newClass.type.tsym == symbols.clCodeView) {
                    var viewCompExpression = newClass.args.head;
                    final String code;

                    if (viewCompExpression instanceof JCTree.JCLambda lambda)
                        if (lambda.body instanceof JCTree.JCNewClass compExpr) {
                            try {
                                var cuCode = cu.getSourceFile().getCharContent(true);
                                var prefix = whitespacePrefix(cuCode, compExpr.getStartPosition());

                                code = cuCode.subSequence(
                                        compExpr.getStartPosition(),
                                        compExpr.getEndPosition(cu.endPositions))
                                    .toString()
                                    .replaceAll("\n" + prefix, "\n");

                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else throw util.compilationError(
                            cu, new CodeViewAstProcessor.CodeViewMisuseException(
                                newClass,
                                "expected new class as CodeView component lambda body but has: " +
                                    lambda.body
                            )
                        );
                    else throw util.compilationError(
                        cu, new CodeViewAstProcessor.CodeViewMisuseException(
                            newClass,
                            "expected lambda component as first arg for CodeView but has: " +
                                viewCompExpression
                        )
                    );

                    newClass.constructor = symbols.constructor2;
                    newClass.constructorType = symbols.constructor2.type;
                    newClass.args = newClass.args.append(maker.Literal(code).setType(symbols.clString.type));
                }

                super.visitNewClass(newClass);
            }
        });

    }
}

