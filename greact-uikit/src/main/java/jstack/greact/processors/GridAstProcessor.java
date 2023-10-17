package jstack.greact.processors;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import jstack.greact.AstProcessor;
import jstack.greact.Util;
import jstack.greact.model.MemberRef;
import jstack.greact.uikit.Column;
import jstack.greact.uikit.Grid;
import jstack.jscripter.transpiler.generate.util.CompileException;
import jstack.jscripter.transpiler.model.JSExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GridAstProcessor implements AstProcessor {
    private Util util;
    private Name constructorName;

    class Symbols {
        Symbol.ClassSymbol clObject = util.lookupClass(Object.class);
        Symbol.ClassSymbol clGrid = util.lookupClass(Grid.class);
        Symbol.ClassSymbol clMemberRef = util.lookupClass(MemberRef.class);
        Symbol.ClassSymbol clColumn = util.lookupClass(Column.class);
        Symbol.ClassSymbol clJSExpression = util.lookupClass(JSExpression.class);
        Symbol.ClassSymbol clString = util.lookupClass(String.class);
        Symbol.MethodSymbol mtJSExpressionOf = util.lookupMember(clJSExpression, "of");

        Symbol.MethodSymbol columnConstructor2 = (Symbol.MethodSymbol)
            clColumn.getEnclosedElements().stream()
                .filter(member -> member.getSimpleName() == constructorName)
                .skip(1)
                .findFirst().orElseThrow(() -> new RuntimeException("unreachable"));
        Symbol.MethodSymbol gridConstructor1 = (Symbol.MethodSymbol)
            clGrid.getEnclosedElements().stream()
                .filter(member -> member.getSimpleName() == constructorName)
                .findFirst().orElseThrow(() -> new RuntimeException("unreachable"));

        Symbol.MethodSymbol gridConstructor2 = (Symbol.MethodSymbol)
            clGrid.getEnclosedElements().stream()
                .filter(member -> member.getSimpleName() == constructorName)
                .skip(1)
                .findFirst().orElseThrow(() -> new RuntimeException("unreachable"));
    }


    TreeMaker maker;
    Symbols symbols;
    Symtab symtab;
    Names names;

    @Override public void init(Context context) {
        this.util = new Util(context);
        this.constructorName = Names.instance(context).fromString("<init>");
        this.maker = TreeMaker.instance(context);
        this.symbols = new Symbols();
        this.names = new Names(context);
        this.symtab = Symtab.instance(context);
    }

    class GridColumnAutoCreator extends TreeScanner {

        private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\$\\d+)");
        private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("_");
        private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(\\p{Upper})");

        @Override public void visitNewClass(JCTree.JCNewClass newClass) {

            if (newClass.clazz.type.tsym == symbols.clGrid && newClass.args.size() == 1) {
                var rowType = ((Type.ArrayType) newClass.args.head.type).elemtype;
                var rowClass = (Symbol.ClassSymbol) rowType.tsym;
                var newColumnExpressions = List.<JCTree.JCExpression>nil();

                for (var recordComponent : rowClass.getRecordComponents()) {
                    var columnName = recordComponent.name.toString();

                    columnName = SNAKE_CASE_PATTERN.matcher(columnName)
                        .replaceAll(x -> " ");

                    columnName = CAMEL_CASE_PATTERN.matcher(columnName)
                        .replaceAll(x -> " " + x.group().toLowerCase());

                    columnName = DIGIT_PATTERN.matcher(columnName)
                        .replaceAll(x ->
                            String.valueOf(
                                (char) Integer.parseInt(x.group().substring(1))
                            )
                        );

                    columnName = String.valueOf(columnName.charAt(0))
                        .toUpperCase() + columnName.substring(1);

                    var columnType = new Type.ClassType(
                        Type.noType,
                        List.of(rowType, recordComponent.type),
                        symbols.clColumn.type.tsym
                    );

                    var memberRefType = new Type.ClassType(
                        Type.noType,
                        List.of(rowType, recordComponent.type),
                        symbols.clMemberRef
                    );

                    var newColumnExpression = (JCTree.JCNewClass) maker.NewClass(
                        null,
                        List.nil(),
                        maker.TypeApply(
                            maker.Ident(symbols.clColumn), List.nil()
                        ).setType(symbols.clColumn.type),
                        List.of(
                            maker.Literal(columnName).setType(symbols.clString.type),
                            maker.Reference(
                                MemberReferenceTree.ReferenceMode.INVOKE,
                                recordComponent.name,
                                maker.Ident(rowType.tsym),
                                null
                            ).setType(memberRefType)
                        ),
                        null
                    ).setType(columnType);

                    newColumnExpression.constructor = symbols.columnConstructor2;
                    newColumnExpression.constructorType = symbols.columnConstructor2.type;
                    newColumnExpressions = newColumnExpressions.append(newColumnExpression);
                }

                newClass.args = newClass.args.append(
                    maker.NewArray(
                        maker.Ident(symbols.clColumn),
                        List.nil(),
                        newColumnExpressions
                    ).setType(
                        new Type.ArrayType(
                            symbols.clColumn.erasure_field, symtab.arrayClass
                        )
                    )
                );

                if (!newClass.type.tsym.isAnonymous()) {
                    newClass.constructor = symbols.gridConstructor2;
                    newClass.constructorType = symbols.gridConstructor2.type;

                } else {
                    var constructor = ((JCTree.JCMethodDecl) (newClass.def.defs.get(0)));

                    var newVar = new Symbol.VarSymbol(
                        0, names.fromString("arg1"),
                        new Type.ArrayType(
                            new Type.ClassType(Type.noType, List.nil(), symbols.clColumn),
                            symtab.arrayClass
                        ),
                        constructor.sym
                    );

                    ((Type.MethodType) newClass.constructor.type).argtypes =
                        ((Type.MethodType) newClass.constructor.type).argtypes.append(newVar.type);

                    constructor.params = constructor.params.append(maker.VarDef(newVar, null));
                    constructor.sym.params = constructor.sym.params.append(newVar);

                    var constructorType = (Type.MethodType) constructor.type;
                    constructorType.argtypes = constructorType.argtypes.append(newVar.type);

                    var superInvocation = (JCTree.JCMethodInvocation) (
                        (JCTree.JCExpressionStatement) constructor.body.stats.head
                    ).expr;

                    var superMethodType = (Type.MethodType) superInvocation.meth.type;
                    superMethodType.argtypes = superMethodType.argtypes.append(newVar.type);
                    ((JCTree.JCIdent) superInvocation.meth).sym = symbols.gridConstructor2;
                    superInvocation.args = superInvocation.args.append(maker.Ident(newVar));
                }
            }

            super.visitNewClass(newClass);
        }
    }

    class MemberRefTranslator extends TreeTranslator {

        @Override public void visitReference(JCTree.JCMemberReference tree) {
            if (tree.type.tsym == symbols.clMemberRef) {
                var memberRef = "{_memberNames: () => ['" +
                    tree.name +
                    "'], _value: (v) => v." +
                    tree.name +
                    ", _className: () => '" +
                    ((Symbol.ClassSymbol) tree.type.allparams().get(1).tsym).className() +
                    "'}";

                var jsExprCall = makeCall(
                    symbols.clJSExpression, symbols.mtJSExpressionOf, List.of(
                        maker.Literal(memberRef).setType(symbols.clString.type)
                    )
                );
                jsExprCall.varargsElement = symbols.clObject.type;

                this.result = jsExprCall;
            } else super.visitReference(tree);
        }

        @Override public void visitLambda(JCTree.JCLambda tree) {
            if (tree.type.tsym == symbols.clMemberRef) {
                var fields = memberRefExtractFields(new ArrayList<>(), tree.body);
                Collections.reverse(fields);

                var memberRef = "{_memberNames: () => " +
                    fields.stream().map(f -> "'" + f + "'")
                        .collect(Collectors.joining(", ", "[", "]")) +
                    ", _value: (v) => v." +
                    String.join(".", fields) +
                    ", _className: () => '" +
                    ((Symbol.ClassSymbol) tree.type.allparams().get(1).tsym).className() +
                    "'}";

                this.result = makeCall(
                    symbols.clJSExpression, symbols.mtJSExpressionOf, List.of(
                        maker.Literal(memberRef).setType(symbols.clString.type)
                    )
                );
            } else super.visitLambda(tree);
        }

        JCTree.JCMethodInvocation makeCall(
            Symbol self, Symbol.MethodSymbol method,
            com.sun.tools.javac.util.List<JCTree.JCExpression> args
        ) {
            var select = (self instanceof Symbol.ClassSymbol || self.isStatic())
                ? maker.Select(buildStatic(self), method)
                : maker.Select(maker.Ident(self), method);

            return maker.App(select, args);
        }

        JCTree.JCExpression buildStatic(Symbol sym) {
            return sym.owner instanceof Symbol.RootPackageSymbol
                ? maker.Ident(sym)
                : maker.Select(buildStatic(sym.owner), sym);
        }

        static java.util.List<String> memberRefExtractFields(
            java.util.List<String> acc, JCTree tree
        ) {
            if (tree instanceof JCTree.JCIdent) {
                return acc;

            } else if (tree instanceof JCTree.JCFieldAccess access) {
                acc.add(access.name.toString());
                return memberRefExtractFields(acc, access.selected);

            } else if (tree instanceof JCTree.JCMethodInvocation invoke) {
                if (invoke.meth instanceof JCTree.JCFieldAccess access)
                    if (access.sym.owner instanceof Symbol.ClassSymbol classSymbol)
                        if (classSymbol.getRecordComponents().stream()
                            .anyMatch(comp -> comp.accessor == access.sym)) {

                            acc.add(access.name.toString());
                            return memberRefExtractFields(acc, access.selected);

                        } else throw memberRefUsedIncorrect();
                    else throw memberRefUsedIncorrect();
                else throw memberRefUsedIncorrect();
            }

            throw memberRefUsedIncorrect();
        }

        static CompileException memberRefUsedIncorrect() {
            return new CompileException(CompileException.ERROR.MEMBER_REF_USED_INCORRECT, """
                MemberRef<T> usage:
                  for record fields:
                    record X(long field) {}
                    MemberRef<X, Long> ref = X::field;
                  for class fields:
                    class X { long field; }
                    MemberRef<X, Long> ref = x -> x.field;
                  for nested fields (class or record):
                    record X { long b; }
                    record Y { long a; }
                    MemberRef<Y, Long> ref = y -> y.a.b
                """
            );
        }
    }

    @Override public void apply(JCTree.JCCompilationUnit cu) {
        cu.accept(new GridColumnAutoCreator());
        cu.accept(new MemberRefTranslator());
    }
}
