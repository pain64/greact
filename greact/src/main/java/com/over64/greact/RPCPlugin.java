package com.over64.greact;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greact.model.DoNotTranspile;
import com.over64.greact.rpc.RPC;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;

public class RPCPlugin {
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
        Symbol.ClassSymbol clInt = symtab.enterClass(symtab.java_base, names.fromString("java.lang.Integer"));
        Symbol.ClassSymbol clLong = symtab.enterClass(symtab.java_base, names.fromString("java.lang.Long"));
        Symbol.ClassSymbol clClass = symtab.enterClass(symtab.java_base, names.fromString("java.lang.Class"));
        Symbol.ClassSymbol clRPC = lookupClass(RPC.class.getName());
        Symbol.ClassSymbol clJsonNode = lookupClass(JsonNode.class.getName());
        Symbol.ClassSymbol clObjectMapper = lookupClass(ObjectMapper.class.getName());
        Symbol.ClassSymbol clDoNotTranspile = lookupClass(DoNotTranspile.class.getName());
        Symbol.MethodSymbol mtObjectMapperTreeToValue = lookupMember(clObjectMapper, "treeToValue");
        Symbol.ClassSymbol clList = lookupClass(java.util.List.class.getName());
        Symbol.MethodSymbol mtListGet = lookupMember(clList, "get");


