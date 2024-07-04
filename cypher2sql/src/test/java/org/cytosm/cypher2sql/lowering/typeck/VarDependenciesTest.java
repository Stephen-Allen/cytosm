package org.cytosm.cypher2sql.lowering.typeck;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cytosm.cypher2sql.PassAvailables;
import org.cytosm.cypher2sql.cypher.ast.SingleQuery;
import org.cytosm.cypher2sql.cypher.ast.Statement;
import org.cytosm.cypher2sql.cypher.ast.clause.Clause;
import org.cytosm.cypher2sql.cypher.ast.clause.match.Match;
import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.PatternPart;
import org.cytosm.cypher2sql.cypher.ast.clause.projection.With;
import org.cytosm.cypher2sql.lowering.typeck.expr.Expr;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprTree;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprVar;
import org.cytosm.cypher2sql.lowering.typeck.rel.Relationship;
import org.cytosm.cypher2sql.lowering.typeck.var.AliasVar;
import org.cytosm.cypher2sql.lowering.typeck.var.Var;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 */
class VarDependenciesTest extends BaseVarTests {

    /**
     * Simple example where we make sure that the variable
     * reference is the same between the MATCH that has declared it
     * and the RETURN statement.
     */
    @Test
    void variablesMatchReturn() {
        String cypher = "MATCH (a) RETURN a.firstName";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);
        ClauseId id = this.genClauseForASTNode(
                this.getPatternPart(sq.clauses.iterator().next()).next()
        );
        List<Expr> ret = dependencies.getReturnExprs();
        ExprTree.PropertyAccess retProp = (ExprTree.PropertyAccess) ((ExprTree.AliasExpr) ret.get(0)).expr;

