package org.cytosm.cypher2sql.lowering.typeck.var;

import java.util.HashSet;
import java.util.Set;

import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.NodePattern;
import org.cytosm.cypher2sql.cypher.ast.expression.MapExpression;
import org.cytosm.cypher2sql.lowering.typeck.AvailableVariables;
import org.cytosm.cypher2sql.lowering.typeck.NameProvider;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprTree;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprTreeBuilder;
import org.cytosm.cypher2sql.lowering.typeck.types.AType;
import org.cytosm.cypher2sql.lowering.typeck.types.NodeType;

/**
 * Variable representing a node.
 */
public class NodeVar extends NodeOrRelVar {


    /**
     * Node can be restricted on some
     * properties using map expressions.
     */
    public ExprTree.MapExpr predicate;

    /**
     * This represent the future properties
     * that will be required to be transmitted between selects.
     */
    public Set<String> propertiesRequired = new HashSet<>();

    public AType type() {
        return new NodeType();
    }

    public NodeVar(NodePattern np, AvailableVariables vars) {
        super(np);
        this.name = NameProvider.getName(np);

        if (np.properties.isPresent()) {
            MapExpression mapExpression = np.properties.get();
            this.predicate = (ExprTree.MapExpr) ExprTreeBuilder.buildFromCypherExpression(
                    mapExpression, vars
            );
        }
    }
}
