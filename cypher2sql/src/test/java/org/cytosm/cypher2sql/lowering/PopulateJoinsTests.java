package org.cytosm.cypher2sql.lowering;

import org.cytosm.cypher2sql.PassAvailables;
import org.cytosm.cypher2sql.cypher.ast.Statement;
import org.cytosm.cypher2sql.lowering.sqltree.ScopeSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelect;
import org.cytosm.cypher2sql.lowering.typeck.VarDependencies;
import org.cytosm.cypher2sql.lowering.typeck.var.NodeVar;
import org.cytosm.cypher2sql.lowering.typeck.var.TempVar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 */
class PopulateJoinsTests extends BaseLDBCTests {

    @Test
    void populateJoins() throws Exception {
        String cypher =
                "MATCH (a:Person {id: 0})\n" +
                "OPTIONAL MATCH (a)<-[:KNOWS]-(b:Person)\n" +
                "RETURN a.firstName, b.firstName";

        ScopeSelect tree = fromBeginningUntilpopulateJoins(cypher);

        SimpleSelect optMatch = (SimpleSelect) tree.withQueries.get(1).subquery;
        assertEquals(2, optMatch.joinList().size());
        assertEquals(1, optMatch.fromItem.size());
        assertEquals(1, optMatch.fromItem.get(0).variables.size());
        assertEquals("person_knows_person", optMatch.joinList().get(0).joiningItem.sourceTableName);
        assertEquals("Person", optMatch.joinList().get(1).joiningItem.sourceTableName);
        assertEquals(1, optMatch.joinList().get(0).joiningItem.variables.size());
        assertInstanceOf(TempVar.class, optMatch.joinList().get(0).joiningItem.variables.get(0));
        assertEquals(1, optMatch.joinList().get(1).joiningItem.variables.size());
        assertEquals("b", ((NodeVar) optMatch.joinList().get(1).joiningItem.variables.get(0)).name);
    }

    @Test
    void populateJoinsMultiRelations() throws Exception {
        String cypher =
                "MATCH (a:Person)<-[:KNOWS]-(b:Person)<-[:KNOWS]-(c:Person)\n" +
                "RETURN a.firstName, b.firstName, c.firstName";
        ScopeSelect tree = fromBeginningUntilpopulateJoins(cypher);
        SimpleSelect match = (SimpleSelect) tree.withQueries.get(0).subquery;
        assertEquals(4, match.joinList().size());
        assertEquals(1, match.fromItem.size());
        assertEquals(1, match.fromItem.get(0).variables.size());
        assertEquals("person_knows_person", match.joinList().get(0).joiningItem.sourceTableName);
        assertEquals("Person", match.joinList().get(1).joiningItem.sourceTableName);
        assertEquals("person_knows_person", match.joinList().get(2).joiningItem.sourceTableName);
        assertEquals("Person", match.joinList().get(3).joiningItem.sourceTableName);
    }

    @Test
    void populateJoinsReuseBothInFrom() throws Exception {
        String cypher =
                "MATCH (a:Person)<-[:KNOWS]-(b:Person)\n" +
                "MATCH (a)-[:KNOWS]->(b)\n" +
                "RETURN a.firstName, b.firstName";

        ScopeSelect tree = fromBeginningUntilpopulateJoins(cypher);

        SimpleSelect match2 = (SimpleSelect) tree.withQueries.get(1).subquery;
        assertEquals(1, match2.fromItem.size());
        assertEquals(2, match2.fromItem.get(0).variables.size());
        assertEquals(1, match2.joinList().size());
        assertEquals("person_knows_person", match2.joinList().get(0).joiningItem.sourceTableName);
    }

    private ScopeSelect fromBeginningUntilpopulateJoins(String cypher) throws Exception {
        Statement st = PassAvailables.parseCypher(cypher);
        VarDependencies vars = new VarDependencies(st);
        ScopeSelect tree = SelectTreeBuilder.createQueryTree(vars, st);
        NameSubqueries.nameSubqueries(tree);
        ComputeFromItems.computeFromItems(tree, vars);
        MoveRestrictionInPattern.moveRestrictionInPatterns(tree, vars);
        tree = ExpandNodeVarWithGtop.computeTableNamesOnFromItems(tree, getGTopInterface());
        return PopulateJoins.populateJoins(tree, vars, getGTopInterface());
    }
}
