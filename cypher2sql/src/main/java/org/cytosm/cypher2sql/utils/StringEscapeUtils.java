package org.cytosm.cypher2sql.utils;

import static com.google.common.base.Preconditions.checkNotNull;
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
        checkNotNull(value, "value must not be null");
        checkNotNull(specialChars, "special chars must not be null");

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
}
