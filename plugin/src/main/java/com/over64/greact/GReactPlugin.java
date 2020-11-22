package com.over64.greact;

import com.greact.TranspilerPlugin;
import com.greact.generate.util.Overloads;
import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sun.tools.javac.util.List.nil;


public class GReactPlugin implements Plugin {

    public static final String NAME = "GReact";

    @Override
    public String getName() {
        return NAME;
    }


    Context context;
    Ctx ctx;

    class Ctx {

        Symbol.ClassSymbol lookupClass(String name) {
            return symtab.enterClass(symtab.unnamedModule, names.fromString(name));
        }

        <T extends Symbol> T lookupMember(Symbol.ClassSymbol from, String name) {
            @SuppressWarnings("unchecked")
            var res = (T) from.getEnclosedElements().stream()
                .filter(el -> el.name.equals(names.fromString(name)))
                .findFirst().orElseThrow();
            return res;
        }

        JavacProcessingEnvironment env = JavacProcessingEnvironment.instance(context);
        Symtab symtab = Symtab.instance(context);
        Names names = Names.instance(context);
        Types types = Types.instance(context);
        TreeMaker maker = TreeMaker.instance(context);

        class Symbols {
            Symbol.ClassSymbol stringClass = symtab.enterClass(symtab.java_base, names.fromString("java.lang.String"));
            Symbol.ClassSymbol greactClass = lookupClass("com.over64.greact.GReact");
            Symbol.ClassSymbol fragmentClass = lookupClass("com.over64.greact.dom.DocumentFragment");
            Symbol.ClassSymbol globalsClass = lookupClass("com.over64.greact.dom.Globals");
            Symbol.ClassSymbol documentClass = lookupClass("com.over64.greact.dom.Document");
            Symbol.ClassSymbol nodeClass = lookupClass("com.over64.greact.dom.Node");
            Symbol.ClassSymbol htmlElementClass = lookupClass("com.over64.greact.dom.HtmlElement");

            Symbol.VarSymbol documentField = lookupMember(globalsClass, "document");

            Symbol.MethodSymbol createDocumentFragmentMethod = lookupMember(documentClass, "createDocumentFragment");
            Symbol.MethodSymbol createElement = lookupMember(documentClass, "createElement");
            Symbol.MethodSymbol appendChildMethod = lookupMember(nodeClass, "appendChild");
        }

        Symbols symbols = new Symbols();
    }

    Ctx instance(Context context) {
        this.context = context;
        if (ctx == null) ctx = new Ctx();
        return ctx;
    }


    String joinSubarray(String[] array, int to) {
        var joiner = new StringJoiner(".");
        for (var i = 0; i <= to; i++)
            joiner.add(array[i]);

        return joiner.toString();
    }

    JCTree.JCExpression recMakeSelect(Ctx ctx, String[] idents, int i) {
        return i == 0
            ? ctx.maker.Ident(ctx.symtab.enterPackage(ctx.symtab.noModule, ctx.names.fromString(idents[i])))
            : ctx.maker.Select(recMakeSelect(ctx, idents, i - 1),
            ctx.symtab.enterPackage(ctx.symtab.noModule,
                ctx.names.fromString(joinSubarray(idents, i))));
    }

    JCTree.JCExpression makeSelect(Ctx ctx, String[] idents) {
        return ctx.maker.Select(
            recMakeSelect(ctx, idents, idents.length - 2),
            ctx.symtab.enterPackage(ctx.symtab.noModule, ctx.names.fromString(joinSubarray(idents, idents.length - 1))));
    }

    void _classFieldNames(HashSet<Name> dest, Symbol.ClassSymbol symbol) {
        var superClass = symbol.getSuperclass();
        if (superClass.tsym != null)
            _classFieldNames(dest, (Symbol.ClassSymbol) superClass.tsym);

        for (var el : symbol.getEnclosedElements())
            if (el instanceof Symbol.VarSymbol)
                dest.add(el.name);
    }

    HashSet<Name> classFieldNames(Symbol.ClassSymbol symbol) {
        HashSet<Name> dest = new HashSet<>();
        _classFieldNames(dest, symbol);
        return dest;
    }

