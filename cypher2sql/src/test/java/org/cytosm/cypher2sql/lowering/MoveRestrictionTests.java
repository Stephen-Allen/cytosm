package org.cytosm.cypher2sql.lowering;

import org.cytosm.cypher2sql.PassAvailables;
import org.cytosm.cypher2sql.cypher.ast.Statement;
import org.cytosm.cypher2sql.lowering.exceptions.Cypher2SqlException;
import org.cytosm.cypher2sql.lowering.sqltree.ScopeSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelect;
import org.cytosm.cypher2sql.lowering.typeck.VarDependencies;
import org.cytosm.cypher2sql.lowering.typeck.constexpr.ConstVal;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprTree;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 */
class MoveRestrictionTests {

    @Test
    void movePredicateAsWhereClauseShouldWork() throws Cypher2SqlException {
        String cypher = "MATCH (a:Person {id: 0}) RETURN a.firstName";
        Statement query = PassAvailables.parseCypher(cypher);
        VarDependencies vars = new VarDependencies(query);
        ScopeSelect tree = SelectTreeBuilder.createQueryTree(vars, query);
        NameSubqueries.nameSubqueries(tree);
        MoveRestrictionInPattern.moveRestrictionInPatterns(tree, vars);

        // Assertions:
        SimpleSelect s = (SimpleSelect) tree.withQueries.get(0).subquery;
        assertNotNull(s.whereCondition);
        ExprTree.Eq eq = (ExprTree.Eq) s.whereCondition;
        assertEquals(0, ((ConstVal.LongVal) eq.rhs).value);
        assertEquals("id", ((ExprTree.PropertyAccess) eq.lhs).propertyAccessed);
    }

    @Test
    void moveTwoPredicateAsWhereClauseShouldWork() throws Cypher2SqlException {
        String cypher = "MATCH (a:Person {id: 0})-[:KNOWS]-(b:Person {foo: 30})\n" +
                "RETURN a.firstName";
        Statement query = PassAvailables.parseCypher(cypher);
        VarDependencies vars = new VarDependencies(query);
        ScopeSelect tree = SelectTreeBuilder.createQueryTree(vars, query);
        NameSubqueries.nameSubqueries(tree);
        MoveRestrictionInPattern.moveRestrictionInPatterns(tree, vars);

        // Assertions:
        SimpleSelect s = (SimpleSelect) tree.withQueries.get(0).subquery;
        assertNotNull(s.whereCondition);
        ExprTree.And and = (ExprTree.And) s.whereCondition;
        ExprTree.Eq eq1 = (ExprTree.Eq) and.lhs;
        ExprTree.Eq eq2 = (ExprTree.Eq) and.rhs;
        assertEquals(0, ((ConstVal.LongVal) eq1.rhs).value);
        assertEquals("id", ((ExprTree.PropertyAccess) eq1.lhs).propertyAccessed);
        assertEquals(30, ((ConstVal.LongVal) eq2.rhs).value);
        assertEquals("foo", ((ExprTree.PropertyAccess) eq2.lhs).propertyAccessed);
    }

    @Test
    void movePredicateEverywhere() throws Cypher2SqlException {
        String cypher = "MATCH (a:Person {id: 0})-[:KNOWS]-(b:Person)\n" +
                "MATCH (b)-[:KNOWS]-(c:Person {foo:23}) RETURN a.firstName";
        Statement query = PassAvailables.parseCypher(cypher);
        VarDependencies vars = new VarDependencies(query);
        ScopeSelect tree = SelectTreeBuilder.createQueryTree(vars, query);
        NameSubqueries.nameSubqueries(tree);
        MoveRestrictionInPattern.moveRestrictionInPatterns(tree, vars);

        // Assertions:
        SimpleSelect s = (SimpleSelect) tree.withQueries.get(0).subquery;
        assertNotNull(s.whereCondition);
        ExprTree.Eq eq = (ExprTree.Eq) s.whereCondition;
        assertEquals(0, ((ConstVal.LongVal) eq.rhs).value);
        assertEquals("id", ((ExprTree.PropertyAccess) eq.lhs).propertyAccessed);
        SimpleSelect s2 = (SimpleSelect) tree.withQueries.get(1).subquery;
        assertNotNull(s2.whereCondition);
        ExprTree.Eq eq2 = (ExprTree.Eq) s2.whereCondition;
        assertEquals(23, ((ConstVal.LongVal) eq2.rhs).value);
        assertEquals("foo", ((ExprTree.PropertyAccess) eq2.lhs).propertyAccessed);
    }

