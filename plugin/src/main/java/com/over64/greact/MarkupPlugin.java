package com.over64.greact;

import com.over64.greact.dom.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.sun.tools.javac.util.List.nil;

public class MarkupPlugin {
    final Context context;
    final JavacProcessingEnvironment env;
    final Symtab symtab;
    final Names names;
    final Types types;
    final TreeMaker maker;

    class Symbols {
        Symbol.ClassSymbol lookupClass(String name) {
            return symtab.enterClass(symtab.unnamedModule, names.fromString(name));
        }

        <T extends Symbol> T lookupMember(Symbol.ClassSymbol from, String name) {
            @SuppressWarnings("unchecked")
            var res = (T) from.getEnclosedElements().stream()
                .filter(el -> el.name.equals(names.fromString(name)))
                .findFirst().orElseThrow(() ->
                    new RuntimeException("oops"));
            return res;
        }

        Symbol.ClassSymbol clObject = symtab.enterClass(symtab.java_base, names.fromString("java.lang.Object"));
        Symbol.ClassSymbol clString = symtab.enterClass(symtab.java_base, names.fromString("java.lang.String"));
        Symbol.ClassSymbol clComponent = lookupClass(HTMLNativeElements.Component.class.getName());
        Symbol.ClassSymbol clComponent0 = lookupClass(HTMLNativeElements.Component0.class.getName());
        Symbol.ClassSymbol clComponent1 = lookupClass(HTMLNativeElements.Component1.class.getName());
        Symbol.ClassSymbol clComponent2 = lookupClass(HTMLNativeElements.Component2.class.getName());
        Symbol.ClassSymbol clComponent3 = lookupClass(HTMLNativeElements.Component3.class.getName());
        Symbol.ClassSymbol clDocument = lookupClass(Document.class.getName());
        Symbol.ClassSymbol clNode = lookupClass(Node.class.getName());
        Symbol.ClassSymbol clHtmlElement = lookupClass(HtmlElement.class.getName());
        Symbol.ClassSymbol clViewFragment = lookupClass(Fragment.ViewFragment.class.getName());
        Symbol.ClassSymbol clSlot = lookupClass(HTMLNativeElements.slot.class.getName());

        Symbol.ClassSymbol clFragment = lookupClass(Fragment.class.getName());
        Symbol.ClassSymbol clRenderer = lookupClass(Fragment.Renderer.class.getName());
        Symbol.MethodSymbol mtFragmentOf = lookupMember(clFragment, "of");
        Symbol.MethodSymbol mtFragmentCleanup = lookupMember(clFragment, "cleanup");
        Symbol.MethodSymbol mtFragmentAppendChild = lookupMember(clFragment, "appendChild");
        Symbol.VarSymbol flFragmentRenderer = lookupMember(clFragment, "renderer");
        Symbol.MethodSymbol mtRendererRender = lookupMember(clRenderer, "render");


        Symbol.ClassSymbol clGlobals = lookupClass("com.over64.greact.dom.Globals");
        Symbol.MethodSymbol mtGReactMount = lookupMember(clGlobals, "gReactMount");
        Symbol.MethodSymbol mtGReactReturn = lookupMember(clGlobals, "gReactReturn");
        Symbol.VarSymbol documentField = lookupMember(clGlobals, "document");
        Symbol.VarSymbol flGlobalsGReactElement = lookupMember(clGlobals, "gReactElement");

        Symbol.MethodSymbol effectMethod = lookupMember(clComponent, "effect");
        Symbol.MethodSymbol createElementMethod = lookupMember(clDocument, "createElement");
        Symbol.MethodSymbol appendChildMethod = lookupMember(clNode, "appendChild");
    }

    final Symbols symbols;

    MarkupPlugin(Context context) {
        this.context = context;
        this.env = JavacProcessingEnvironment.instance(context);
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.maker = TreeMaker.instance(context);
        this.symbols = new Symbols();
    }

    static class MountCtx {
        private int n = 0;
        private int viewFragN = 0;