    JCTree.JCExpression mapExpression(Ctx ctx, Symbol.MethodSymbol owner,
                                      Symbol.VarSymbol dest, Symbol.VarSymbol element,
                                      int n, JCTree.JCExpression expr) {

        Function<JCTree.JCExpression, JCTree.JCExpression> exprF =
            (e) -> mapExpression(ctx, owner, dest, element, n, e);

        if (expr instanceof JCTree.JCLiteral) {

        } else if (expr instanceof JCTree.JCAssign assign) {
            assign.lhs = exprF.apply(assign.lhs);
            assign.rhs = exprF.apply(assign.rhs);
        } else if (expr instanceof JCTree.JCIdent id) {
            var fieldNames = classFieldNames((Symbol.ClassSymbol) element.type.tsym);
            if (fieldNames.contains(id.name))
                return ctx.maker.Select(ctx.maker.Ident(element), id.sym);
        } else if (expr instanceof JCTree.JCConditional ternary) {
            ternary.cond = exprF.apply(ternary.cond);
            ternary.cond = exprF.apply(ternary.truepart);
            ternary.cond = exprF.apply(ternary.falsepart);
        } else if (expr instanceof JCTree.JCUnary unary) {
            unary.arg = exprF.apply(unary.arg);
        } else if (expr instanceof JCTree.JCBinary binary) {
            binary.lhs = exprF.apply(binary.lhs);
            binary.rhs = exprF.apply(binary.rhs);
        } else if (expr instanceof JCTree.JCAssignOp compoundAssign) {
            compoundAssign.lhs = exprF.apply(compoundAssign.lhs);
            compoundAssign.rhs = exprF.apply(compoundAssign.rhs);
        } else if (expr instanceof JCTree.JCNewArray newArray) {
            if (newArray.elems != null)
                newArray.elems = newArray.elems.map(exprF);
        } else if (expr instanceof JCTree.JCArrayAccess access) {
            access.indexed = exprF.apply(access.indexed);
            access.index = exprF.apply(access.index);
        } else if (expr instanceof JCTree.JCFieldAccess field) {
            // FIXME: field.name
            field.selected = exprF.apply(field.selected);
        } else if (expr instanceof JCTree.JCTypeCast cast) {
            cast.expr = exprF.apply(cast.expr);
        } else if (expr instanceof JCTree.JCParens parens) {
            parens.expr = exprF.apply(parens.expr);
        } else if (expr instanceof JCTree.JCLambda lambda) {
            //FIXME
            //lambda.body = mapBlock(lambda.body);
        } else if (expr instanceof JCTree.JCSwitchExpression switchExpr) {
            switchExpr.selector = exprF.apply(switchExpr.selector);

            var cases = switchExpr.getCases();
            cases.forEach(caseStmt -> {
                caseStmt.pats = caseStmt.pats.map(exprF);
                caseStmt.stats = caseStmt.stats.map(s -> mapBlock(ctx, owner, dest, element, n, s));
                //caseStmt.body
            });

        } else if (expr instanceof JCTree.JCMethodInvocation call) {
            call.meth = exprF.apply(call.meth);
            call.args = call.args.map(exprF);
        } else if (expr instanceof JCTree.JCMemberReference memberRef) {
            memberRef.expr = exprF.apply(memberRef.expr);
        } else if (expr instanceof JCTree.JCInstanceOf instanceOf) {
            instanceOf.expr = exprF.apply(instanceOf.expr);
        } else if (expr instanceof JCTree.JCNewClass) {
            throw new RuntimeException("forbidden");
        }

        return expr;
    }

    JCTree.JCStatement mapBlock(Ctx ctx, Symbol.MethodSymbol owner,
                                Symbol.VarSymbol dest, Symbol.VarSymbol element,
                                int n, JCTree.JCStatement tree) {

        var list = mapStatement(ctx, owner, dest, element, n, tree);
        if (list.length() == 1) return list.head;
        else return ctx.maker.Block(Flags.BLOCK, list);
    }


