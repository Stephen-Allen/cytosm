package org.cytosm.cypher2sql.cypher.ast.expression;

import org.cytosm.cypher2sql.utils.StringEscapeUtils;

/**
 */
public class Variable extends Expression {

    public String name;

    public Variable(final String value) {
        final String str = value.startsWith("`") ? value.substring(1, value.length() - 1) : value;
        this.name = StringEscapeUtils.unescapeCypherName(str);
    }
}
