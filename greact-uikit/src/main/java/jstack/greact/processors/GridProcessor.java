package jstack.greact.processors;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
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
import jstack.greact.uikit.Column;
import jstack.greact.uikit.Grid;
import jstack.jscripter.transpiler.generate.util.CompileException;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.model.MemberRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GridProcessor implements AstProcessor {
    private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\$\\d+)");
    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("_");
    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(\\p{Upper})");
    private Util util;
    private Name constructorName;

    class Symbols {
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

    @Override
    public void init(Context context) {
        this.util = new Util(context);
        this.constructorName = Names.instance(context).fromString("<init>");
        this.maker = TreeMaker.instance(context);
        this.symbols = new Symbols();
        this.names = new Names(context);
        this.symtab = Symtab.instance(context);
    }

    @Override
    public void apply(JCTree.JCCompilationUnit cu) {
        cu.accept(new TreeScanner() {
            @Override
            public void visitNewClass(JCTree.JCNewClass newClass) {
                if (newClass.clazz.type != null && newClass.clazz.type.tsym == symbols.clGrid && newClass.args.size() == 1) {
                    if (newClass.args.head.type instanceof Type.ArrayType arrayType) {
                        var typeElem = arrayType.elemtype;
                        if (typeElem.tsym instanceof Symbol.ClassSymbol elemClass) {
                            var recordComp = elemClass.getRecordComponents();
                            var args = new ArrayList<JCTree.JCExpression>();

                            for (Symbol.RecordComponent recordComponent : recordComp) {
                                var columnName = recordComponent.name.toString();

                                var snakeCaseMatcher = SNAKE_CASE_PATTERN.matcher(columnName);
                                columnName = snakeCaseMatcher.replaceAll(x -> " ");

                                var camelCaseMatcher = CAMEL_CASE_PATTERN.matcher(columnName);
                                columnName = camelCaseMatcher.replaceAll(x -> " " + x.group().toLowerCase());

                                var digitMatcher = DIGIT_PATTERN.matcher(columnName);
                                columnName = digitMatcher.replaceAll(x ->
                                        String.valueOf((char) Integer.parseInt(x.group().substring(1))));

                                columnName = String.valueOf(columnName.charAt(0)).toUpperCase() + columnName.substring(1);

                                var colType = new Type.ClassType(
                                        symbols.clColumn.type,
                                        List.of(typeElem,
                                                recordComponent.type), symbols.clColumn.type.tsym);
                                System.out.println(new Type.ClassType(symbols.clMemberRef.type, List.of(typeElem, recordComponent.type), symbols.clMemberRef).allparams());

                                var memberRefType = new Type.ClassType(newClass.type.getEnclosingType(), List.of(typeElem, recordComponent.type), symbols.clMemberRef);

                                var clazz = (JCTree.JCNewClass) maker.NewClass(maker.ClassLiteral(newClass.type.getEnclosingType()),
                                        List.nil(),
                                        maker.TypeApply(maker.Ident(symbols.clColumn), List.nil()).setType(symbols.clColumn.type),
                                        List.of(
                                                maker.Literal(columnName).setType(symbols.clString.type),
                                                maker.Reference(
                                                        MemberReferenceTree.ReferenceMode.INVOKE,
                                                        recordComponent.name,
                                                        maker.Ident(typeElem.tsym),
                                                        null).setType(memberRefType)
                                        ),
                                        null).setType(colType);
                                clazz.constructor = symbols.columnConstructor2;
                                clazz.constructorType = symbols.columnConstructor2.type;
                                args.add(clazz);
                            }

                            var symb = new Symbol.VarSymbol(Flags.FINAL,
                                    names.fromString("columns"),
                                    new Type.ArrayType(new Type.ClassType(symbols.clColumn.type,
                                            List.nil(),
                                            symbols.clColumn), symtab.arrayClass),
                                    newClass.type.getEnclosingType().tsym);

                            var construct = ((JCTree.JCMethodDecl) (newClass.def.defs.get(0)));
                            construct.params = construct.params.append(maker.VarDef(symb, null));
                            ((Symbol.MethodSymbol) newClass.constructor).params = ((Symbol.MethodSymbol) newClass.constructor).params.append(symb);


                            var array = maker.NewArray(maker.TypeApply(maker.Ident(symbols.clColumn), List.nil()).setType(symbols.clColumn.type), List.nil(), List.from(args));
                            array.setType(new Type.ArrayType(symbols.clColumn.type, symtab.arrayClass));
                            newClass.varargsElement = symbols.clColumn.type;
                            newClass.args = newClass.args.append(array);
                            System.out.println(newClass);
                        }
                    }
                }

                super.visitNewClass(newClass);
            }
        });

        cu.accept(new TreeTranslator() {
            @Override
            public void visitReference(JCTree.JCMemberReference tree) {
                if (tree.type.tsym == symbols.clMemberRef) {
                    System.out.println(tree.type.allparams().get(1).tsym);
                    var sb = "{_memberNames: () => ['" +
                            tree.name +
                            "'], _value: (v) => v." +
                            tree.name +
                            ", _className: () => '" +
                            ((Symbol.ClassSymbol) tree.type.allparams().get(1).tsym).className() +
                            "'}";

                    this.result = makeCall(symbols.clJSExpression, symbols.mtJSExpressionOf, List.of(
                            maker.Literal(sb).setType(symbols.clString.type)));
                } else super.visitReference(tree);
            }

            @Override
            public void visitLambda(JCTree.JCLambda tree) {
                if (tree.type.tsym == symbols.clMemberRef) {
                    var fields = memberRefExtractFields(new ArrayList<>(), tree.body);
                    Collections.reverse(fields);

                    var sb = "{_memberNames: () => " +
                            fields.stream().map(f -> "'" + f + "'").collect(Collectors.joining(", ", "[", "]")) +
                            ", _value: (v) => v." +
                            String.join(".", fields) +
                            ", _className: () => '" +
                            ((Symbol.ClassSymbol) tree.type.allparams().get(1).tsym).className() +
                            "'}";

                    this.result = makeCall(symbols.clJSExpression, symbols.mtJSExpressionOf, List.of(
                            maker.Literal(sb).setType(symbols.clString.type)));
                } else super.visitLambda(tree);
            }
        });
    }

    java.util.List<String> memberRefExtractFields(java.util.List<String> acc, JCTree tree) {
        if (tree instanceof JCTree.JCIdent) {
            return acc;
        } else if (tree instanceof JCTree.JCFieldAccess access) {
            acc.add(access.name.toString());
            return memberRefExtractFields(acc, access.selected);
        } else if (tree instanceof JCTree.JCMethodInvocation invoke) {
            if (invoke.meth instanceof JCTree.JCFieldAccess access)
                if (access.sym.owner instanceof Symbol.ClassSymbol classSymbol)
                    if (classSymbol.getRecordComponents().stream().anyMatch(comp -> comp.accessor == access.sym)) {
                        acc.add(access.name.toString());
                        return memberRefExtractFields(acc, access.selected);
                    } else throw memberRefUsedIncorrect();
                else throw memberRefUsedIncorrect();
            else throw memberRefUsedIncorrect();
        }

        throw memberRefUsedIncorrect();
    }

    JCTree.JCMethodInvocation makeCall(Symbol self, Symbol.MethodSymbol method, com.sun.tools.javac.util.List<JCTree.JCExpression> args) {
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

    CompileException memberRefUsedIncorrect() {
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
                    """);
    }
}