    @Test
    void dontAssertTwice() throws Cypher2SqlException {
        String cypher = "MATCH (a:Person {id: 0})-[:KNOWS]-(b:Person)\n" +
                "MATCH (a)-[:KNOWS]-(c:Person) " +
                "RETURN a.firstName";
        Statement query = PassAvailables.parseCypher(cypher);
        VarDependencies vars = new VarDependencies(query);
        ScopeSelect tree = SelectTreeBuilder.createQueryTree(vars, query);
        NameSubqueries.nameSubqueries(tree);
        MoveRestrictionInPattern.moveRestrictionInPatterns(tree, vars);

        // Assertions:
        SimpleSelect s = (SimpleSelect) tree.withQueries.get(0).subquery;
        assertNotNull(s.whereCondition);
        ExprTree.Eq eq = (ExprTree.Eq) s.whereCondition;
        assertEquals(0, ((ConstVal.LongVal) eq.rhs).value);
        assertEquals("id", ((ExprTree.PropertyAccess) eq.lhs).propertyAccessed);
        SimpleSelect s2 = (SimpleSelect) tree.withQueries.get(1).subquery;
        assertNull(s2.whereCondition);
    }

    @Test
    @Disabled
    void moveWorksAcrossNonOptionalMatches() throws Cypher2SqlException {
        String cypher = "MATCH (a:Person {firstName: 'Richard'}) " +
                "MATCH (a {id:1099511636050}) " +
                "RETURN a.firstName";
        Statement query = PassAvailables.parseCypher(cypher);
        VarDependencies vars = new VarDependencies(query);
        ScopeSelect tree = SelectTreeBuilder.createQueryTree(vars, query);
        NameSubqueries.nameSubqueries(tree);
        MoveRestrictionInPattern.moveRestrictionInPatterns(tree, vars);

        // Assertions
        SimpleSelect s = (SimpleSelect) tree.withQueries.get(0).subquery;
        assertNotNull(s.whereCondition);
        ExprTree.And and = (ExprTree.And) s.whereCondition;
        ExprTree.Eq eq1 = (ExprTree.Eq) and.lhs;
        ExprTree.Eq eq2 = (ExprTree.Eq) and.rhs;
        assertEquals("Richard", ((ConstVal.StrVal) eq1.rhs).value);
        assertEquals("firstName", ((ExprTree.PropertyAccess) eq1.lhs).propertyAccessed);
        assertEquals(1099511636050L, ((ConstVal.LongVal) eq2.rhs).value);
        assertEquals("id", ((ExprTree.PropertyAccess) eq2.lhs).propertyAccessed);
        SimpleSelect s2 = (SimpleSelect) tree.withQueries.get(1).subquery;
        assertNull(s2.whereCondition);
    }

    @Test
    @Disabled
    void moveDoesNotBreakOptionalMatches() throws Cypher2SqlException {
        String cypher = "MATCH (a:Person {firstName: 'Richard'}) " +
                "OPTIONAL MATCH (a {id:1099511636050}) " +
                "RETURN a.firstName";
        Statement query = PassAvailables.parseCypher(cypher);
        VarDependencies vars = new VarDependencies(query);
        ScopeSelect tree = SelectTreeBuilder.createQueryTree(vars, query);
        NameSubqueries.nameSubqueries(tree);
        MoveRestrictionInPattern.moveRestrictionInPatterns(tree, vars);

        // Assertions
        SimpleSelect s = (SimpleSelect) tree.withQueries.get(0).subquery;
        assertNotNull(s.whereCondition);
        ExprTree.Eq eq1 = (ExprTree.Eq) s.whereCondition;
        assertEquals("Richard", ((ConstVal.StrVal) eq1.rhs).value);
        assertEquals("firstName", ((ExprTree.PropertyAccess) eq1.lhs).propertyAccessed);

        SimpleSelect s2 = (SimpleSelect) tree.withQueries.get(1).subquery;
        assertNotNull(s2.whereCondition);
        ExprTree.Eq eq2 = (ExprTree.Eq) s2.whereCondition;
        assertEquals(1099511636050L, ((ConstVal.LongVal) eq2.rhs).value);
        assertEquals("id", ((ExprTree.PropertyAccess) eq2.lhs).propertyAccessed);
    }
}
