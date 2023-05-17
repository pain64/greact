package jstack.greact;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import jstack.greact.html.Component0;
import jstack.greact.html.Component1;

import java.util.ArrayList;
import java.util.List;

public class ViewEntryFinder {
    final Symtab symtab;
    final Names names;
    final Types types;
    final Util util;
    final Name mountMethodName;
    final Name defaultConstructorMethodName;

    public ViewEntryFinder(Context context) {
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.util = new Util(context);
        this.symbols = new Symbols();
        this.mountMethodName = names.fromString("mount");
        this.defaultConstructorMethodName = names.fromString("<init>");
    }

    class Symbols {
        Symbol.ClassSymbol clComponent0 = util.lookupClass(Component0.class);
        Symbol.ClassSymbol clComponent1 = util.lookupClass(Component1.class);
    }
    final Symbols symbols;

    /** Gradle 7.2 cannot analyze sealed classes for incremental recompilation */
    /* sealed */ public interface ViewHolder {
        JCTree.JCMethodDecl owner();
        JCTree target();
        JCTree.JCNewClass view();
    }
    public record MountMethodViewHolder(JCTree.JCMethodDecl owner, JCTree.JCNewClass view) implements ViewHolder {
        @Override public JCTree target() {return owner;}
    }
    public record LambdaViewHolder(JCTree.JCMethodDecl owner /* mount method, or class default constructor */,
                                   JCTree.JCLambda lmb,
                                   JCTree.JCNewClass view) implements ViewHolder {
        @Override public JCTree target() {return lmb;}
    }

    public record ClassEntry(JCTree.JCClassDecl classDecl, List<ViewHolder> viewHolders) {}

    boolean isImplementsComponent(Type type) {
        Type realType = type;
        if (type.tsym != null && type.tsym.isAnonymous())
            if (type.tsym instanceof Symbol.ClassSymbol classSym)
                realType = classSym.getSuperclass();

        var ifaces = types.interfaces(realType);
        if (ifaces.stream().anyMatch(iface -> iface.tsym == symbols.clComponent0)) return true;
        if (ifaces.stream().anyMatch(iface -> iface.tsym == symbols.clComponent1)) return true;

        if (realType instanceof Type.ClassType clType)
            if (clType.supertype_field != null)
                return isImplementsComponent(clType.supertype_field);

        return false;
    }

    public List<ClassEntry> find(JCTree.JCCompilationUnit cu) {
        var found = new ArrayList<ClassEntry>();

        cu.accept(new TreeScanner() {
            ClassEntry currentClassEntry;
            JCTree.JCMethodDecl currentMethod;

            @Override public void visitClassDef(JCTree.JCClassDecl tree) {
                if(tree.sym.isAnonymous())
                    super.visitClassDef(tree);
                else {
                    var oldClassEntry = currentClassEntry;
                    currentClassEntry = new ClassEntry(tree, new ArrayList<>());
                    found.add(currentClassEntry);
                    super.visitClassDef(tree);
                    currentClassEntry = oldClassEntry;
                }
            }

            @Override public void visitMethodDef(JCTree.JCMethodDecl mt) {
                var oldMethod = currentMethod;
                currentMethod = mt;
                super.visitMethodDef(mt);
                currentMethod = oldMethod;

                if (mt.name == mountMethodName)
                    if (isImplementsComponent(mt.sym.owner.type))
                        if (mt.body.stats.last() instanceof JCTree.JCReturn ret)
                            if (ret.expr instanceof JCTree.JCNewClass newClass)
                                currentClassEntry.viewHolders.add(new MountMethodViewHolder(mt, newClass));
            }

            @Override public void visitLambda(JCTree.JCLambda lmb) {
                super.visitLambda(lmb);

                var parent = currentMethod != null ? currentMethod :
                    currentClassEntry.classDecl.defs.stream()
                        .filter(d -> d instanceof JCTree.JCMethodDecl mt &&
                            mt.name.equals(defaultConstructorMethodName))
                        .map(d -> (JCTree.JCMethodDecl) d)
                        .findFirst().orElseThrow(() ->
                            new IllegalStateException("unreachable: cannot find default constructor"));

                if (lmb.type.tsym == symbols.clComponent0 || lmb.type.tsym == symbols.clComponent1)
                    if (lmb.body instanceof JCTree.JCExpression expr) {
                        if (expr instanceof JCTree.JCNewClass newClass)
                            currentClassEntry.viewHolders.add(new LambdaViewHolder(parent, lmb, newClass));
                    } else if (lmb.body instanceof JCTree.JCBlock block)
                        if (block.stats.last() instanceof JCTree.JCReturn ret)
                            if (ret.expr instanceof JCTree.JCNewClass newClass)
                                currentClassEntry.viewHolders.add(new LambdaViewHolder(parent, lmb, newClass));
            }
        });

        return found;
    }
}
