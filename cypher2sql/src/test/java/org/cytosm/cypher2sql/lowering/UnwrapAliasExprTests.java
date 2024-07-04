package org.cytosm.cypher2sql.lowering;

import org.cytosm.cypher2sql.PassAvailables;
import org.cytosm.cypher2sql.lowering.sqltree.ScopeSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleOrScopeSelect;
import org.cytosm.cypher2sql.lowering.sqltree.UnionSelect;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 */
class UnwrapAliasExprTests extends BaseLDBCTests {

    @Test
    void thatNoAliasExprIsPresentOnNestedScopeSelect() throws Exception {
        String cypher =
                "MATCH (a:Message)\n" +
                "RETURN a.length";
        ScopeSelect tree = PassAvailables.cypher2sqlOnExpandedPaths(getGTopInterface(), cypher);

        assertEquals(1, tree.ret.exportedItems.size());
        assertInstanceOf(ExprTree.AliasExpr.class, tree.ret.exportedItems.get(0));

        UnionSelect unionSelect = (UnionSelect) tree.withQueries.get(0).subquery;

        assertEquals(2, unionSelect.unions.size());

        for (SimpleOrScopeSelect select: unionSelect.unions) {
            ScopeSelect nestedScopeSelect = (ScopeSelect) select;
            assertFalse(nestedScopeSelect.ret.exportedItems.get(0) instanceof ExprTree.AliasExpr);
        }
    }
}
