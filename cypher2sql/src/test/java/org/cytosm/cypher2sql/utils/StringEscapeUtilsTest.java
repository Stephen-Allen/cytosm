package org.cytosm.cypher2sql.utils;

import org.junit.Test;

import static org.cytosm.cypher2sql.utils.StringEscapeUtils.escape;
import static org.junit.Assert.assertEquals;

public class StringEscapeUtilsTest {

    @Test(expected = NullPointerException.class)
    public void itShouldRequireANonNullString() {
        escape(null, new char[] {'f'});
    }

    @Test(expected = NullPointerException.class)
    public void itShouldRequireNonNullChars() {
        escape("P_P", null);
    }

    @Test
    public void itShouldEscapeAnyChars() {
        assertEquals("\\p\\p", escape("pp", new char[]{'p', 'q'}));
        assertEquals("pp", escape("pp", new char[]{}));
        assertEquals("pp", escape("pp", new char[]{'r', ' '}));
    }

}
