package jstack.greact;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;
import jstack.greact.rpc.RPC;
import jstack.jscripter.transpiler.model.DoNotTranspile;
import jstack.jscripter.transpiler.model.RPCEndPoint;
import org.apache.commons.cli.CommandLine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class RPCPlugin {
    final Context context;
    final CommandLine cmd;
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
        Symbol.ClassSymbol clJsonNode = lookupClass(JsonNode.class.getName());
        Symbol.ClassSymbol clObjectMapper = lookupClass(ObjectMapper.class.getName());
        Symbol.ClassSymbol clRPCEndPoint = lookupClass(RPCEndPoint.class.getName());
        Symbol.ClassSymbol clRPC = lookupClass(RPC.class.getName());

        Symbol.MethodSymbol rpcConstructor2 = (Symbol.MethodSymbol)
            clRPC.getEnclosedElements().stream()
                .filter(member -> member.getSimpleName() == Names.instance(context).fromString("<init>"))
                .skip(1)
                .findFirst().orElseThrow(() -> new RuntimeException("unreachable"));
        Symbol.ClassSymbol doNotTranspile = lookupClass(DoNotTranspile.class.getName());
        Symbol.MethodSymbol mtObjectMapperTreeToValue = lookupMember(clObjectMapper, "treeToValue");
        Symbol.ClassSymbol clList = lookupClass(java.util.List.class.getName());
        Symbol.MethodSymbol mtListGet = lookupMember(clList, "get");

        Symbol.ClassSymbol clGlobals = lookupClass("jstack.greact.dom.Globals");
        Symbol.MethodSymbol mtDoRemoteCall = lookupMember(clGlobals, "doRemoteCall");
    }

    final Symbols symbols;
    final JCDiagnostic.Factory diagnosticsFactory;
    final Log javacLog;

    public RPCPlugin(Context context, CommandLine cmd) {
        this.context = context;
        this.cmd = cmd;
        this.env = JavacProcessingEnvironment.instance(context);
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.maker = TreeMaker.instance(context);
        this.symbols = new Symbols();
        this.javacLog = Log.instance(context);
        this.diagnosticsFactory = JCDiagnostic.Factory.instance(context);
    }

    class RPCConstructorCallDelegate extends TreeScanner {
        @Override
        public void visitMethodDef(JCTree.JCMethodDecl tree) {
            if (tree.sym.isConstructor() && tree.sym.owner instanceof Symbol.ClassSymbol classSymbol)
                if (classSymbol.getSuperclass().tsym == symbols.clRPC.type.tsym) {
                    var expr = ((JCTree.JCExpressionStatement) tree.body.stats.head).expr;
                    var superInvocation = (JCTree.JCMethodInvocation) expr;

                    var jsPackage = maker.Literal(cmd.getOptionValue("js-src-package")).setType(symbols.clString.type);

                    if (superInvocation.args.isEmpty()) {
                        try {
                            var method = CommandLine.class.getDeclaredMethod("addArg", String.class);
                            method.setAccessible(true);
                            method.invoke(cmd, !cmd.hasOption("rpc-url") ? "/" + tree.sym.owner : cmd.getOptionValue("rpc-url"));
                        } catch (NoSuchMethodException |
                                 IllegalAccessException |
                                 InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }

                        var rpcUrlLiteral = maker.Literal(cmd.getArgList().get(0)).setType(symbols.clString.type);

                        var superMethodType = (Type.MethodType) superInvocation.meth.type;
                        superMethodType.argtypes = superMethodType.argtypes.append(rpcUrlLiteral.type).append(jsPackage.type);

                        ((JCTree.JCIdent) superInvocation.meth).sym = symbols.rpcConstructor2;
                        superInvocation.args = superInvocation.args.append(rpcUrlLiteral).append(jsPackage);
                    }
                }

            super.visitMethodDef(tree);
        }
    }

    JCTree.JCExpression buildStatic(Symbol sym) {
        return sym.owner instanceof Symbol.RootPackageSymbol
            ? maker.Ident(sym)
            : maker.Select(buildStatic(sym.owner), sym);
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

        var localVars = new LinkedHashSet<Symbol.VarSymbol>();
        var lambdaCachedVars = new HashMap<Symbol.VarSymbol, JCTree.JCIdent>();
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
                varDef.sym.owner = endpoint;
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

                    if (lambdaCachedVars.containsKey(varSym)) {
                        this.result = lambdaCachedVars.get(varSym);
                        return;
                    }


                    rpcArgs.list = rpcArgs.list.append(maker.Ident(varSym));
                    var parsedArg = new Symbol.VarSymbol(Flags.FINAL,
                        names.fromString("$closure" + parsedArgs.size()),
                        varSym.type, method);

                    var parsedId = maker.Ident(parsedArg);

                    lambdaCachedVars.put(varSym, parsedId);
                    parsedArgs.add(parsedArg);

                    this.result = parsedId;
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
        cu.accept(new RPCConstructorCallDelegate());
        cu.accept(new TreeScanner() {
            JCTree.JCClassDecl classDecl;

            void withNewClassDecl(JCTree.JCClassDecl newDecl, Runnable in) {
                JCTree.JCClassDecl old = classDecl;
                classDecl = newDecl;
                in.run();
                classDecl = old;
            }

            @Override public void visitClassDef(JCTree.JCClassDecl tree) {
                if (!tree.sym.isStatic()) classDecl = tree;
                withNewClassDecl(tree, () -> super.visitClassDef(tree));
            }

            @Override
            public void visitMethodDef(JCTree.JCMethodDecl methodDecl) {
                methodDecl.accept(new TreeScanner() {
                    @Override public void visitApply(JCTree.JCMethodInvocation invoke) {
                        super.visitApply(invoke);
                        if (invoke.meth instanceof JCTree.JCIdent id) {
                            var superclass = ((Symbol.ClassSymbol) id.sym.owner).getSuperclass();

                            if (superclass != null &&
                                superclass.tsym == symbols.clRPC.type.tsym &&
                                id.name.toString().equals("server")
                            ) {
                                var diagnostic = diagnosticsFactory.create(
                                    new DiagnosticSource(cu.getSourceFile(), javacLog),
                                    new JCDiagnostic.SimpleDiagnosticPosition(id.pos),
                                    JCDiagnostic.DiagnosticInfo.of(
                                        JCDiagnostic.DiagnosticType.NOTE, "", ""
                                    )
                                );

                                var nextEndpointName = "$endpoint" + "_line" + diagnostic.getLineNumber() + "_col" + diagnostic.getColumnNumber();
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
                                    List.of(
                                        new Attribute.Compound(symbols.clRPCEndPoint.type, List.nil()),
                                        new Attribute.Compound(symbols.doNotTranspile.type, List.nil())
                                    )
                                );

                                endpointSymbol.params = List.<Symbol.VarSymbol>nil()
                                    .append(new Symbol.ParamSymbol(0, names.fromString("x0"),
                                        diType, endpointSymbol))
                                    .append(new Symbol.ParamSymbol(0, names.fromString("x1"),
                                        symbols.clObjectMapper.type, endpointSymbol))
                                    .append(new Symbol.ParamSymbol(0, names.fromString("x2"),
                                        typeListOfJsonElement, endpointSymbol));

                                classDecl.sym.members_field.enterIfAbsent(endpointSymbol);

                                var rpcInvocation = invoke.args.get(0);
                                final Pair<List<JCTree.JCExpression>, JCTree.JCBlock> argsAndBlock;

                                if (rpcInvocation instanceof JCTree.JCMemberReference mRef) {
                                    var methodSymbol = (Symbol.MethodSymbol) mRef.sym;
                                    argsAndBlock = Pair.of(
                                        List.nil(),
                                        maker.Block(Flags.BLOCK, List.of(
                                            maker.Return(maker.Apply(
                                                List.nil(),
                                                maker.Select(
                                                    maker.Ident(endpointSymbol.params.get(0)),
                                                    methodSymbol
                                                ),
                                                List.nil()
                                            ).setType(methodSymbol.getReturnType()))
                                        ))
                                    );
                                } else if (rpcInvocation instanceof JCTree.JCLambda lmb)
                                    argsAndBlock = mapLambdaBody(methodDecl.sym, endpointSymbol, lmb);
                                else throw new RuntimeException(
                                        "unexpected expression for rpc invocation: " + rpcInvocation
                                    );

                                invoke.meth = buildStatic(symbols.mtDoRemoteCall);
                                invoke.varargsElement = symbols.clObject.type;

                                invoke.args = List.of(
                                    maker.Literal(cmd.getArgList().get(0)).setType(symbols.clString.type),
                                    maker.Literal(fullQualified).setType(symbols.clString.type));
                                invoke.args = invoke.args.appendList(argsAndBlock.fst);

                                // generate new static class

                                var endpoint = maker.MethodDef(endpointSymbol, argsAndBlock.snd);
                                classDecl.defs = classDecl.defs.prepend(endpoint);
                            }
                        }
                    }
                });
            }
        });
    }
}
