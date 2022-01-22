package com.greact.generate2;

import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.Name;
import java.util.List;
import java.util.Map;

abstract class MethodGen extends StatementGen {
    Map<Name, List<JCTree.JCMethodDecl>> groups = null;

    void withGroups(Map<Name, List<JCTree.JCMethodDecl>> groups, Runnable action) {
        var old = groups;
        this.groups = groups;
        action.run();
        this.groups = old;
    }
}