        List<Var> matchVars = dependencies.getUsedVars(id);
        assertEquals(1, matchVars.size());
        assertEquals("a", matchVars.get(0).name);
        assertSame(matchVars.get(0), ((ExprVar) retProp.expression).var);
        assertEquals("firstName", retProp.propertyAccessed);
    }

    @Test
    void reachableVariablesMatchMatchWithMatchReturn() {
        String cypher =
                "MATCH (a)--(b)\n" +     // match0
                "MATCH (b)\n" +          // match1
                "WITH (a)\n" +           // with2
                "MATCH (b)\n" +          // match3
                "RETURN a.firstName";    // ret
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);
        Iterator<Clause> iter = sq.clauses.iterator();
        AvailableVariables match0 = dependencies.getReachableVars(this.genClauseForASTNode(this.getPatternPart(iter.next()).next()));
        AvailableVariables match1 = dependencies.getReachableVars(this.genClauseForASTNode(this.getPatternPart(iter.next()).next()));
        AvailableVariables with2 = dependencies.getReachableVars(this.genClauseForASTNode(iter.next()));
        AvailableVariables match3 = dependencies.getReachableVars(this.genClauseForASTNode(this.getPatternPart(iter.next()).next()));
        AvailableVariables ret = dependencies.getReachableVars(this.genClauseForASTNode(iter.next()));

        assertTrue(match0.isEmpty());
        assertTrue(match1.get("a").isPresent());
        assertTrue(match1.get("b").isPresent());
        assertTrue(with2.get("a").isPresent());
        assertTrue(with2.get("b").isPresent());
        assertTrue(match3.get("a").isPresent());
        assertFalse(match3.get("b").isPresent());
        assertTrue(ret.get("a").isPresent());
        assertTrue(ret.get("b").isPresent());
    }

    @Test
    void usedVariablesMatchMatchWithMatchReturn() {
        String cypher =
                "MATCH (a)--(b)\n" +     // match0
                "MATCH (b)\n" +          // match1
                "WITH (a)\n" +           // with2
                "MATCH (b)\n" +          // match3
                "RETURN a.firstName";    // ret
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);
        Iterator<Clause> iter = sq.clauses.iterator();
        List<Var> match0 = dependencies.getUsedVars(this.genClauseForASTNode(this.getPatternPart(iter.next()).next()));
        List<Var> match1 = dependencies.getUsedVars(this.genClauseForASTNode(this.getPatternPart(iter.next()).next()));
        List<Var> with2 = dependencies.getUsedVars(this.genClauseForASTNode(iter.next()));
        List<Var> match3 = dependencies.getUsedVars(this.genClauseForASTNode(this.getPatternPart(iter.next()).next()));
        List<Expr> ret = dependencies.getReturnExprs();
        ExprTree.PropertyAccess retProp = (ExprTree.PropertyAccess) ((ExprTree.AliasExpr) ret.get(0)).expr;

        // Make sure we have collected something.
        assertEquals(2, match0.size());
        // Make sure nothing is null.
        assertNotSame(null, getByName(match0, "b"));
        assertNotSame(null, getByName(match0, "a"));
        assertNotSame(null, getByName(match3, "b"));
        // Make sure that references are the same where they should be...
        assertSame(getByName(match0, "b"), getByName(match1, "b"));
        assertSame(getByName(match0, "a"), getByName(with2, "a"));
        assertSame(getByName(match0, "a"), ((ExprVar) retProp.expression).var);
        // ...and not the same where they should not be.
        assertNotSame(getByName(match0, "b"), getByName(match3, "b"));
    }

    @Test
    void variablesHiddenInMapExpression1() {
        String cypher =
                "MATCH (a:Person)\n" +
                "WITH a, {b: {c: \"test\", d: a.firstName}} AS b\n" +
                "MATCH (c:Person) WHERE c.firstName = b.b.c\n" +
                "RETURN a, b, c";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);
        Iterator<Clause> iter = sq.clauses.iterator();
        List<Var> match0 = dependencies.getUsedVars(this.genClauseForASTNode(this.getPatternPart(iter.next()).next()));
        List<Var> with1 = dependencies.getUsedVars(this.genClauseForASTNode(iter.next()));
        List<Var> match2 = dependencies.getUsedVars(this.genClauseForASTNode(this.getPatternPart(iter.next()).next()));
        List<Expr> ret = dependencies.getReturnExprs();

        // Check the MapExpression
        assertSame(getByName(match0, "a"), getByName(with1, "a"));
        AliasVar b = (AliasVar) getByName(with1, "b");
        assertSame(getByName(match0, "a") ,
                ((ExprVar) ((ExprTree.PropertyAccess) ((ExprTree.MapExpr)((ExprTree.MapExpr) b.aliased)
                        .props.get("b"))
                        .props.get("d"))
                        .expression).var);
        assertSame(getByName(with1, "b"), ((ExprVar) ((ExprTree.AliasExpr) ret.get(1)).expr).var);
    }

    @Test
    void variablesMatchCommaReturn() {
        String cypher =
                "MATCH (a), (b)\n" +
                "RETURN a.firstName";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);
        Match match = ((Match) sq.clauses.iterator().next());
        Iterator<PatternPart> iter = this.getPatternPart(match);
        List<Var> match0 = dependencies.getUsedVars(this.genClauseForASTNode(iter.next()));
        List<Var> comma1 = dependencies.getUsedVars(this.genClauseForASTNode(iter.next()));

        // Make sure we have collected something.
        assertEquals(1, match0.size());
        assertEquals(1, comma1.size());
        assertEquals("a", match0.get(0).name);
        assertEquals("b", comma1.get(0).name);
    }

    @Test
    void relationships() {
        String cypher =
                "MATCH (a)--(b)\n" +
                "RETURN 42";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);
        Match match = ((Match) sq.clauses.iterator().next());
        Iterator<PatternPart> iter = this.getPatternPart(match);
        List<Relationship> match0 = dependencies.getRelationships(this.genClauseForASTNode(iter.next()));
        assertEquals(1, match0.size());
        assertEquals("a", match0.get(0).leftNode.name);
        assertEquals("b", match0.get(0).rightNode.name);
    }

    @Test
    void relationshipsPathOrder() {
        String cypher =
                "MATCH (a)--(b)--(c)\n" +
                "RETURN 42";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);
        Match match = ((Match) sq.clauses.iterator().next());
        Iterator<PatternPart> iter = this.getPatternPart(match);
        List<Relationship> match0 = dependencies.getRelationships(this.genClauseForASTNode(iter.next()));

        // Make sure the path has the correct order
        assertEquals(2, match0.size());
        assertEquals("a", match0.get(0).leftNode.name);
        assertEquals("b", match0.get(0).rightNode.name);
        assertEquals("b", match0.get(1).leftNode.name);
        assertEquals("c", match0.get(1).rightNode.name);
        // Make sure the reference to variables are the same
        assertSame(match0.get(0).rightNode, match0.get(1).leftNode);
    }

    @Test
    void getUsedAndIndirectUsedVars1() {
        String cypher =
                "MATCH (a)--(b)\n" +
                "MATCH (b)--(c)\n" +
                "MATCH (a)--(d)\n" +
                "RETURN 42";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);

        Iterator<Clause> clauses = sq.clauses.iterator();
        List<Var> match0 = dependencies.getUsedAndIndirectUsedVars(
                this.genClauseForASTNode(this.getPatternPart(clauses.next()).next()));
        List<Var> match1 = dependencies.getUsedAndIndirectUsedVars(
                this.genClauseForASTNode(this.getPatternPart(clauses.next()).next()));
        List<Var> match2 = dependencies.getUsedAndIndirectUsedVars(
                this.genClauseForASTNode(this.getPatternPart(clauses.next()).next()));

        assertEquals(2, match0.size());
        assertEquals(3, match1.size());
        assertEquals(4, match2.size());
    }

    @Test
    void getUsedAndIndirectUsedVars2() {
        String cypher =
                "MATCH (a)--(e)\n" +
                "MATCH (b)--(c)\n" +
                "MATCH (a)--(d)\n" +
                "RETURN 42";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);

        Iterator<Clause> clauses = sq.clauses.iterator();
        List<Var> match0 = dependencies.getUsedAndIndirectUsedVars(
                this.genClauseForASTNode(this.getPatternPart(clauses.next()).next()));
        List<Var> match1 = dependencies.getUsedAndIndirectUsedVars(
                this.genClauseForASTNode(this.getPatternPart(clauses.next()).next()));
        List<Var> match2 = dependencies.getUsedAndIndirectUsedVars(
                this.genClauseForASTNode(this.getPatternPart(clauses.next()).next()));

        assertEquals(2, match0.size());
        assertEquals(2, match1.size());
        assertEquals(3, match2.size());
    }

    @Test
    void variableDefinedInReturn() {
        String cypher =
                "MATCH (a)\n" +
                "RETURN a AS b ORDER BY b";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies deps = new VarDependencies(st);
        AvailableVariables ret = deps.getReachableVars(new ClauseId(sq.clauses.get(1)));
        List<Var> allVars = new ArrayList<>(deps.getAllVariables());
        assertFalse(ret.get("b").isPresent());
        assertNotNull(getByName(allVars, "b"));
    }

    @Test
    void reachableVarsInOrderBy() {
        String cypher =
                "MATCH (a)\n" +
                "WITH a.firstName AS foo ORDER BY foo\n" +
                "RETURN foo";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies deps = new VarDependencies(st);
        With with = (With) sq.clauses.get(1);
        AvailableVariables orderBy = deps.getReachableVars(new ClauseId(with.orderBy.get()));
        assertTrue(orderBy.get("foo").isPresent());
    }
}