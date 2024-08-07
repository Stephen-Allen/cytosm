package org.cytosm.cypher2sql.lowering;

import org.cytosm.cypher2sql.PassAvailables;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Rendering tests don't assert anything. Instead they
 * pass if and only if the code doesn't rise any exception.
 * They are sort of end 2 end tests.
 * Finally this is also a convenient place to visualize
 * the SQL result to get a feeling of the result being generated
 * on simple examples.
 */
class RenderingTests extends BaseLDBCTests {

    @Test
    @Disabled
    void basicCypherToSQL() throws Exception {
        String cypher =
                "MATCH (a:Person {firstName: 'Richard', lastName: 'Smith'})-[:KNOWS]-(b:Person)\n" +
                "MATCH (a {id: 0})-[:KNOWS]-(c:Person)-[:KNOWS]-(b)\n" +
                "RETURN b.firstName";
        cypher2sql(cypher);
    }

    @Test
    void orderByOnReturnUsingAnAliasVar() throws Exception {
        String cypher =
                "MATCH (a:Person)\n" +
                "RETURN a.firstName AS foo ORDER BY foo DESC";
        cypher2sql(cypher);
    }

    @Test
    void countFunction() throws Exception {
        String cypher =
                "MATCH (a:Message)\n" +
                "WITH count(a) AS messageCount\n" +
                "RETURN messageCount";
        cypher2sql(cypher);
    }

    @Test
    void aliasVarInWith() throws Exception {
        String cypher =
                "MATCH (a:Person)\n" +
                "WITH a AS b\n" +
                "MATCH (b)-[:KNOWS]-(c:Person) WHERE c.firstName = b.firstName\n" +
                "RETURN b.firstName, c.age";
        cypher2sql(cypher);
    }

    @Test
    void disjointMatch() throws Exception {
        // This is example should trigger the second MATCH
        // to increase the cardinality of the final result.
        String cypher =
                "MATCH (a:Person {id:0})-[:KNOWS]-(b:Person)\n" +
                "MATCH (c:Person)\n" +
                "RETURN 42";
        cypher2sql(cypher);
    }

    @Test
    void multipleMatchOnNode() throws Exception {
        String cypher =
                "MATCH (a:Message)\n" +
                "RETURN a.length ORDER BY a.length DESC\n" +
                "LIMIT 10";
        cypher2sql(cypher);
    }

    @Test
    void hops() throws Exception {
        String cypher = "MATCH (a:Person)-[:KNOWS*1..2]-(b:Person) RETURN a.id";
        System.out.println(PassAvailables.cypher2sql(getGTopInterface(), cypher));
    }

    @Test
    void mergeExpandCyphers() throws Exception {
        String cypher =
                "MATCH (a)\n" +
                "RETURN a.id\n" +
                "LIMIT 10";
        System.out.println(PassAvailables.cypher2sql(getGTopInterface(), cypher));
    }

    @Test
    void indirectDependency() throws Exception {
        String cypher =
                "MATCH (a:Person)-[:KNOWS]-(b:Person)\n" +
                "MATCH (b)-[:KNOWS]-(c:Person)\n" +
                "MATCH (a)-[:KNOWS]-(d:Person)\n" +
                "RETURN 42";
        cypher2sql(cypher);
    }

    @Test
    void fullExample() throws Exception {
        String cypher =
                "MATCH (test:Person {id:2199023259437})-[:KNOWS]->(friend:Person)\n" +
                "MATCH (:Person)-[:KNOWS]->(other_friend:Person)\n" +
                "MATCH (friend)-[:STUDY_AT]->(:University)-[:IS_LOCATED_IN]->(:City)\n" +
                "RETURN friend.id";
        cypher2sql(cypher);
    }

    @Test
    void circularRelationships() throws Exception {
        String cypher =
                "MATCH (a:Person)-[:KNOWS]-(b:Person)-[:KNOWS]-(c:Person)\n" +
                "MATCH (a)-[:KNOWS]-(c)\n" +
                "RETURN a.id, b.id, c.id";
        cypher2sql(cypher);
    }

    @Test
    void joinsExpandedIntoProblematicUnions() throws Exception {
        String cypher =
                "MATCH (a:Message)\n" +
                "MATCH (a)<-[:LIKES]-(b:Person)\n" +
                "RETURN a.id, b.id";
        cypher2sql(cypher);
    }

    @Test
    void aliasVar() throws Exception {
        String cypher =
                "MATCH (a:Person)-[:KNOWS]-(b:Person)\n" +
                "WITH a, {c:{b:b}} AS d\n" +
                "RETURN a.firstName, d.c.b.firstName";
        cypher2sql(cypher);
    }

    @Test
    void inExpression() throws Exception {
        String cypher =
                "MATCH (a:Person) WHERE a.firstName IN ['foo', 'bar']\n" +
                "RETURN a.firstName";
        cypher2sql(cypher);
    }

    @Test
    void aliasVarInUnions() throws Exception {
        String cypher =
                "MATCH (a:Message)\n" +
                "WITH a, {c:{b:a}} AS d\n" +
                "RETURN a.firstName, d.c.b.firstName";
        cypher2sql(cypher);
    }

    private void cypher2sql(String cypher) throws Exception {
        System.out.println(PassAvailables.cypher2sqlOnExpandedPaths(getGTopInterface(), cypher).toSQLString());
    }
}