    List<JCTree.JCStatement> mapStatement(Ctx ctx, Symbol.MethodSymbol owner,
                                          Symbol.VarSymbol dest, Symbol.VarSymbol element,
                                          int n, JCTree.JCStatement stmt) {

        Function<JCTree.JCExpression, JCTree.JCExpression> expr =
            (e) -> mapExpression(ctx, owner, dest, element, n, e);

        if (stmt instanceof JCTree.JCExpressionStatement exprStmt)
            if (exprStmt.expr instanceof JCTree.JCNewClass newClass)
                return mapNewClass(ctx, owner, dest, n, newClass);
            else {
                exprStmt.expr = expr.apply(exprStmt.expr);
                return List.of(exprStmt);
            }

        Function<JCTree.JCStatement, JCTree.JCStatement> block =
            (tree) -> mapBlock(ctx, owner, dest, element, n, tree);


        if (stmt instanceof JCTree.JCBreak ||
            stmt instanceof JCTree.JCContinue) {
            /* nop */
        } else if (stmt instanceof JCTree.JCReturn ret) {
            ret.expr = expr.apply(ret.getExpression());
        } else if (stmt instanceof JCTree.JCVariableDecl varDecl) {
            if (varDecl.init != null)
                varDecl.init = expr.apply(varDecl.getInitializer());
        } else if (stmt instanceof JCTree.JCIf ifStmt) {
            ifStmt.cond = expr.apply(ifStmt.cond);
            ifStmt.thenpart = block.apply(ifStmt.thenpart);
            if (ifStmt.elsepart != null)
                ifStmt.elsepart = block.apply(ifStmt.elsepart);
        } else if (stmt instanceof JCTree.JCWhileLoop whileStmt) {
            whileStmt.cond = expr.apply(whileStmt.cond);
            whileStmt.body = block.apply(whileStmt.body);
        } else if (stmt instanceof JCTree.JCDoWhileLoop doWhile) {
            doWhile.cond = expr.apply(doWhile.cond);
            doWhile.body = block.apply(doWhile.body);
        } else if (stmt instanceof JCTree.JCForLoop forStmt) {
            forStmt.init = forStmt.init.map(block);
            forStmt.step = forStmt.step.map(s -> (JCTree.JCExpressionStatement) block.apply(s));
            forStmt.cond = expr.apply(forStmt.cond);
            forStmt.body = block.apply(forStmt.body);
        } else if (stmt instanceof JCTree.JCEnhancedForLoop forEach) {
            forEach.var = (JCTree.JCVariableDecl) block.apply(forEach.var);
            forEach.expr = expr.apply(forEach.expr);
            forEach.body = block.apply(forEach.body);
        } else if (stmt instanceof JCTree.JCLabeledStatement label) {
            label.body = block.apply(label.body);
        } else if (stmt instanceof JCTree.JCSwitch switchStmt) {
            switchStmt.selector = expr.apply(switchStmt.selector);
            switchStmt.cases.forEach(caseStmt -> {
                caseStmt.pats = caseStmt.pats.map(expr);
                caseStmt.stats = caseStmt.stats.map(block);
                // FIXME: caseStmt.body
            });
        } else if (stmt instanceof JCTree.JCYield yieldExpr) {
            yieldExpr.value = expr.apply(yieldExpr.value);
        } else if (stmt instanceof JCTree.JCThrow throwStmt) {
            throwStmt.expr = expr.apply(throwStmt.expr);
        } else if (stmt instanceof JCTree.JCTry tryStmt) {
            tryStmt.catchers.forEach(catchStmt -> {
                catchStmt.param = (JCTree.JCVariableDecl) block.apply(catchStmt.param);
                catchStmt.body = (JCTree.JCBlock) block.apply(catchStmt.body);
            });
            tryStmt.body = (JCTree.JCBlock) block.apply(tryStmt.body);
            tryStmt.finalizer = (JCTree.JCBlock) block.apply(tryStmt.finalizer);
        } else if (stmt instanceof JCTree.JCBlock blockStmt) {
            blockStmt.stats = blockStmt.stats.stream()
                .map(bstmt -> mapStatement(ctx, owner, dest, element, n, bstmt))
                .reduce(List::appendList).orElseThrow();
        } else throw new RuntimeException("unexpected statement of kind: " + stmt.getKind());

        return List.of(stmt);
    }

    // expression
    // запретить var x = new div() {{ }}; ???
    List<JCTree.JCStatement> mapNewClass(Ctx ctx, Symbol.MethodSymbol owner,
                                         Symbol.VarSymbol dest, int n, JCTree.JCNewClass newClass) {

        newClass.type = ((Type.ClassType) newClass.type).supertype_field;
        var el1VarSymbol = new Symbol.VarSymbol(Flags.HASINIT | Flags.FINAL,
            ctx.names.fromString("$el" + n), newClass.type, owner);

        final JCTree.JCVariableDecl elDecl;

        if (ctx.types.isSubtype(newClass.type, ctx.symbols.htmlElementClass.type)) {
            var pkgSelect2 = makeSelect(ctx, new String[]{"com", "over64", "greact", "dom"});
            var globalsClassSelect2 = ctx.maker.Select(pkgSelect2, ctx.symbols.globalsClass);
            var documentSelect2 = ctx.maker.Select(globalsClassSelect2, ctx.symbols.documentField);
            var createElementSelect = ctx.maker.Select(documentSelect2, ctx.symbols.createElement);

            var createElementCall = ctx.maker.App(createElementSelect,
                List.of(ctx.maker.Literal(newClass.type.tsym.name.toString()).setType(ctx.symbols.stringClass.type)));
            elDecl = ctx.maker.VarDef(el1VarSymbol, createElementCall);
        } else
            elDecl = ctx.maker.VarDef(el1VarSymbol, newClass);

        var mappedBody = newClass.def != null ?
            newClass.def.defs.stream().map(tree -> {
                if (tree instanceof JCTree.JCStatement stmt)
                    return mapStatement(ctx, owner, dest, el1VarSymbol, n + 1, stmt);
                else if (tree instanceof MethodTree method && method.getName().toString().equals("<init>"))
                    return List.<JCTree.JCStatement>nil();
                else throw new RuntimeException("oops, only statements allowed, but has: " + tree);
            }).reduce(List::appendList).orElseThrow()
            : List.<JCTree.JCStatement>nil();


        newClass.def = null;

        var appendEl1Call = ctx.maker.App(
            ctx.maker.Select(ctx.maker.Ident(dest), ctx.symbols.appendChildMethod),
            com.sun.tools.javac.util.List.of(ctx.maker.Ident(el1VarSymbol)));
        appendEl1Call.polyKind = JCTree.JCPolyExpression.PolyKind.STANDALONE;

        return mappedBody.prepend(elDecl).append(ctx.maker.Exec(appendEl1Call));
    }

