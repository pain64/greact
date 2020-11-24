package com.over64.greact;

import com.greact.TranspilerPlugin;
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

import javax.lang.model.element.Name;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringJoiner;
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
                if (tree instanceof JCTree.JCStatement stmt) {
                    stmt.accept(new TreeTranslator() {
                        @Override public <T extends JCTree> T translate(T tree) {
                            if (tree == null) return null;

                            if (tree instanceof JCTree.JCExpressionStatement estmt)
                                if (estmt.expr instanceof JCTree.JCNewClass _newClass)
                                    return (T) ctx.maker.Block(Flags.BLOCK, mapNewClass(ctx, owner, dest, n + 1, _newClass));

                            return super.translate(tree);
                        }

                        @Override public void visitIdent(JCTree.JCIdent id) {
                            if (ctx.types.isSubtype(el1VarSymbol.type, id.sym.owner.type))
                                this.result =  ctx.maker.Select(ctx.maker.Ident(el1VarSymbol), id.sym);
                            else
                                super.visitIdent(id);
                        }
                    });

                    return List.of(stmt);
                } else if (tree instanceof MethodTree method && method.getName().toString().equals("<init>"))
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