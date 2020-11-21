package com.over64.greact;

import com.greact.generate.TypeGen;
import com.greact.generate.util.JSOut;
import com.greact.generate.util.JavaStdShim;
import com.sun.source.tree.Tree;
import com.sun.source.util.*;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.StringJoiner;
import java.util.stream.Collectors;


public class GReactPlugin implements Plugin {

    public static final String NAME = "GReact";

    @Override
    public String getName() {
        return NAME;
    }

    boolean isPathEquals(JCTree tree, String[] path, int i) {
        if (i >= path.length) return false;

        var forCompare = path[path.length - i - 1];

        if (tree instanceof JCTree.JCFieldAccess field)
            return field.name.toString().equals(forCompare) &&
                isPathEquals(field.selected, path, i + 1);

        if (tree instanceof JCTree.JCIdent ident)
            return ident.name.toString().equals(forCompare) &&
                i == path.length - 1;

        throw new RuntimeException("unreachable");
    }

    boolean hasImport(JCTree.JCCompilationUnit cu, boolean isStatic, String[] path) {
        return cu.getImports().stream().anyMatch(imp ->
            imp.isStatic() == isStatic &&
                isPathEquals(imp.getQualifiedIdentifier(), path, 0));
    }


    Name importPathEntryName(JCTree entry) {
        if (entry instanceof JCTree.JCFieldAccess field) return field.name;
        if (entry instanceof JCTree.JCIdent ident) return ident.name;
        throw new RuntimeException("unreachable");
    }


    String joinSubarray(String[] array, int to) {
        var joiner = new StringJoiner(".");
        for (var i = 0; i <= to; i++)
            joiner.add(array[i]);

        return joiner.toString();
    }

    JCTree.JCExpression recMakeSelect(TreeMaker maker, Symtab symtab, Names names, String[] idents, int i) {
        return i == 0
            ? maker.Ident(symtab.enterPackage(symtab.noModule, names.fromString(idents[i])))
            : maker.Select(recMakeSelect(maker, symtab, names, idents, i - 1),
            symtab.enterPackage(symtab.noModule,
                names.fromString(joinSubarray(idents, i))));
    }

    JCTree.JCExpression makeSelect(TreeMaker maker, Symtab symtab, Names names, String[] idents) {
//        maker.Select(null, new Symbol.VarSymbol(Flags.))
        return maker.Select(
            recMakeSelect(maker, symtab, names, idents, idents.length - 2),
            symtab.enterPackage(symtab.noModule, names.fromString(joinSubarray(idents, idents.length - 1))));
    }