        final Map<Symbol.VarSymbol, List<Symbol.VarSymbol>> viewFragments;
        final Symbol.MethodSymbol owner;

        MountCtx(Map<Symbol.VarSymbol, List<Symbol.VarSymbol>> viewFragments, Symbol.MethodSymbol owner) {
            this.viewFragments = viewFragments;
            this.owner = owner;
        }

        int nextN() {
            return n++;
        }

        int nextViewFragN() {
            return viewFragN++;
        }
    }

    JCTree.JCExpression buildStatic(Symbol sym) {
        return sym.owner instanceof Symbol.RootPackageSymbol
            ? maker.Ident(sym)
            : maker.Select(buildStatic(sym.owner), sym);
    }

    JCTree.JCMethodInvocation makeCall(Symbol self, Symbol.MethodSymbol method, List<JCTree.JCExpression> args) {
        var select = (self instanceof Symbol.ClassSymbol || self.isStatic())
            ? maker.Select(buildStatic(self), method)
            : maker.Select(maker.Ident(self), method);

        return maker.App(select, args);
    }

    class IdentPatcher extends TreeTranslator {
        final Symbol.VarSymbol scope;

        IdentPatcher(Symbol.VarSymbol scope) {
            this.scope = scope;
        }

        @Override
        public void visitIdent(JCTree.JCIdent id) {
            if (id.sym.owner.type != null) {// foreach loop var
                if (scope.type.tsym == id.sym.owner.type.tsym ||
                    types.isSubtype(scope.type, id.sym.owner.type)) {
                    this.result = maker.Select(maker.Ident(scope), id.sym);
                    return;
                }

                if (id.name.equals(names.fromString("this")) &&
                    types.isSubtype(id.sym.owner.type, scope.type)) {
                    this.result = maker.Ident(scope);
                    return;
                }
            }

            super.visitIdent(id);
        }
    }

    static class EffectAnalyzer extends TreeScanner {
        final HashSet<Symbol.VarSymbol> forEffect;
        final HashSet<Symbol.VarSymbol> effected = new HashSet<>();

        EffectAnalyzer(HashSet<Symbol.VarSymbol> forEffect) {
            this.forEffect = forEffect;
        }

        @Override
        public void visitIdent(JCTree.JCIdent id) {
            if (id.sym instanceof Symbol.VarSymbol sym)
                if (forEffect.contains(sym))
                    effected.add(sym);
        }

        @Override
        public void visitLambda(JCTree.JCLambda tree) {
        }

        HashSet<Symbol.VarSymbol> apply(JCTree tree) {
            tree.accept(this);
            return effected;
        }
    }

    HashSet<Symbol.VarSymbol> findEffectedVars(HashSet<Symbol.VarSymbol> forEffect,
                                               JCTree.JCStatement stmt) {
        Function<JCTree.JCStatement, RuntimeException> unexpectedStmt = st ->
            new RuntimeException("unexpected statement " + st + "\nof kind " + st.getKind());

        if (stmt instanceof JCTree.JCExpressionStatement exprStmt) {
            var expr = exprStmt.expr;

            if (expr instanceof JCTree.JCNewClass newClass) {
                // вызывается как для native и так для не native компонентов
                var ea = new EffectAnalyzer(forEffect);
                var isCustom = !types.isSubtype(newClass.type, symbols.clHtmlElement.type);
                //if (isCustom)
                newClass.args.forEach(arg -> arg.accept(ea));

                if (newClass.def != null) {
                    newClass.def.defs.forEach(def -> {
                        if (def instanceof JCTree.JCBlock block)
                            block.stats.forEach(st -> {
                                if (st instanceof JCTree.JCExpressionStatement estmt)
                                    if (estmt.expr instanceof JCTree.JCAssign assign)
                                        assign.accept(ea);
                            });
                    });

                }

                return ea.effected;
            } else if (expr instanceof JCTree.JCAssign assignExpr)
                return new EffectAnalyzer(forEffect).apply(assignExpr);
            else
                throw unexpectedStmt.apply(stmt);

        } else if (stmt instanceof JCTree.JCIf ifStmt)
            return new EffectAnalyzer(forEffect) {
                @Override
                public void visitIf(JCTree.JCIf tree) {
                    scan(tree.cond);
                }
            }.apply(ifStmt);
        else if (stmt instanceof JCTree.JCSwitch switchStmt)
            return new EffectAnalyzer(forEffect) {
                @Override
                public void visitCase(JCTree.JCCase tree) {
                    scan(tree.pats);
                }
            }.apply(switchStmt);
        else if (stmt instanceof JCTree.JCEnhancedForLoop foreachStmt)
            return new EffectAnalyzer(forEffect) {
                @Override
                public void visitForeachLoop(JCTree.JCEnhancedForLoop tree) {
                    scan(tree.var);
                    scan(tree.expr);
                }
            }.apply(foreachStmt);
        else if (stmt instanceof JCTree.JCBreak)
            return new HashSet<>();
        else
            throw unexpectedStmt.apply(stmt);
    }

