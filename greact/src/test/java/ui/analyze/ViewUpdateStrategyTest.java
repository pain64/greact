package ui.analyze;

import jstack.greact.EffectCallFinder;
import jstack.greact.Util;
import jstack.greact.ViewEntryFinder;
import jstack.greact.ViewUpdateStrategy;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import jstack.greact.html.slot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.AnalyzeAssertionsCompiler.CompilerAssertion;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static util.AnalyzeAssertionsCompiler.withAssert;

public class ViewUpdateStrategyTest {
    public record TNode(
        String self,
        TreeSet<String> dependsSelf,
        TreeSet<String> dependsTransitive,
        List<TNode> children) implements Serializable {}

    static TNode nodeToTestNode(ViewUpdateStrategy.Node node) {
        return new TNode(node.self().clazz.toString(),
            node.dependsSelf().stream().map(Symbol.VarSymbol::toString)
                .collect(Collectors.toCollection(TreeSet::new)),
            node.dependsConditional().stream().map(Symbol.VarSymbol::toString)
                .collect(Collectors.toCollection(TreeSet::new)),
            node.children().stream().map(ViewUpdateStrategyTest::nodeToTestNode).toList());
    }

    static TreeSet<String> treeSet(String... elements) {
        var ts = new TreeSet<String>();
        Collections.addAll(ts, elements);
        return ts;
    }

