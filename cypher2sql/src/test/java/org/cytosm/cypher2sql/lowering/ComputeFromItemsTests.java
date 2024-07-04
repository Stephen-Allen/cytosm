package org.cytosm.cypher2sql.lowering;

import java.util.List;
import java.util.Optional;

import org.cytosm.cypher2sql.PassAvailables;
import org.cytosm.cypher2sql.cypher.ast.Statement;
import org.cytosm.cypher2sql.lowering.sqltree.ScopeSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelect;
import org.cytosm.cypher2sql.lowering.sqltree.WithSelect;
import org.cytosm.cypher2sql.lowering.sqltree.from.FromItem;
import org.cytosm.cypher2sql.lowering.typeck.VarDependencies;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 */
class ComputeFromItemsTests {

    @Test
    void computeFromItemsWithIndirectDependencies() throws Exception {
        String cypher =
                "MATCH (a)--(b)\n" +
                "MATCH (b)--(c)\n" +
                "RETURN a.firstName";
        ScopeSelect tree = computeFromItems(cypher);

        assertEquals(1, tree.ret.fromItem.size());
        assertEquals(3, tree.ret.fromItem.get(0).variables.size());

        WithSelect match0 = tree.withQueries.get(0);
        WithSelect match1 = tree.withQueries.get(1);

        SimpleSelect match0sub = (SimpleSelect) match0.subquery;
        SimpleSelect match1sub = (SimpleSelect) match1.subquery;

        assertSame(match1, tree.ret.fromItem.get(0).source);
        assertEquals(2, match1sub.fromItem.size());

        // If this throw a null pointer exception then there's a bug found.
        FromItem b = getByName(match1sub.fromItem, "b").get();
        assertSame(match0, b.source);
        assertNull(getByName(match1sub.fromItem, "c").get().source);
        assertNull(getByName(match0sub.fromItem, "a").get().source);
    }

    @Test
    void computeFromItemsOnWeirdIndirectDependencies() throws Exception {
        String cypher =
                "MATCH (a)--(b)\n" +
                "WITH a, {b:{d:b}} AS h\n" +
                "MATCH (a)--(c)\n" +
                "RETURN h.b.d.firstName";
        ScopeSelect tree = computeFromItems(cypher);

        assertEquals(1, tree.ret.fromItem.size());
    }

    @Test
    void computeFromItemsOnMergedVariablesIntoOne() throws Exception {
        // This example is problematic
        // because an AliasVar might actually be propagated through
        // two FromItem.
        String cypher =
                "MATCH (a)\n" +
                "MATCH (b)\n" +
                "WITH {a: a, b: b} AS d\n" +
                "RETURN d";
        ScopeSelect tree = computeFromItems(cypher);

        WithSelect match0 = tree.withQueries.get(0);
        WithSelect match1 = tree.withQueries.get(1);
        WithSelect with2 = tree.withQueries.get(2);

        SimpleSelect match0sub = (SimpleSelect) match0.subquery;
        SimpleSelect match1sub = (SimpleSelect) match1.subquery;
        SimpleSelect with2sub = (SimpleSelect) with2.subquery;

        // Sizes checks
        assertEquals(1, tree.ret.fromItem.size());
        assertEquals(2, with2sub.fromItem.size());
        assertEquals(1, match1sub.fromItem.size());
        assertEquals(1, match0sub.fromItem.size());

        // References checks
        assertSame(with2, tree.ret.fromItem.get(0).source);
        assertSame(match1, with2sub.fromItem.get(1).source);
        assertSame(match0, with2sub.fromItem.get(0).source);
        assertNull(match0sub.fromItem.get(0).source);
        assertNull(match1sub.fromItem.get(0).source);
    }

    @Test
    void computeFromItemsWhenInPresenceOfAliases() throws Exception {
        String cypher =
                "MATCH (a)\n" +
                "WITH a AS b\n" +
                "RETURN b.firstName";
        ScopeSelect tree = computeFromItems(cypher);


        WithSelect match0 = tree.withQueries.get(0);
        WithSelect with1 = tree.withQueries.get(1);

        SimpleSelect match0sub = (SimpleSelect) match0.subquery;
        SimpleSelect with1sub = (SimpleSelect) with1.subquery;

        assertEquals(1, tree.ret.fromItem.size());
        assertEquals(1, match0sub.fromItem.size());
        assertEquals(1, with1sub.fromItem.size());
        assertEquals(1, tree.ret.fromItem.get(0).variables.size());
        assertEquals(1, match0sub.fromItem.get(0).variables.size());
        assertEquals(1, with1sub.fromItem.get(0).variables.size());

        assertSame(with1, tree.ret.fromItem.get(0).source);
        assertSame(match0, with1sub.fromItem.get(0).source);
        assertNull(match0sub.fromItem.get(0).source);
    }

    @Test
    void computeFromItemsWithManyAliases() throws Exception {
        String cypher =
                "MATCH (a)\n" +
                "WITH a AS b\n" +
                "MATCH (b)--(c)\n" +
                "WITH c AS b\n" +
                "RETURN b.firstName";
        ScopeSelect tree = computeFromItems(cypher);

        WithSelect match0 = tree.withQueries.get(0);
        WithSelect with1 = tree.withQueries.get(1);
        WithSelect match2 = tree.withQueries.get(2);
        WithSelect with3 = tree.withQueries.get(3);

        SimpleSelect match0sub = (SimpleSelect) match0.subquery;
        SimpleSelect with1sub = (SimpleSelect) with1.subquery;
        SimpleSelect match2sub = (SimpleSelect) match2.subquery;
        SimpleSelect with3sub = (SimpleSelect) with3.subquery;

        assertEquals(1, tree.ret.fromItem.size());
        assertEquals(1, with3sub.fromItem.size());
        assertEquals(2, match2sub.fromItem.size());
        assertEquals(1, with1sub.fromItem.size());
        assertEquals(1, match0sub.fromItem.size());


        assertEquals(1, tree.ret.fromItem.get(0).variables.size());
        assertEquals(1, with3sub.fromItem.get(0).variables.size());
        assertEquals(1, match2sub.fromItem.get(0).variables.size());
        assertEquals(1, match2sub.fromItem.get(1).variables.size());
        assertEquals(1, with1sub.fromItem.get(0).variables.size());
        assertEquals(1, match0sub.fromItem.get(0).variables.size());

        // The following is the most important part of the test
        assertEquals("b", match2sub.fromItem.get(0).variables.get(0).name);
        assertEquals("c", match2sub.fromItem.get(1).variables.get(0).name);
        assertSame(with3, tree.ret.fromItem.get(0).source);
        assertSame(match2, with3sub.fromItem.get(0).source);
        assertSame(with1, match2sub.fromItem.get(0).source);
        assertNull(match2sub.fromItem.get(1).source);
        assertSame(match0, with1sub.fromItem.get(0).source);
        assertNull(match0sub.fromItem.get(0).source);
    }

    private ScopeSelect computeFromItems(String cypher) throws Exception {
        Statement st = PassAvailables.parseCypher(cypher);
        VarDependencies vars = new VarDependencies(st);
        ScopeSelect tree = SelectTreeBuilder.createQueryTree(vars, st);
        NameSubqueries.nameSubqueries(tree);
        ComputeFromItems.computeFromItems(tree, vars);
        return tree;
    }

    private Optional<FromItem> getByName(List<FromItem> fis, String varname) {
        return fis.stream()
                .filter(fi -> fi.variables.stream().anyMatch(x -> x.name.equals(varname)))
                .findAny();
    }
}