    void patchIdentifiers(Symbol.VarSymbol dest, JCTree.JCStatement stmt) {
        Function<JCTree.JCStatement, RuntimeException> unexpectedStmt = st ->
            new RuntimeException("unexpected statement " + st + "\nof kind " + st.getKind());

        if (stmt instanceof JCTree.JCExpressionStatement exprStmt) {
            var expr = exprStmt.expr;

            if (expr instanceof JCTree.JCNewClass) {
                /* NOP */
            } else if (expr instanceof JCTree.JCAssign assignExpr)
                new IdentPatcher(dest).translate(assignExpr);
            else throw unexpectedStmt.apply(stmt);

        } else if (stmt instanceof JCTree.JCIf ifStmt)
            new IdentPatcher(dest) {
                @Override
                public void visitIf(JCTree.JCIf tree) {
                    tree.cond = translate(tree.cond);
                    this.result = tree;
                }
            }.translate(ifStmt);
        else if (stmt instanceof JCTree.JCSwitch switchStmt)
            new IdentPatcher(dest) {
                @Override
                public void visitCase(JCTree.JCCase tree) {
                    tree.pats = translate(tree.pats);
                    this.result = tree;
                }
            }.translate(switchStmt);
        else if (stmt instanceof JCTree.JCEnhancedForLoop foreachStmt)
            new IdentPatcher(dest) {
                @Override
                public void visitForeachLoop(JCTree.JCEnhancedForLoop tree) {
                    tree.var = translate(tree.var);
                    tree.expr = translate(tree.expr);
                    this.result = tree;
                }
            }.translate(foreachStmt);
        else if (stmt instanceof JCTree.JCBreak) {} else
            throw unexpectedStmt.apply(stmt);
    }

    Optional<Type> componentImplOpt_(Type type) {
        return types.interfaces(type).stream()
            .filter(iface -> iface.tsym == symbols.clComponent0 ||
                iface.tsym == symbols.clComponent1 ||
                iface.tsym == symbols.clComponent2 ||
                iface.tsym == symbols.clComponent3)
            .findFirst();
    }

    Type componentImpl(Type type) {
        return componentImplOpt_(type).get();
    }

    Type popNativeElementAnonTypes(Type anon) {
        if (types.isSubtype(anon.getEnclosingType(), symbols.clHtmlElement.type))
            return popNativeElementAnonTypes(anon.getEnclosingType());
        return anon.getEnclosingType();
    }

