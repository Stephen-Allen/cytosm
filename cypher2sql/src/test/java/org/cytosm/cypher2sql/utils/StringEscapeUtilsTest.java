package org.cytosm.cypher2sql.utils;

import org.junit.jupiter.api.Test;

import static org.cytosm.cypher2sql.utils.StringEscapeUtils.escape;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringEscapeUtilsTest {

    @Test
    void itShouldRequireANonNullString() {
        assertThrows(NullPointerException.class, () -> {
            escape(null, new char[]{'f'});
        });
    }

    @Test
    void itShouldRequireNonNullChars() {
        assertThrows(NullPointerException.class, () -> {
            escape("P_P", null);
        });
    }

    @Test
    void itShouldEscapeAnyChars() {
        assertEquals("\\p\\p", escape("pp", new char[]{'p', 'q'}));
        assertEquals("pp", escape("pp", new char[]{}));
        assertEquals("pp", escape("pp", new char[]{'r', ' '}));
    }

}
