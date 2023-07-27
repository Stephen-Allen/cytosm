package org.cytosm.cypher2sql.lowering.rendering;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RenderingHelperTest {

    @Test
    public void itShouldEscapeForSqlLiteral() {
        final RenderingHelper helper = new RenderingHelper();
        assertEquals(" spaces do nothing ", helper.escapeForSqlLiteral(" spaces do nothing "));
        assertEquals("should escape '' marks", helper.escapeForSqlLiteral("should escape ' marks"));
        assertEquals("should escape ''foo'' marks", helper.escapeForSqlLiteral("should escape 'foo' marks"));
        assertEquals("should not escape ~", helper.escapeForSqlLiteral("should not escape ~"));
        assertEquals("should not escape %", helper.escapeForSqlLiteral("should not escape %"));
    }

    @Test
    public void itShouldEscapeForSqlLike() {
        final RenderingHelper helper = new RenderingHelper();
        assertEquals("\\_\\_\\_", helper.escapeForSqlLike("___"));
        assertEquals("\\%\\%\\%", helper.escapeForSqlLike("%%%"));
        assertEquals("i\\_love\\_ 'em underscores!", helper.escapeForSqlLike("i_love_ 'em underscores!"));
        assertEquals(" spaces do nothing ", helper.escapeForSqlLike(" spaces do nothing "));
        assertEquals("should not escape ~", helper.escapeForSqlLike("should not escape ~"));
        assertEquals("should escape \\%", helper.escapeForSqlLike("should escape %"));
        assertEquals("\\_\\%'", helper.escapeForSqlLike("_%'"));
    }
}