    @Override
    public void init(JavacTask task, String... strings) {
        var context = ((BasicJavacTask) task).getContext();

        task.addTaskListener(new TaskListener() {

            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                    var ctx = instance(context);
                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();

                    for (var typeDecl : cu.getTypeDecls()) {
                        typeDecl.accept(new TreeScanner() {
                            @Override
                            public void visitMethodDef(JCTree.JCMethodDecl methodTree) {

                                methodTree.accept(new TreeTranslator() {
                                    @Override
                                    public void visitExec(JCTree.JCExpressionStatement exec) {
                                        this.result = exec;
                                        if (exec.expr instanceof JCTree.JCMethodInvocation that) {
                                            final Symbol methodSym;
                                            if (that.meth instanceof JCTree.JCIdent ident)
                                                methodSym = ident.sym;
                                            else if (that.meth instanceof JCTree.JCFieldAccess field)
                                                methodSym = field.sym;
                                            else return;


                                            if (!methodSym.name.equals(ctx.names.fromString("mount")) ||
                                                !methodSym.owner.name.equals(ctx.symbols.greactClass.name)) return;

                                            var template = that.args.get(1);
                                            if (!(template instanceof JCTree.JCNewClass newClassTemplate))
                                                throw new RuntimeException("expected new class expression as template");

                                            // prologue
                                            var pkgSelect = makeSelect(ctx, new String[]{"com", "over64", "greact", "dom"});
                                            var globalsClassSelect = ctx.maker.Select(pkgSelect, ctx.symbols.globalsClass);
                                            var documentSelect = ctx.maker.Select(globalsClassSelect, ctx.symbols.documentField);
                                            var createDocumentFragmentSelect = ctx.maker.Select(documentSelect, ctx.symbols.createDocumentFragmentMethod);


                                            var fragVarSymbol = new Symbol.VarSymbol(Flags.HASINIT | Flags.FINAL,
                                                ctx.names.fromString("$frag"), ctx.symbols.fragmentClass.type, methodTree.sym);

                                            var fragDecl = ctx.maker.VarDef(
                                                fragVarSymbol,
                                                ctx.maker.App(
                                                    createDocumentFragmentSelect,
                                                    nil()));

                                            var statements = mapNewClass(ctx, methodTree.sym, fragVarSymbol, 0, newClassTemplate);


                                            // epilogue
                                            var fragVarIdent = ctx.maker.Ident(fragVarSymbol);
                                            var appendChildSelect = ctx.maker.Select(that.args.get(0), ctx.symbols.appendChildMethod);
                                            var appendCall = ctx.maker.App(appendChildSelect, com.sun.tools.javac.util.List.of(fragVarIdent));
                                            appendCall.polyKind = JCTree.JCPolyExpression.PolyKind.STANDALONE;


                                            this.result = ctx.maker.Block(Flags.BLOCK,
                                                statements.prepend(fragDecl).append(ctx.maker.Exec(appendCall)));

                                        }
                                    }
                                });

                                super.visitMethodDef(methodTree);
                            }
                        });
                    }

                    try {
                        var jsFile = ctx.env.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                            cu.getPackageName().toString(),
                            e.getTypeElement().getSimpleName() + ".java.patch");

                        var writer = jsFile.openWriter();
                        writer.write(cu.toString());
                        writer.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    // ((JCTree.JCMethodDecl) ((JCTree.JCClassDecl) cu.defs.get(5)).defs.get(1)).body.stats
                    var zz = 1;
                    // new class file
                }
            }
        });

        new TranspilerPlugin().init(task, strings);
    }
}