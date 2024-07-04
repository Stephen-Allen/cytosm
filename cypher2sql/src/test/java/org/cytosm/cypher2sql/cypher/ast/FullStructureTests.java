package org.cytosm.cypher2sql.cypher.ast;

import java.util.List;
import java.util.Optional;

import org.cytosm.cypher2sql.cypher.ast.clause.Clause;
import org.cytosm.cypher2sql.cypher.ast.clause.match.Match;
import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.NodePattern;
import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.RelationshipChain;
import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.RelationshipPattern;
import org.cytosm.cypher2sql.cypher.ast.clause.projection.Return;
import org.cytosm.cypher2sql.cypher.ast.clause.projection.ReturnItem;
import org.cytosm.cypher2sql.cypher.ast.clause.projection.With;
import org.cytosm.cypher2sql.cypher.ast.expression.Binary;
import org.cytosm.cypher2sql.cypher.ast.expression.ListExpression;
import org.cytosm.cypher2sql.cypher.ast.expression.Literal;
import org.cytosm.cypher2sql.cypher.ast.expression.MapExpression;
import org.cytosm.cypher2sql.cypher.ast.expression.Property;
import org.cytosm.cypher2sql.cypher.ast.expression.Variable;
import org.cytosm.cypher2sql.cypher.parser.ASTBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Those tests take a long time to write but
 * they check the entire structure to make sure everything is correct.
 */
class FullStructureTests {

    // This test would be hard to debug but it test all the features
    // together and so might be more likely to catch any bug introduced.
    @Test
    void fullExampleWithoutRelationships() {
        String cypher = "MATCH (a:Person {id: 'test'}) WHERE a.x > (12 + 4 / 1)\n" +
                "WITH a AS foobar RETURN 234, foobar.test ORDER BY {} SKIP 23 LIMIT 42";
        Statement st = ASTBuilder.parse(cypher);
        List<Clause> clauses = ((SingleQuery) st.query.part).clauses;
        assertEquals(3, clauses.size());
        assertInstanceOf(Match.class, clauses.get(0));
        assertInstanceOf(With.class, clauses.get(1));
        assertInstanceOf(Return.class, clauses.get(2));

        Match m = (Match) clauses.get(0);
        With w = (With) clauses.get(1);
        Return r = (Return) clauses.get(2);

        // Match
        assertFalse(m.optional);
        assertNotNull(m.pattern);
        assertTrue(m.where.isPresent());

        assertEquals(1, m.pattern.patternParts.size());
        assertInstanceOf(NodePattern.class, m.pattern.patternParts.get(0).element);
        NodePattern np = (NodePattern) m.pattern.patternParts.get(0).element;
        assertEquals(1, np.labels.size());
        assertEquals("Person", np.labels.get(0).name);
        assertTrue(np.variable.isPresent());
        assertEquals("a", np.variable.get().name);

        assertTrue(np.properties.isPresent());
        MapExpression props = np.properties.get();

        assertEquals("id", props.props.get(0).getKey().name);
        assertEquals("test", ((Literal.StringLiteral) props.props.get(0).getValue()).value);

        Binary.GreaterThan whereExpr = (Binary.GreaterThan) m.where.get().expression;
        Property lhs = (Property)  whereExpr.lhs;
        assertEquals("x", lhs.propertyKey.name);
        assertEquals("a", ((Variable) lhs.map).name);

        Binary.Add rhs = (Binary.Add) whereExpr.rhs;
        assertEquals(12L, ((Literal.Integer) rhs.lhs).value);
        assertEquals(4L, ((Literal.Integer) ((Binary.Divide) rhs.rhs).lhs).value);
        assertEquals(1L, ((Literal.Integer) ((Binary.Divide) rhs.rhs).rhs).value);

        // With
        assertEquals(1, w.returnItems.size());
        assertInstanceOf(ReturnItem.Aliased.class, w.returnItems.get(0));
        assertEquals("foobar", ((ReturnItem.Aliased) w.returnItems.get(0)).alias.name);
        assertEquals("a", ((Variable) w.returnItems.get(0).expression).name);

        // Return
        assertEquals(2, r.returnItems.size());
        assertInstanceOf(ReturnItem.Unaliased.class, r.returnItems.get(0));
        assertInstanceOf(ReturnItem.Unaliased.class, r.returnItems.get(1));
        assertEquals("foobar.test", ((ReturnItem.Unaliased) r.returnItems.get(1)).name);
        assertTrue(r.orderBy.isPresent());
        assertEquals(1, r.orderBy.get().sortItems.size());
        assertTrue(r.skip.isPresent());
        assertEquals(23, ((Literal.Integer) r.skip.get().expression).value);
        assertTrue(r.limit.isPresent());
        assertEquals(42, ((Literal.Integer) r.limit.get().expression).value);
    }

    @Test
    void relationshipChains1() {
        String cypher = "MATCH (a)-[r:TEST]-(c)";
        Statement st = ASTBuilder.parse(cypher);
        List<Clause> clauses = ((SingleQuery) st.query.part).clauses;
        Match m = (Match) clauses.get(0);

        assertInstanceOf(RelationshipChain.class, m.pattern.patternParts.get(0).element);

        RelationshipChain rc = (RelationshipChain) m.pattern.patternParts.get(0).element;
        assertInstanceOf(NodePattern.class, rc.element);
        assertTrue(((NodePattern) rc.element).variable.isPresent());
        assertTrue(rc.rightNode.variable.isPresent());
        assertEquals("a", ((NodePattern) rc.element).variable.get().name);
        assertEquals("c", rc.rightNode.variable.get().name);
        assertEquals(RelationshipPattern.SemanticDirection.BOTH, rc.relationship.direction);
        assertEquals(rc.relationship.length, Optional.empty());
        assertEquals(1, rc.relationship.types.size());
        assertEquals("TEST", rc.relationship.types.get(0).name);
    }

    @Test
    void listExpressions() {
        String cypher = "MATCH (a) WHERE a.lastName IN ['foo','bar'] RETURN a.firstName";
        Statement st = ASTBuilder.parse(cypher);
        List<Clause> clauses = ((SingleQuery) st.query.part).clauses;
        Match m = (Match) clauses.get(0);

        assertTrue(m.where.isPresent());
        assertInstanceOf(Binary.In.class, m.where.get().expression);

        Binary.In in = (Binary.In) m.where.get().expression;

        assertInstanceOf(ListExpression.class, in.rhs);

        ListExpression list = (ListExpression) in.rhs;

        assertEquals(2, list.exprs.size());
        assertEquals("foo", ((Literal.StringLiteral) list.exprs.get(0)).value);
        assertEquals("bar", ((Literal.StringLiteral) list.exprs.get(1)).value);
    }
}