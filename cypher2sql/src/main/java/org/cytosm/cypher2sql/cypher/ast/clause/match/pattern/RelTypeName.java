package org.cytosm.cypher2sql.cypher.ast.clause.match.pattern;

import org.cytosm.cypher2sql.cypher.ast.ASTNode;
import org.cytosm.cypher2sql.utils.StringEscapeUtils;

/**
 */
public class RelTypeName extends ASTNode {

    public String name;

    public RelTypeName(final String value) {
        final String str = value.startsWith("`") ? value.substring(1, value.length() - 1) : value;
        this.name = StringEscapeUtils.unescapeCypherName(str);
    }
}
