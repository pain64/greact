package jstack.greact;

import com.sun.tools.javac.tree.JCTree;

public class GReactCompileException extends RuntimeException {
    final JCTree tree;
    final String message;

    public GReactCompileException(JCTree tree, String message) {
        this.tree = tree;
        this.message = message;
    }
}
