package org.cytosm.cypher2sql.cypher.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.cytosm.cypher2sql.cypher.ast.Statement;
import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.Pattern;

/**
 */
public class ASTBuilder {

    /**
     * Parse the provided Cypher string and converts it into an AST.
     * @param cypher is the cypher to parse.
     * @return Returns the generated AST.
     */
    public static Statement parse(String cypher) {
        CharStream input = CharStreams.fromString(cypher);
        CypherLexer lexer = new CypherLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CypherParser parser = new CypherParser(tokens);
        return parser.cypher().res;
    }

    /**
     * Parse the provided pattern string and converts it into an AST.
     * @param pattern is the pattern to parse.
     * @return Returns the generated AST.
     */
    public static Pattern parsePattern(String pattern) {
        CharStream input = CharStreams.fromString(pattern);
        CypherLexer lexer = new CypherLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CypherParser parser = new CypherParser(tokens);
        return parser.pattern().res;
    }
}