        Symbol.ClassSymbol clGlobals = lookupClass("com.over64.greact.dom.Globals");
        Symbol.MethodSymbol mtDoRemoteCall = lookupMember(clGlobals, "doRemoteCall");
    }

    final Symbols symbols;

    public RPCPlugin(Context context) {
        this.context = context;
        this.env = JavacProcessingEnvironment.instance(context);
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.maker = TreeMaker.instance(context);
        this.symbols = new Symbols();
    }

    JCTree.JCExpression buildStatic(Symbol sym) {
        return sym.owner instanceof Symbol.RootPackageSymbol
            ? maker.Ident(sym)
            : maker.Select(buildStatic(sym.owner), sym);
    }

    static class IdxHolder {
        private int idx = 0;

        int inc() { return idx++; }
    }

    static class EndpointsHolder {
        List<JCTree> list = List.nil();
    }

    JCTree.JCExpression readJson(Symbol.MethodSymbol method, Symbol.VarSymbol argGson, Symbol.VarSymbol argData,
                                 int idx, Type argType) {
        var paramReadExpr = maker.App(
            maker.Select(maker.Ident(argData), symbols.mtListGet)
                .setType(new Type.MethodType(
                    List.of(new Type.JCPrimitiveType(TypeTag.INT, symbols.clInt)),
                    symbols.clJsonNode.type,
                    List.nil(),
                    method.owner.type.tsym
                )),
            List.of(maker.Literal(TypeTag.INT, idx)
                .setType(new Type.JCPrimitiveType(TypeTag.INT, symbols.clInt))));


        final Type argTypeWithPrimitive;

        if (argType instanceof Type.JCPrimitiveType primitive) {
            switch (primitive.getTag()) {
                case INT -> argTypeWithPrimitive = symbols.clInt.type;
                case LONG -> argTypeWithPrimitive = symbols.clLong.type;
                default -> throw new RuntimeException("not impl now for type " + argType);
            }
        } else if (argType.tsym == symbols.clString.type.tsym) {
            argTypeWithPrimitive = symbols.clString.type;
        } else
            argTypeWithPrimitive = argType;

        return maker.App(maker.Select(maker.Ident(argGson), symbols.mtObjectMapperTreeToValue),
            List.of(paramReadExpr, maker.ClassLiteral(argTypeWithPrimitive)));
    }

    Pair<List<JCTree.JCExpression>, JCTree.JCBlock> mapLambdaBody(
        Symbol.MethodSymbol method, Symbol.MethodSymbol endpoint, JCTree.JCLambda lambda) {

        var localVars = new ArrayList<Symbol.VarSymbol>();
        var diSymbol = lambda.params.get(0).sym;
        localVars.add(diSymbol); // di symbol
        var rpcArgs = new Object() {
            List<JCTree.JCExpression> list = List.nil();
        };
        var parsedArgs = new ArrayList<Symbol.VarSymbol>();

        lambda.body.accept(new TreeScanner() {
            @Override
            public void visitVarDef(JCTree.JCVariableDecl varDef) {
                localVars.add(varDef.sym);
            }
        });

        lambda.body.accept(new TreeTranslator() {
            @Override
            public void visitIdent(JCTree.JCIdent id) {
                this.result = id;

                if (id.sym instanceof Symbol.VarSymbol varSym) {
                    if (varSym == diSymbol) {
                        this.result = maker.Ident(endpoint.params.get(0));
                        return;
                    }

                    if (localVars.contains(varSym)) return;

//                    if (varSym instanceof Symbol.ParamSymbol ||
//                        varSym.owner == method ||
//                        varSym.owner == method.owner) {

                    rpcArgs.list = rpcArgs.list.append(maker.Ident(varSym));
                    var parsedArg = new Symbol.VarSymbol(Flags.FINAL,
                        names.fromString("$closure" + parsedArgs.size()),
                        varSym.type, method);

                    parsedArgs.add(parsedArg);
                    this.result = maker.Ident(parsedArg);
//                    }
                }
            }
        });


        final JCTree.JCBlock block;
        if (lambda.body instanceof JCTree.JCBlock bl) block = bl;
        else block = maker.Block(Flags.BLOCK, List.of(maker.Return(
            (JCTree.JCExpression) lambda.body)));

        for (var i = 0; i < parsedArgs.size(); i++) {
            var parsedArg = parsedArgs.get(i);
            block.stats = block.stats.prepend(
                maker.VarDef(parsedArg, readJson(method,
                    endpoint.params.get(1), endpoint.params.get(2), i, parsedArg.type)));
        }

        return new Pair<>(rpcArgs.list, block);
    }

    void apply(JCTree.JCCompilationUnit cu) {
        var idx = new IdxHolder();

        for (var typeDecl : cu.getTypeDecls()) {
            if (typeDecl instanceof JCTree.JCClassDecl classDecl) {
                var endpoints = new EndpointsHolder();

                typeDecl.accept(new TreeScanner() {
                    @Override
                    public void visitMethodDef(JCTree.JCMethodDecl methodDecl) {
                        methodDecl.accept(new TreeScanner() {
                            @Override
                            public void visitApply(JCTree.JCMethodInvocation invoke) {
                                super.visitApply(invoke);

                                if (invoke.meth instanceof JCTree.JCIdent id) {
                                    var epAnnotation = id.sym.getAnnotation(RPC.RPCEntryPoint.class);
                                    if (epAnnotation != null) {
                                        var nextEndpointName = "$endpoint" + idx.inc();
                                        var fullQualified = classDecl.sym.flatname + "." + nextEndpointName;
                                        var diType =
                                            ((Symbol.MethodSymbol) id.sym).params.get(0).type.allparams().get(0);
                                        var typeListOfJsonElement = new Type.ClassType(
                                            classDecl.type, List.of(symbols.clJsonNode.type),
                                            symbols.clList);


                                        var endpointSymbol = new Symbol.MethodSymbol(
                                            Flags.STATIC | Flags.PUBLIC,
                                            names.fromString(nextEndpointName),
                                            new Type.MethodType(
                                                List.of(diType, symbols.clObjectMapper.type, typeListOfJsonElement),
                                                symbols.clObject.type, List.nil(), classDecl.type.tsym),
                                            classDecl.sym);
                                        endpointSymbol.prependAttributes(
                                            List.of(new Attribute.Compound(symbols.clDoNotTranspile.type, List.nil()))
                                        );

                                        endpointSymbol.params = List.<Symbol.VarSymbol>nil()
                                            .append(new Symbol.ParamSymbol(0, names.fromString("x0"),
                                                diType, endpointSymbol))
                                            .append(new Symbol.ParamSymbol(0, names.fromString("x1"),
                                                symbols.clObjectMapper.type, endpointSymbol))
                                            .append(new Symbol.ParamSymbol(0, names.fromString("x2"),
                                                typeListOfJsonElement, endpointSymbol));

                                        classDecl.sym.members_field.enterIfAbsent(endpointSymbol);


                                        var argsAndBlock = mapLambdaBody(methodDecl.sym, endpointSymbol,
                                            (JCTree.JCLambda) invoke.args.get(0));

                                        invoke.meth = buildStatic(symbols.mtDoRemoteCall);
                                        invoke.args = List.of(
                                            maker.Literal(epAnnotation.value()).setType(symbols.clString.type),
                                            maker.Literal(fullQualified).setType(symbols.clString.type),
                                            maker.NewArray(maker.Ident(symbols.clObject), List.nil(),
                                                argsAndBlock.fst)
                                                .setType(types.makeArrayType(symbols.clObject.type)));

                                        // generate new static class


                                        var endpoint = maker.MethodDef(endpointSymbol, argsAndBlock.snd);


                                        endpoints.list = endpoints.list.prepend(endpoint);


                                        var zz = 1;
                                    }
                                }
                            }
                        });
                    }
                });

                //((JCTree.JCMethodDecl) ((JCTree.JCClassDecl) cu.defs.get(4)).defs.get(0)).body.stats
//((JCTree.JCMethodDecl) ((JCTree.JCClassDecl) cu.defs.get(4)).defs.get(3)).body.stats

                classDecl.defs = classDecl.defs.prependList(endpoints.list);
                var tt = 1;
            }
        }

    }
}
