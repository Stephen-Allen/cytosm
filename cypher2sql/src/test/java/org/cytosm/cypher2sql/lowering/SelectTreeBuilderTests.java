package org.cytosm.cypher2sql.lowering;

import org.cytosm.cypher2sql.PassAvailables;
import org.cytosm.cypher2sql.lowering.sqltree.ScopeSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelectWithInnerJoins;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelectWithLeftJoins;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 */
class SelectTreeBuilderTests {

    @Test
    void structure() {
        String cypher =
                "MATCH (a)\n" +
                "MATCH (a)\n" +
                "RETURN a.firstName;";
        ScopeSelect tree = PassAvailables.buildQueryTree(cypher);
        assertEquals(-1, tree.ret.limit);
        assertEquals(-1, tree.ret.skip);
        assertTrue(tree.ret.orderBy.isEmpty());
        assertNull(tree.ret.whereCondition);
        assertEquals(2, tree.withQueries.size());
        assertEquals(tree.withQueries.get(0).varId, tree.withQueries.get(0).subquery.varId);
        assertEquals(tree.withQueries.get(1).varId, tree.withQueries.get(1).subquery.varId);
        assertNotNull(tree.withQueries.get(0).subquery.varId);
        assertNotNull(tree.withQueries.get(1).subquery.varId);
        assertNotNull(tree.ret.varId);
    }

    @Test
    void skipRet() {
        String cypher =
                "MATCH (a) RETURN a.firstName SKIP 2 + 4*10";
        ScopeSelect tree = PassAvailables.buildQueryTree(cypher);
        assertEquals(42, tree.ret.skip);
    }

    @Test
    void skipWith() {
        String cypher =
                "MATCH (a) WITH a SKIP 2 + 4*10 RETURN a";
        ScopeSelect tree = PassAvailables.buildQueryTree(cypher);
        assertEquals(42, ((SimpleSelect) tree.withQueries.get(1).subquery).skip);
    }

    @Test
    void limitRet() {
        String cypher =
                "MATCH (a) RETURN a.firstName LIMIT 2 + 4*10";
        ScopeSelect tree = PassAvailables.buildQueryTree(cypher);
        assertEquals(42, tree.ret.limit);
    }

    @Test
    void limitWith() {
        String cypher =
                "MATCH (a) WITH a.firstName AS afirstName LIMIT 2 + 4*10 RETURN 50";
        ScopeSelect tree = PassAvailables.buildQueryTree(cypher);
        assertEquals(42, ((SimpleSelect) tree.withQueries.get(1).subquery).limit);
    }

    @Test
    void orderByASC() {
        String cypher = "MATCH (a) RETURN a.firstName ORDER BY a.firstName ASC";
        ScopeSelect tree = PassAvailables.buildQueryTree(cypher);
        assertFalse(tree.ret.orderBy.get(0).descending);
    }

    @Test
    void orderByDESC() {
        String cypher = "MATCH (a) RETURN a.firstName ORDER BY a.firstName DESC";
        ScopeSelect tree = PassAvailables.buildQueryTree(cypher);
        assertTrue(tree.ret.orderBy.get(0).descending);
    }

    @Test
    void optionalMatchNormalMatch() {
        String cypher =
                "MATCH (a:Person {id: 0})\n" +
                "OPTIONAL MATCH (a)-[:KNOWS]-(b:Person)\n" +
                "RETURN a.firstName, b.firstName";
        ScopeSelect tree = PassAvailables.buildQueryTree(cypher);
        assertEquals(2, tree.withQueries.size());
        assertInstanceOf(SimpleSelectWithInnerJoins.class, tree.withQueries.get(0).subquery);
        assertInstanceOf(SimpleSelectWithLeftJoins.class, tree.withQueries.get(1).subquery);
    }
}
