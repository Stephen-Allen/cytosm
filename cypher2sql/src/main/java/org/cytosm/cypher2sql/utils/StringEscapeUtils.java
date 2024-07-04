package org.cytosm.cypher2sql.utils;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

import static org.apache.commons.lang3.ArrayUtils.contains;

/**
 * Utility methods for escaping strings
 */
public class StringEscapeUtils {

    /**
     * Escape the specified special characters in a String.
     *
     * @param value the string to escape
     * @param specialChars the special characters which need to be escaped
     * @return the escaped string
     */
    public static String escape(final String value, final char[] specialChars) {
        if (null == value) throw new NullPointerException("value must not be null");
        if (null == specialChars) throw new NullPointerException("special chars must not be null");

        return escape(value, specialChars, new char[] {});
    }

    /**
     * Escape the specified special characters in a String.
     *
     * @param value the string to escape
     * @param specialChars the special characters which need to be escaped
     * @param doubleChars the special characters which need to be doubled
     * @return the escaped string
     */
    public static String escape(final String value, final char[] specialChars, final char[] doubleChars) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            final char currentChar = value.charAt(i);

            if (currentChar == 0) {
                throw new IllegalArgumentException("The zero byte may not occur in string parameters");
            }

            // escape special chars
            if (contains(specialChars, currentChar)) {
                sb.append('\\');
            }

            // double chars as appropriate
            if (contains(doubleChars, currentChar)) {
                sb.append(currentChar);
            }
            sb.append(currentChar);
        }
        return sb.toString();
    }

    /**
     * Unescape cypher string literals.
     *
     * @see <a href="https://neo4j.com/docs/cypher-manual/current/syntax/expressions/">Cypher documentation</a>
     */
    public static String unescapeCypherStringLiteral(final String value) {
        return org.apache.commons.lang3.StringEscapeUtils.unescapeJava(value);
    }

    /**
     * Unescape cypher name literals.
     *
     * @see <a href="https://neo4j.com/docs/cypher-manual/current/syntax/naming/">Cypher documentation</a>
     */
    public static String unescapeCypherName(final String value) {
        final CharSequenceTranslator translator = new AggregateTranslator(
            new UnicodeUnescaper(),
            new LookupTranslator(new String[][] {
                {"``", "`"},
            })
        );
        return translator.translate(value);
    }

}
