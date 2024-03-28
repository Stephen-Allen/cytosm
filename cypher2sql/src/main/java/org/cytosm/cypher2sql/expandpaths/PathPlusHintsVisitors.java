package org.cytosm.cypher2sql.expandpaths;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.cytosm.cypher2sql.cypher.ast.ASTNode;
import org.cytosm.cypher2sql.cypher.ast.Span;
import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.Pattern;
import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.PatternPart;
import org.cytosm.cypher2sql.cypher.ast.expression.Expression;
import org.cytosm.cypher2sql.cypher.visitor.Walk;

/**
 * Collect all the relationship chains that exists.
 */
public class PathPlusHintsVisitors {

    private static class ClauseVisitor extends Walk.BaseRootVisitor {
        String originalCypher;
        List<String> relationshipchains = new ArrayList<>();

        ClauseVisitor(String originalCypher) {
            this.originalCypher = originalCypher;
        }

        @Override
        public void visitPattern(Pattern pattern) {
            for (PatternPart pp : pattern.patternParts) {
                addRelchain(pp.span);
            }
        }

        @Override
        public void visitExpression(Expression expression) {}

        void parseClause(ASTNode clause) {
            Walk.walk(this, clause);
        }

        private void addRelchain(Span span) {
            relationshipchains.add(originalCypher.substring(span.lo, span.hi));
        }

    }

    public static void parse(ASTNode clause, List<PathPlusHints> matches, String originalCypher) {
        ClauseVisitor matchVisitor = new ClauseVisitor(originalCypher);
        matchVisitor.parseClause(clause);
        matches.addAll(matchVisitor.relationshipchains.stream()
                .map(PathPlusHints::new).collect(Collectors.toList()));
    }
}