    JCTree.JCStatement mapStmt(MountCtx mctx,
                               HashSet<Symbol.VarSymbol> forEffect,
                               Symbol.VarSymbol dest, JCTree.JCStatement stmt) {

        Function<JCTree.JCStatement, RuntimeException> unexpectedStmt = st ->
            new RuntimeException("unexpected statement " + st + "\nof kind " + st.getKind());

        var effected = findEffectedVars(forEffect, stmt);
        var unhandledEffects = new HashSet<>(forEffect);
        unhandledEffects.removeAll(effected);
        patchIdentifiers(dest, stmt);


        BiFunction<JCTree.JCStatement, Symbol.VarSymbol, JCTree.JCStatement>
            patchStatements = (st, nextDest) -> {
            if (st instanceof JCTree.JCExpressionStatement exprStmt) {
                var expr = exprStmt.expr;

                if (expr instanceof JCTree.JCNewClass newClass) {
                    var compType = newClass.def == null // anon inner class
                        ? newClass.type
                        : ((Type.ClassType) newClass.type).supertype_field;

                    var nextN = mctx.nextN();

                    if (compType.tsym == symbols.clSlot) {
                        // FIXME: dedup code
                        var htmlElementType = compType.allparams().get(0);
                        var elInit = makeCall(symbols.documentField, symbols.createElementMethod, List.of(
                            maker.Literal(htmlElementType.tsym.name.toString()).setType(symbols.clString.type)));
                        var elVarSymbol = new Symbol.VarSymbol(Flags.HASINIT | Flags.FINAL,
                            names.fromString("$el" + nextN), htmlElementType, mctx.owner);
                        var elDecl = maker.VarDef(elVarSymbol, elInit);

                        var appendMethod = nextDest.type.tsym == symbols.clFragment ?
                            symbols.mtFragmentAppendChild : symbols.appendChildMethod;
                        var appendElCall = makeCall(nextDest, appendMethod,
                            List.of(maker.Ident(elVarSymbol)));
                        appendElCall.polyKind = JCTree.JCPolyExpression.PolyKind.STANDALONE;

                        return maker.Block(Flags.BLOCK, List.of(
                            elDecl,
                            maker.Exec(makeCall(symbols.clGlobals, symbols.mtGReactMount, List.of(
                                maker.Ident(elVarSymbol), newClass.args.get(0),
                                maker.NewArray(maker.Ident(symbols.clObject), List.nil(),
                                    newClass.args.tail != null ? newClass.args.tail : List.nil())
                                    .setType(types.makeArrayType(symbols.clObject.type))
                            ))),
                            maker.Exec(appendElCall)));
                    } else if (types.isSubtype(compType, symbols.clHtmlElement.type) ||
                        componentImplOpt_(compType).isPresent()) {

                        var isCustom = !types.isSubtype(compType, symbols.clHtmlElement.type);
                        var htmlElementType = isCustom
                            ? componentImpl(compType).allparams().get(0)
                            : compType;

                        if (isCustom) {
                            if (newClass.def != null) { // anon inner class
                                var ct = ((Type.ClassType) newClass.type);
                                ct.setEnclosingType(popNativeElementAnonTypes(newClass.type));
                            }
                        } else {
                            if (newClass.def != null)  // anon inner class
                                newClass.type = ((Type.ClassType) newClass.type).supertype_field;

                        }

                        var elInit = makeCall(symbols.documentField, symbols.createElementMethod, List.of(
                            maker.Literal(htmlElementType.tsym.name.toString()).setType(symbols.clString.type)));
                        var elVarSymbol = new Symbol.VarSymbol(Flags.HASINIT | Flags.FINAL,
                            names.fromString("$el" + nextN), htmlElementType, mctx.owner);
                        var elDecl = maker.VarDef(elVarSymbol, elInit);

                        var mapToVarSymbol = isCustom
                            ? new Symbol.VarSymbol(Flags.HASINIT | Flags.FINAL,
                            names.fromString("$comp" + nextN), newClass.type, mctx.owner)
                            : elVarSymbol;

                        var appendMethod = nextDest.type.tsym == symbols.clFragment ?
                            symbols.mtFragmentAppendChild : symbols.appendChildMethod;
                        var appendElCall = makeCall(nextDest, appendMethod,
                            List.of(maker.Ident(elVarSymbol)));
                        appendElCall.polyKind = JCTree.JCPolyExpression.PolyKind.STANDALONE;

                        var prologue = isCustom
                            ? List.<JCTree.JCStatement>of(
                            elDecl, maker.VarDef(mapToVarSymbol, newClass))
                            : List.<JCTree.JCStatement>of(
                            elDecl, mapNewClass(mctx, unhandledEffects, mapToVarSymbol, newClass));
                        var epilogue = isCustom
                            ? List.<JCTree.JCStatement>of(
                            maker.Exec(makeCall(symbols.clGlobals, symbols.mtGReactMount, List.of(
                                maker.Ident(elVarSymbol), maker.Ident(mapToVarSymbol),
                                maker.NewArray(maker.Ident(symbols.clObject), List.nil(), List.nil())
                                    .setType(types.makeArrayType(symbols.clObject.type))
                            ))))
                            : List.<JCTree.JCStatement>nil();

                        return maker.Block(Flags.BLOCK, prologue
                            .appendList(epilogue)
                            .append(maker.Exec(appendElCall)));
                    }

                } else if (expr instanceof JCTree.JCAssign assignExpr) {
                    /* nop */
                } else
                    throw unexpectedStmt.apply(st);

            } else if (st instanceof JCTree.JCIf ifStmt) {
                if (ifStmt.thenpart instanceof JCTree.JCBlock block)
                    block.stats = block.stats.map(bst -> mapStmt(mctx, unhandledEffects, nextDest, bst));
                else
                    ifStmt.thenpart = mapStmt(mctx, unhandledEffects, nextDest, ifStmt.thenpart);

                if (ifStmt.elsepart != null)
                    if (ifStmt.elsepart instanceof JCTree.JCBlock block)
                        block.stats = block.stats.map(bst -> mapStmt(mctx, unhandledEffects, nextDest, bst));
                    else
                        ifStmt.elsepart = mapStmt(mctx, unhandledEffects, nextDest, ifStmt.elsepart);
            } else if (st instanceof JCTree.JCSwitch switchStmt)
                switchStmt.cases.forEach(tree -> {
                    if (tree.stats != null)
                        tree.stats = tree.stats.map(s -> mapStmt(mctx, unhandledEffects, nextDest, s));
                });
            else if (st instanceof JCTree.JCEnhancedForLoop foreachStmt) {
                if (foreachStmt.body instanceof JCTree.JCBlock block)
                    block.stats = block.stats.map(bst -> mapStmt(mctx, unhandledEffects, nextDest, bst));
                else foreachStmt.body = mapStmt(mctx, unhandledEffects, nextDest, foreachStmt.body);
            } else if (stmt instanceof JCTree.JCBreak) {
                /* nop */
            } else
                throw unexpectedStmt.apply(st);

            return st;
        };


        if (effected.isEmpty()) {
            return patchStatements.apply(stmt, dest);
        } else {
            var fragN = mctx.nextViewFragN();
            var viewRendererVar = new Symbol.VarSymbol(Flags.PRIVATE,
                names.fromString("$viewFrag" + fragN),
                symbols.clFragment.type,
                mctx.owner.owner);

            effected.forEach(eVar -> {
                var old = mctx.viewFragments.computeIfAbsent(eVar, k -> List.nil());
                mctx.viewFragments.put(eVar, old.append(viewRendererVar));
            });

            var cleanupCall = maker.Exec(
                makeCall(viewRendererVar, symbols.mtFragmentCleanup, List.nil()));

            var patchedStmt = patchStatements.apply(stmt, viewRendererVar);
            var stmtBody = patchedStmt instanceof JCTree.JCBlock block ?
                block.stats : List.of(patchedStmt);

            var lmb = maker.Lambda(
                nil(),
                maker.Block(Flags.BLOCK, stmtBody.prepend(cleanupCall)))
                .setType(symbols.clRenderer.type);
            lmb.target = symbols.clRenderer.type;
            lmb.polyKind = JCTree.JCPolyExpression.PolyKind.POLY;
            lmb.paramKind = JCTree.JCLambda.ParameterKind.EXPLICIT;

            var newFragment = makeCall(symbols.clFragment, symbols.mtFragmentOf,
                List.of(lmb, maker.Ident(dest)));

            return maker.Exec(maker.App(
                maker.Select(
                    maker.Select(
                        maker.Parens(
                            maker.Assign(
                                maker.Ident(viewRendererVar),
                                newFragment
                            )).setType(symbols.clViewFragment.type),
                        symbols.flFragmentRenderer),
                    symbols.mtRendererRender)));
        }
    }

