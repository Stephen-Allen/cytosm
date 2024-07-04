package org.cytosm.cypher2sql.lowering.typeck;

import java.util.ArrayList;
import java.util.List;

import org.cytosm.cypher2sql.PassAvailables;
import org.cytosm.cypher2sql.cypher.ast.SingleQuery;
import org.cytosm.cypher2sql.cypher.ast.Statement;
import org.cytosm.cypher2sql.lowering.typeck.var.Var;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 */
class NamingTests extends BaseVarTests {

    @Test
    void generatedNames() {
        String cypher = "MATCH p=(a)-[r]-(b) WITH a AS foo RETURN foo.bar";
        Statement st = PassAvailables.parseCypher(cypher);
        SingleQuery sq = (SingleQuery) st.query.part;
        VarDependencies dependencies = new VarDependencies(st);

        List<Var> vars = new ArrayList<>(dependencies.getAllVariables());

        assertEquals(5, vars.size());
        assertEquals("__cytosm6$7", getByName(vars, "p").uniqueName);
        assertEquals("__cytosm9$10", getByName(vars, "a").uniqueName);
        assertEquals("__cytosm13$14", getByName(vars, "r").uniqueName);
        assertEquals("__cytosm17$18", getByName(vars, "b").uniqueName);
        assertEquals("__cytosm30$33", getByName(vars, "foo").uniqueName);
    }
}