    @Override
    public void init(JavacTask task, String... strings) {
        var context = ((BasicJavacTask) task).getContext();

        task.addTaskListener(new TaskListener() {

            @Override
            public void finished(TaskEvent e) {
                var env = JavacProcessingEnvironment.instance(context);
                var symtab = Symtab.instance(context);
                var names = Names.instance(context);
                var types = Types.instance(context);
                var maker = TreeMaker.instance(context);

                var z = 1;


                if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                    var greactClassSymbol = symtab.enterClass(symtab.unnamedModule,
                        names.fromString("com.over64.greact.GReact"));
                    var fragmentClassSymbol = symtab.enterClass(symtab.unnamedModule,
                        names.fromString("org.over64.jscripter.std.js.DocumentFragment"));

                    var globalsClassSymbol = symtab.enterClass(symtab.unnamedModule,
                        names.fromString("org.over64.jscripter.std.js.Globals"));
                    var documentFieldSymbol = globalsClassSymbol.getEnclosedElements().stream()
                        .filter(el -> el.name.equals(names.fromString("document")))
                        .findFirst().orElseThrow();

                    var documentClassSymbol = symtab.enterClass(symtab.unnamedModule,
                        names.fromString("org.over64.jscripter.std.js.Document"));
                    var createDocumentFragmentMethodSymbol = documentClassSymbol.getEnclosedElements().stream()
                        .filter(el -> el.name.equals(names.fromString("createDocumentFragment")))
                        .findFirst().orElseThrow();

                    var nodeClassSymbol = symtab.enterClass(symtab.unnamedModule,
                        names.fromString("org.over64.jscripter.std.js.Node"));
                    var appendChildMethodSymbol = nodeClassSymbol.getEnclosedElements().stream()
                        .filter(el -> el.name.equals(names.fromString("appendChild")))
                        .findFirst().orElseThrow();

                    var htmlNativeElementsClassSymbol = symtab.enterClass(symtab.unnamedModule,
                        names.fromString("com.over64.greact.model.components.HTMLNativeElements"));

                    var htmlElementClassSymbol = symtab.enterClass(symtab.unnamedModule,
                        names.fromString("com.over64.greact.model.components.Element"));

                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();
                    var x = 1;

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


                                            if (!methodSym.name.equals(names.fromString("mount")) ||
                                                !methodSym.owner.name.equals(greactClassSymbol.name)) return;

                                            var template = that.args.get(1);
                                            if (!(template instanceof JCTree.JCNewClass newClassTemplate))
                                                throw new RuntimeException("expected new class expression as template");

                                            // prologue
                                            var pkgSelect = makeSelect(maker, symtab, names, new String[]{"org", "over64", "jscripter", "std", "js"});
                                            var globalsClassSelect = maker.Select(pkgSelect, globalsClassSymbol);
                                            var documentSelect = maker.Select(globalsClassSelect, documentFieldSymbol);
                                            var createDocumentFragmentSelect = maker.Select(documentSelect, createDocumentFragmentMethodSymbol);


                                            var fragVarSymbol = new Symbol.VarSymbol(Flags.HASINIT | Flags.FINAL,
                                                names.fromString("$frag"), fragmentClassSymbol.type, methodTree.sym);

                                            var fragDecl = maker.VarDef(
                                                fragVarSymbol,
                                                maker.App(
                                                    createDocumentFragmentSelect,
                                                    com.sun.tools.javac.util.List.nil()));

                                            // transform
                                            final List<JCTree.JCStatement> body;
                                            if (types.isSubtype(newClassTemplate.type, htmlElementClassSymbol.type)) {
                                                // if native element
                                                // var $el1 =  document.createElement(%tag_name%)
                                                //
                                                body = List.nil();
                                            } else {
                                                // if not native element
                                                newClassTemplate.type = ((Type.ClassType) newClassTemplate.type).supertype_field;
                                                newClassTemplate.def = null;
                                                var el1VarSymbol = new Symbol.VarSymbol(Flags.HASINIT | Flags.FINAL,
                                                    names.fromString("$el1"), newClassTemplate.type, methodTree.sym);
                                                var el1Decl = maker.VarDef(el1VarSymbol, newClassTemplate);
                                                //$frag.appencChild($el1)

                                                var appendEl1Call = maker.App(
                                                    maker.Select(maker.Ident(fragVarSymbol), appendChildMethodSymbol),
                                                    com.sun.tools.javac.util.List.of(maker.Ident(el1VarSymbol)));
                                                appendEl1Call.polyKind = JCTree.JCPolyExpression.PolyKind.STANDALONE;

                                                body = List.of(el1Decl, maker.Exec(appendEl1Call));
                                            }



                                            // epilogue
                                            var fragVarIdent = maker.Ident(fragVarSymbol);
                                            var appendChildSelect = maker.Select(that.args.get(0), appendChildMethodSymbol);
                                            var appendCall = maker.App(appendChildSelect, com.sun.tools.javac.util.List.of(fragVarIdent));
                                            appendCall.polyKind = JCTree.JCPolyExpression.PolyKind.STANDALONE;


                                            this.result = maker.Block(Flags.BLOCK, body.prepend(fragDecl).append(maker.Exec(appendCall)));

                                        }
                                    }
                                });

                                super.visitMethodDef(methodTree);
                            }
                        });
                    }

                    try {
                        var jsFile = env.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
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
    }
}