    JCTree.JCBlock mapNewClass(MountCtx mctx, HashSet<Symbol.VarSymbol> forEffect,
                               Symbol.VarSymbol elVarSymbol, JCTree.JCNewClass newClass) {

        if (newClass.def != null) {
            // FIXME: don't map newClass for custom components
            var constructorSymbol = newClass.type.tsym.getEnclosedElements().stream()
                .filter(el -> {
                    if (el instanceof Symbol.MethodSymbol) {
                        var mconsType = (Type.MethodType) newClass.constructorType;
                        var mcandidateType = (Type.MethodType) types.erasure(el.type);

                        if (!types.isAssignable(mconsType.restype, mcandidateType.restype)) return false;
                        if (mconsType.argtypes.length() != mcandidateType.argtypes.length()) return false;

                        for (var i = 0; i < mconsType.argtypes.length(); i++)
                            if (!types.isAssignable(mconsType.argtypes.get(i), mcandidateType.argtypes.get(i)))
                                return false;

                        return true;
                    } else
                        return false;
                })
                .findFirst().orElseThrow(() ->
                    new RuntimeException("oops"));
            newClass.constructorType = constructorSymbol.type;
            newClass.constructor = constructorSymbol;
        }

        var mappedArgs = IntStream.range(0, newClass.args.length())
            .mapToObj(i -> {
                var arg = newClass.args.get(i);
                var argAnnotation = ((Symbol.MethodSymbol) newClass.constructor)
                    .params.get(i)
                    .getAnnotation(HTMLNativeElements.DomProperty.class);

//                    System.out.println("search dom property of " + argAnnotation.value());
//                    System.out.println("search at type " + (Symbol.ClassSymbol) ((Type.ClassType) newClass.type)
//                    .supertype_field.tsym);

                var argSymbol = symbols.lookupMember(
                    (Symbol.ClassSymbol) ((Type.ClassType) newClass.type).supertype_field.tsym,
                    argAnnotation.value());

                return maker.Exec(
                    maker.Assign(
                        maker.Select(
                            maker.Ident(elVarSymbol),
                            argSymbol),
                        arg));
            }).reduce(List.<JCTree.JCStatement>nil(), List::append, List::appendList);

        var blockBody = newClass.def != null ?
            newClass.def.defs.stream()
                .filter(tree -> tree instanceof JCTree.JCBlock)
                .map(block -> ((JCTree.JCBlock) block).stats)
                .findFirst().orElseThrow()
            : List.<JCTree.JCStatement>nil();

        var mapped = mappedArgs.appendList(blockBody).stream()
            .flatMap(tree -> {
                if (tree instanceof JCTree.JCStatement stmt) {
                    var mappedStmt = mapStmt(mctx, forEffect, elVarSymbol, stmt);
                    if (mappedStmt instanceof JCTree.JCBlock block)
                        return block.stats.stream();
                    else return Stream.of(mappedStmt);
                }

                throw new RuntimeException("unexpected tree " + tree);
            }).reduce(List.<JCTree.JCStatement>nil(), List::append, List::appendList);

        // newClass.def = null;
        return maker.Block(Flags.BLOCK, mapped);
    }