    static class AssertNodeTree extends CompilerAssertion<TNode> {
        @Override public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, TNode tRoot) {
            var util = new Util(ctx);
            var view = new ViewEntryFinder(ctx).find(cu)
                .get(0).viewHolders().get(0).view();
            var effectsMap = new EffectCallFinder(ctx).find(cu);
            var effectedVars = effectsMap.values().stream()
                .flatMap(Collection::stream)
                .flatMap(ef -> ef.effected().stream())
                .collect(Collectors.toSet());
            var strategy = new ViewUpdateStrategy();
            var root = strategy.buildTree(view, effectedVars,
                util.lookupClass(slot.class));

            Assertions.assertEquals(
                tRoot.toString(),
                ViewUpdateStrategyTest.nodeToTestNode(root).toString());
        }
    }

    record NodesForVars(List<String> vars, List<String> nodes) implements Serializable {}

    static class AssertNodesForUpdate extends CompilerAssertion<NodesForVars[]> {
        @Override public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, NodesForVars[] cases) {
            var util = new Util(ctx);
            var view = new ViewEntryFinder(ctx).find(cu)
                .get(0).viewHolders().get(0).view();

            var effectsMap = new EffectCallFinder(ctx).find(cu);
            var effectedVars = effectsMap.values().stream()
                .flatMap(Collection::stream)
                .flatMap(ef -> ef.effected().stream())
                .collect(Collectors.toSet());
            var strategy = new ViewUpdateStrategy();
            var root = strategy.buildTree(view, effectedVars,
                util.lookupClass(slot.class));

            for (var tc : cases) {
                var changed = effectedVars.stream()
                    .filter(ev -> tc.vars.stream().anyMatch(tcv -> ev.toString().equals(tcv)))
                    .collect(Collectors.toSet());

                Assertions.assertEquals(
                    tc.nodes,
                    strategy.findNodesForUpdate(root, changed).stream()
                        .map(n -> n.self().clazz.toString()).toList(),
                    () -> "for" + tc.vars);
            }
        }
    }

    @Test void simple_unconditional_all() {
        withAssert(AssertNodeTree.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    @Override public div render() {
                        return new div() {{
                           new div() {{
                             new h1();
                           }};
                           new h2();
                        }};
                    }
                }""",
            new TNode("div", treeSet(), treeSet(), List.of(
                new TNode("div", treeSet(), treeSet(), List.of(
                    new TNode("h1", treeSet(), treeSet(), List.of()))),
                new TNode("h2", treeSet(), treeSet(), List.of()))));
    }

    @Test void conditional_if() {
        withAssert(AssertNodeTree.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    record AAA(int field){};
                    boolean cond = false;
                    @Override public div render() {
                        return new div() {{
                           if(cond) new div();
                           new h2();
                        }};
                    }
                }""",
            new TNode("div", treeSet(), treeSet(), List.of(
                new TNode("h2", treeSet(), treeSet(), List.of()))));
    }

    @Test void effected_vars_for_transitive_and_conditional() {
        withAssert(AssertNodeTree.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    boolean cond = false;
                    boolean eA = false;
                    boolean eB = false;
                    boolean eC = false;
                    boolean eD = false;

                    @Override public div render() {
                        return new div() {{
                           className = "cl" + eA;
                           new h3() {{
                             if(cond) new h1();
                           }};
                           new h2() {{
                             className = "cl" + eB;
                             new a() {{
                               className = "cl" + eC;
                             }};
                             for(var i = 0; i < 10; i++)
                               new span() {{
                                 className = "cl" + eD;
                               }};
                             new h4() {{
                               className = "cl" + eC;
                             }};
                           }};

                           onclick = ev -> {
                             effect(eA = !eA);
                             effect(eB = !eB);
                             effect(eC = !eC);
                             effect(eD = !eD);
                           };
                        }};
                    }
                }""",
            new TNode("div", treeSet("eA"), treeSet(), List.of(
                new TNode("h3", treeSet(), treeSet(), List.of()),
                new TNode("h2", treeSet("eB"), treeSet("eD"), List.of(
                    new TNode("a", treeSet("eC"), treeSet(), List.of()),
                    new TNode("h4", treeSet("eC"), treeSet(), List.of()))))));
    }

    @Test void find_views_for_update() {
        withAssert(AssertNodesForUpdate.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    boolean cond = false;
                    boolean eA = false;
                    boolean eB = false;
                    boolean eC = false;
                    boolean eD = false;

                    @Override public div render() {
                        return new div() {{
                           className = "cl" + eA;
                           new h3() {{
                             if(cond) new h1();
                           }};
                           new h2() {{
                             className = "cl" + eB;
                             new a() {{
                               className = "cl" + eC;
                             }};
                             for(var i = 0; i < 10; i++)
                               new span() {{
                                 className = "cl" + eD;
                               }};
                             new h4() {{
                               className = "cl" + eC;
                             }};
                           }};

                           onclick = ev -> {
                             effect(eA = !eA);
                             effect(eB = !eB);
                             effect(eC = !eC);
                             effect(eD = !eD);
                           };
                        }};
                    }
                }""",
            new NodesForVars[]{
                new NodesForVars(List.of(), List.of()),
                new NodesForVars(List.of("eA"), List.of("div")),
                new NodesForVars(List.of("eB"), List.of("h2")),
                new NodesForVars(List.of("eC"), List.of("a", "h4")),
                new NodesForVars(List.of("eD"), List.of("h2")),
                new NodesForVars(List.of("eB", "eC"), List.of("h2")),
                new NodesForVars(List.of("eA", "eB", "eC", "eD"), List.of("div")),
            });
    }

    @Test void find_view_for_update_slot() {
        withAssert(AssertNodesForUpdate.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    String text = "hello";
                    Component1<div, String> slotValue = null;

                    @Override public div render() {
                        return new div() {{
                           new slot<>(slotValue, text);

                           onclick = ev -> {
                             effect(text);
                           };
                        }};
                    }
                }""",
            new NodesForVars[] {
                new NodesForVars(List.of("text"), List.of("div")),
            });
    }


    // FIXME:
    //  нужно разобраться что делать с лямбдами так как:
    //    1. если лямбда это event handler, то view не зависит от переменных, которые в лямбде используются
    //    2. если нет, то view зависит
    //    и как мы будем различать эти 2 ситуации ???
}

