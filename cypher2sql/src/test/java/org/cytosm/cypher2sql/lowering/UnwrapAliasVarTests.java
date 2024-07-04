package org.cytosm.cypher2sql.lowering;

import org.cytosm.cypher2sql.PassAvailables;
import org.cytosm.cypher2sql.lowering.sqltree.ScopeSelect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 */
class UnwrapAliasVarTests extends BaseLDBCTests {

    @Test
    void aliasVarIsKeptOnReturn() throws Exception {
        String cypher = "MATCH (a:Person) RETURN a.firstName AS a";
        ScopeSelect tree = PassAvailables.cypher2sqlOnExpandedPaths(getGTopInterface(), cypher);
        assertEquals(1, tree.ret.exportedItems.size());
    }
}
