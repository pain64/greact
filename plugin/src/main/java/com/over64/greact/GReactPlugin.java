package com.over64.greact;

import com.greact.TranspilerPlugin;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

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
            Symbol.ClassSymbol clString = symtab.enterClass(symtab.java_base, names.fromString("java.lang.String"));
            Symbol.ClassSymbol clGreact = lookupClass("com.over64.greact.GReact");
            Symbol.ClassSymbol clFragment = lookupClass("com.over64.greact.dom.DocumentFragment");
            Symbol.ClassSymbol clGlobals = lookupClass("com.over64.greact.dom.Globals");
            Symbol.ClassSymbol clDocument = lookupClass("com.over64.greact.dom.Document");
            Symbol.ClassSymbol clNode = lookupClass("com.over64.greact.dom.Node");
            Symbol.ClassSymbol clHtmlElement = lookupClass("com.over64.greact.dom.HtmlElement");

            Symbol.VarSymbol documentField = lookupMember(clGlobals, "document");

            Symbol.MethodSymbol mountMethod = lookupMember(clGreact, "mount");
            Symbol.MethodSymbol effectMethod = lookupMember(clGreact, "effect");
            Symbol.MethodSymbol createDocumentFragmentMethod = lookupMember(clDocument, "createDocumentFragment");
            Symbol.MethodSymbol createElementMethod = lookupMember(clDocument, "createElement");
            Symbol.MethodSymbol appendChildMethod = lookupMember(clNode, "appendChild");
        }

        Symbols symbols = new Symbols();
    }

    Ctx instance(Context context) {
        this.context = context;
        if (ctx == null) ctx = new Ctx();
        return ctx;
    }

    JCTree.JCExpression buildStatic(Ctx ctx, Symbol sym) {
        return sym.owner instanceof Symbol.RootPackageSymbol
            ? ctx.maker.Ident(sym)
            : ctx.maker.Select(buildStatic(ctx, sym.owner), sym);
    }

    JCTree.JCMethodInvocation makeCall(Symbol.VarSymbol self, Symbol.MethodSymbol method, List<JCTree.JCExpression> args) {
        var select = self.isStatic()
            ? ctx.maker.Select(buildStatic(ctx, self), method)
            : ctx.maker.Select(ctx.maker.Ident(self), method);

        return ctx.maker.App(select, args);
    }

    static class IdentTranslator extends TreeTranslator {
        final Ctx ctx;
        final Symbol.VarSymbol scope;

        IdentTranslator(Ctx ctx, Symbol.VarSymbol scope) {
            this.ctx = ctx;
            this.scope = scope;
        }

        @Override
        public void visitIdent(JCTree.JCIdent id) {
            if (ctx.types.isSubtype(scope.type, id.sym.owner.type))
                this.result = ctx.maker.Select(ctx.maker.Ident(scope), id.sym);
            else
                super.visitIdent(id);
        }
    }

    JCTree.JCStatement mapStmt(Ctx ctx, Symbol.MethodSymbol owner,
                               Symbol.VarSymbol dest, Symbol.VarSymbol scope,
                               int n, JCTree.JCStatement stmt) {
        System.out.println("map stmt:\n" + stmt);

        Function<JCTree.JCStatement, RuntimeException> unexpectedStmt = st ->
            new RuntimeException("unexpected statement " + st + "\nof kind " + st.getKind());

        if (stmt instanceof JCTree.JCExpressionStatement exprStmt) {
            var expr = exprStmt.expr;

            if (expr instanceof JCTree.JCNewClass newClassExpr)
                return ctx.maker.Block(Flags.BLOCK, mapNewClass(ctx, owner, dest, n + 1, newClassExpr));
            else if (expr instanceof JCTree.JCAssign assignExpr)
                assignExpr.accept(new IdentTranslator(ctx, scope));
            else
                throw unexpectedStmt.apply(stmt);

        } else if (stmt instanceof JCTree.JCIf ifStmt)
            ifStmt.accept(new IdentTranslator(ctx, scope) {
                @Override
                public void visitIf(JCTree.JCIf tree) {
                    tree.cond = this.translate(tree.cond);
                    this.result = tree;
                }
            });
        else if (stmt instanceof JCTree.JCSwitch switchStmt)
            switchStmt.accept(new IdentTranslator(ctx, scope) {
                @Override
                public void visitCase(JCTree.JCCase tree) {
                    tree.pats = this.translate(tree.pats);
                    if (tree.stats != null)
                        tree.stats = tree.stats.map(st -> mapStmt(ctx, owner, dest, scope, n, st));
                    this.result = tree;
                }
            });
        else if (stmt instanceof JCTree.JCWhileLoop whileStmt)
            whileStmt.accept(new IdentTranslator(ctx, scope) {
                @Override
                public void visitWhileLoop(JCTree.JCWhileLoop tree) {
                    tree.cond = this.translate(tree.cond);
                    tree.body = mapStmt(ctx, owner, dest, scope, n, tree.body);
                    this.result = tree;
                }
            });
        else if (stmt instanceof JCTree.JCForLoop forStmt)
            forStmt.accept(new IdentTranslator(ctx, scope) {
                @Override
                public void visitForLoop(JCTree.JCForLoop tree) {
                    tree.init = this.translate(tree.init);
                    tree.cond = (JCTree.JCExpression) this.translate((JCTree) tree.cond);
                    tree.step = this.translate(tree.step);
                    tree.body = mapStmt(ctx, owner, dest, scope, n, tree.body);
                    this.result = tree;
                }
            });
        else if (stmt instanceof JCTree.JCEnhancedForLoop foreachStmt)
            foreachStmt.accept(new IdentTranslator(ctx, scope) {
                @Override
                public void visitForeachLoop(JCTree.JCEnhancedForLoop tree) {
                    tree.var = this.translate(tree.var);
                    tree.expr = this.translate(tree.expr);
                    tree.body = mapStmt(ctx, owner, dest, scope, n, tree.body);
                    this.result = tree;
                }
            });
        else if (stmt instanceof JCTree.JCBlock blockStmt)
            blockStmt.stats = blockStmt.stats
                .map(st -> mapStmt(ctx, owner, dest, scope, n, st));
        else
            throw unexpectedStmt.apply(stmt);

        return stmt;
    }

    // expression
    // запретить var x = new div() {{ }}; ???
    List<JCTree.JCStatement> mapNewClass(Ctx ctx, Symbol.MethodSymbol owner,
                                         Symbol.VarSymbol dest, int n, JCTree.JCNewClass newClass) {

        newClass.type = ((Type.ClassType) newClass.type).supertype_field;

        var elVarSymbol = new Symbol.VarSymbol(Flags.HASINIT | Flags.FINAL,
            ctx.names.fromString("$el" + n), newClass.type, owner);

        var elInit = ctx.types.isSubtype(newClass.type, ctx.symbols.clHtmlElement.type)
            ? makeCall(ctx.symbols.documentField, ctx.symbols.createElementMethod,
            List.of(
                ctx.maker.Literal(newClass.type.tsym.name.toString()).setType(ctx.symbols.clString.type)))
            : newClass;

        var elDecl = ctx.maker.VarDef(elVarSymbol, elInit);

        var mappedBody = newClass.def != null ?
            newClass.def.defs.stream()
                .filter(tree ->
                    !(tree instanceof JCTree.JCMethodDecl md && md.getName().toString().equals("<init>")))
                .map(tree -> {
                    if (tree instanceof JCTree.JCStatement stmt)
                        return mapStmt(ctx, owner, dest, elVarSymbol, n, stmt);

                    throw new RuntimeException("unexpected tree " + tree);
                }).reduce(List.<JCTree.JCStatement>nil(), List::append, List::appendList)
            : List.<JCTree.JCStatement>nil();


        newClass.def = null;

        var appendElCall = makeCall(dest, ctx.symbols.appendChildMethod,
            List.of(ctx.maker.Ident(elVarSymbol)));
        appendElCall.polyKind = JCTree.JCPolyExpression.PolyKind.STANDALONE;

        return mappedBody.prepend(elDecl).append(ctx.maker.Exec(appendElCall));
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
                        // Find all GReact.effect calls
                        var effectedSymbols = new ArrayList<Symbol.VarSymbol>();

                        typeDecl.accept(new TreeScanner() {
                            @Override
                            public void visitApply(JCTree.JCMethodInvocation tree) {
                                final Symbol methodSym;
                                if (tree.meth instanceof JCTree.JCIdent ident)
                                    methodSym = ident.sym;
                                else if (tree.meth instanceof JCTree.JCFieldAccess field)
                                    methodSym = field.sym;
                                else return;

                                if (methodSym == ctx.symbols.effectMethod)
                                    effectedSymbols.add((Symbol.VarSymbol) ((JCTree.JCIdent) tree.args.get(0)).sym);
                                // assert that is class field ???

                                super.visitApply(tree);
                            }
                        });

                        var z = 1;


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


                                            // FIXME: so bad
                                            if (!methodSym.name.equals(ctx.names.fromString("mount")) ||
                                                !methodSym.owner.name.equals(ctx.symbols.clGreact.name)) return;

                                            var template = that.args.get(1);
                                            if (!(template instanceof JCTree.JCNewClass newClassTemplate))
                                                throw new RuntimeException("expected new class expression as template");

                                            // prologue
                                            var fragVarSymbol = new Symbol.VarSymbol(Flags.HASINIT | Flags.FINAL,
                                                ctx.names.fromString("$frag"), ctx.symbols.clFragment.type, methodTree.sym);

                                            var fragDecl = ctx.maker.VarDef(
                                                fragVarSymbol,
                                                makeCall(ctx.symbols.documentField, ctx.symbols.createDocumentFragmentMethod, nil()));


//                                            newClassTemplate.accept(new TreeScanner() {
//                                                @Override
//                                                public void scan(JCTree tree) {
//                                                    if (tree != null) {
//                                                        if (tree instanceof JCTree.JCStatement stmt) {
//                                                            System.out.println("print stmt(" + stmt.getKind() + "):" + stmt);
//                                                            stmt.accept(this);
//                                                        } else
//                                                            super.scan(tree);
//                                                    }
//                                                }
//                                            });

                                            var statements = mapNewClass(ctx, methodTree.sym, fragVarSymbol, 0, newClassTemplate);


                                            // epilogue
                                            var appendCall = makeCall(
                                                (Symbol.VarSymbol) ((JCTree.JCIdent) that.args.get(0)).sym, // FIXME
                                                ctx.symbols.appendChildMethod,
                                                List.of(ctx.maker.Ident(fragVarSymbol)));

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