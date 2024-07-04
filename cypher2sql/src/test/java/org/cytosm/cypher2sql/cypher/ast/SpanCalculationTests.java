package org.cytosm.cypher2sql.cypher.ast;

import org.cytosm.cypher2sql.cypher.parser.ASTBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 */
class SpanCalculationTests {

    @Test
    void spanCalculation1() {
        String cypher = "MATCH (a:Person {id: 'test'}) WHERE a.x > (12 + 4 / 1)\n" +
                "WITH a AS foobar RETURN 234, foobar.test ORDER BY {} SKIP 23 LIMIT 42";
        Statement st = ASTBuilder.parse(cypher);
        assertEquals(0, st.span.lo);
        assertEquals(124, st.span.hi);
    }
}
