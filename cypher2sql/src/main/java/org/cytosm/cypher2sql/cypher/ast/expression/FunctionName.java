package org.cytosm.cypher2sql.cypher.ast.expression;

import org.cytosm.cypher2sql.cypher.ast.ASTNode;
import org.cytosm.cypher2sql.utils.StringEscapeUtils;

/**
 */
public class FunctionName extends ASTNode {

    public String name;

    public FunctionName(final String value) {
        final String str = value.startsWith("`") ? value.substring(1, value.length() - 1) : value;
        this.name = StringEscapeUtils.unescapeCypherName(str);
    }
}