    Optional<Type> isComponent(Type type) {
        for (var iface : types.interfaces(type)) {
            if (iface.tsym == symbols.clComponent0 ||
                iface.tsym == symbols.clComponent1 ||
                iface.tsym == symbols.clComponent2 ||
                iface.tsym == symbols.clComponent3) return Optional.of(iface);

            var atInterface = isComponent(iface);
            if (atInterface.isPresent()) return atInterface;
        }

        if (type instanceof Type.ClassType clType)
            if (clType.supertype_field != null)
                return isComponent(clType.supertype_field);

        return Optional.empty();
    }


    void apply(JCTree.JCCompilationUnit cu) {
        for (var typeDecl : cu.getTypeDecls()) {
            // check that type implements Component interface
            // FIXME: нужно рекурсивно проверить у Super класса
            var componentImplOpt = isComponent(typeDecl.type);

            final Type componentImpl;
            if (componentImplOpt.isPresent()) componentImpl = componentImplOpt.get();
            else continue;

            // Find all GReact.effect calls
            var effectCalls = new HashMap<Symbol.VarSymbol, java.util.List<JCTree.JCMethodInvocation>>();

            typeDecl.accept(new TreeScanner() {
                @Override
                public void visitApply(JCTree.JCMethodInvocation tree) {
                    final Symbol methodSym;
                    if (tree.meth instanceof JCTree.JCIdent ident)
                        methodSym = ident.sym;
                    else if (tree.meth instanceof JCTree.JCFieldAccess field)
                        methodSym = field.sym;
                    else return;

                    var zz = 1;

                    if (methodSym == symbols.effectMethod) {
                        Function<JCTree.JCExpression, Symbol.VarSymbol> fetchVarSymbol = expr -> {
                            if (expr instanceof JCTree.JCIdent id)
                                return (Symbol.VarSymbol) id.sym;
                            else if (expr instanceof JCTree.JCAssign assign) {
                                if (assign.lhs instanceof JCTree.JCIdent id)
                                    return (Symbol.VarSymbol) id.sym;
                            } else if (expr instanceof JCTree.JCAssignOp op)
                                if (op.lhs instanceof JCTree.JCIdent id)
                                    return (Symbol.VarSymbol) id.sym;

                            throw new RuntimeException("""
                                for ∀ x is class field expected any of:
                                  GReact.effect(x)
                                  GReact.effect(x = expression)
                                  GReact.effect(x op= expression)""");
                        };

                        var varSym = fetchVarSymbol.apply(tree.args.get(0));
                        var list = effectCalls.computeIfAbsent(varSym, k -> new ArrayList<>());
                        list.add(tree);
                    }
                    // assert that is class field ???

                    super.visitApply(tree);
                }
            });

            var z = 1;

            var viewFragments = new HashMap<Symbol.VarSymbol, List<Symbol.VarSymbol>>();
            var mappedLambda = new HashSet<JCTree.JCLambda>();

            typeDecl.accept(new TreeScanner() {
                @Override
                public void visitMethodDef(JCTree.JCMethodDecl methodTree) {
                    if (!methodTree.getName().toString().equals("mount")) return;

                    var rootElementType = componentImpl.allparams().get(0);
                    var rootVar = new Symbol.VarSymbol(Flags.FINAL | Flags.HASINIT,
                        names.fromString("$root"),
                        rootElementType,
                        methodTree.sym);
                    var elementDecl = maker.VarDef(rootVar,
                        maker.TypeCast(rootElementType,
                            maker.Select(
                                buildStatic(symbols.clGlobals),
                                symbols.flGlobalsGReactElement)));

                    methodTree.body.stats = methodTree.body.stats.prepend(elementDecl);


                    methodTree.accept(new TreeTranslator() {
                        @Override
                        public void visitReturn(JCTree.JCReturn ret) {
                            this.result = ret;
                            if (ret.expr instanceof JCTree.JCNewClass that) {
                                var lmb = maker.Lambda(List.nil(), mapNewClass(
                                    new MountCtx(viewFragments, methodTree.sym),
                                    new HashSet<>(effectCalls.keySet()),
                                    rootVar, that))
                                    .setType(symbols.clRenderer.type);
                                lmb.target = symbols.clRenderer.type;
                                lmb.polyKind = JCTree.JCPolyExpression.PolyKind.POLY;
                                lmb.paramKind = JCTree.JCLambda.ParameterKind.EXPLICIT;

                                ret.expr = makeCall(symbols.clGlobals, symbols.mtGReactReturn,
                                    List.of(lmb));
                            }
                        }
                    });

                    methodTree.accept(new TreeTranslator() {
                        @Override
                        public void visitLambda(JCTree.JCLambda lmb) {
                            super.visitLambda(lmb);

                            if (mappedLambda.contains(lmb)) return;

                            if (lmb.type.tsym == symbols.clComponent0 ||
                                lmb.type.tsym == symbols.clComponent1 ||
                                lmb.type.tsym == symbols.clComponent2 ||
                                lmb.type.tsym == symbols.clComponent3) {

                                mappedLambda.add(lmb);

                                if (lmb.body instanceof JCTree.JCExpression expr) {
                                    lmb.body = maker.Block(Flags.BLOCK, List.of(
                                        maker.Return(expr)));
                                }

                                var body = (JCTree.JCBlock) lmb.body;
                                // FIXME: this code is duplicated!!!
                                var rootElementType = lmb.type.allparams().get(0);
                                var rootVar = new Symbol.VarSymbol(Flags.FINAL | Flags.HASINIT,
                                    names.fromString("$root"),
                                    rootElementType,
                                    methodTree.sym);
                                var elementDecl = maker.VarDef(rootVar,
                                    maker.TypeCast(rootElementType,
                                        maker.Select(
                                            buildStatic(symbols.clGlobals),
                                            symbols.flGlobalsGReactElement)));

                                body.stats = body.stats.prepend(elementDecl);
                                var ret = (JCTree.JCReturn) body.stats.last();

                                if (ret.expr instanceof JCTree.JCNewClass that) {
                                    var lmb2 = maker.Lambda(List.nil(), mapNewClass(
                                        new MountCtx(viewFragments, methodTree.sym),
                                        new HashSet<>(effectCalls.keySet()),
                                        rootVar, that))
                                        .setType(symbols.clRenderer.type);
                                    lmb2.target = symbols.clRenderer.type;
                                    lmb2.polyKind = JCTree.JCPolyExpression.PolyKind.POLY;
                                    lmb2.paramKind = JCTree.JCLambda.ParameterKind.EXPLICIT;

                                    ret.expr = makeCall(symbols.clGlobals, symbols.mtGReactReturn
                                        , List.of(lmb2));
                                }
                            }
                        }
                    });

                    super.visitMethodDef(methodTree);
                }
            });

            var classDecl = (JCTree.JCClassDecl) typeDecl;

            for (var list : viewFragments.values())
                for (var viewFrag : list) {
                    // also append s and e
                    classDecl.sym.members_field.enterIfAbsent(viewFrag);
                    classDecl.defs = classDecl.defs
                        .append(maker.VarDef(viewFrag, null));
                }


            effectCalls.forEach((eVar, eCalls) -> {
                var methodSym = new Symbol.MethodSymbol(
                    Flags.PRIVATE,
                    names.fromString("effect$" + eVar.name.toString()),
                    new Type.MethodType(
                        List.of(symbols.clObject.type),
                        new Type.JCVoidType(), List.nil(), classDecl.sym),
                    classDecl.sym);

                methodSym.params = List.of(new Symbol.VarSymbol(
                    0, names.fromString("x0"), symbols.clObject.type, methodSym));

                classDecl.sym.members_field.enterIfAbsent(methodSym);

                var fragmentsForVar = viewFragments.getOrDefault(eVar, List.nil());

                var method = maker.MethodDef(methodSym,
                    maker.Block(Flags.BLOCK,
                        fragmentsForVar.map(frag ->
                            maker.Exec(
                                maker.App(
                                    maker.Select(
                                        maker.Select(
                                            maker.Ident(frag), symbols.flFragmentRenderer),
                                        symbols.mtRendererRender))))));

                method.mods = maker.Modifiers(Flags.PRIVATE);

                classDecl.defs = classDecl.defs.append(method);

                eCalls.forEach(eCall -> {
                    eCall.meth = maker.Ident(methodSym);
                });
            });

            var zz = 1;
        }
    }
}
