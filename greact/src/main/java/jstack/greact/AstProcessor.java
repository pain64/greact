package jstack.greact;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

public interface AstProcessor {
    void init(Context context);
    void apply(JCTree.JCCompilationUnit cu);
}
