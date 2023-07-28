package org.cytosm.cypher2sql.lowering.typeck.var;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.cytosm.cypher2sql.cypher.ast.ASTNode;
import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.NodePattern;
import org.cytosm.cypher2sql.cypher.ast.clause.match.pattern.RelationshipPattern;

/**
 */
public abstract class NodeOrTempOrRelVar extends Var {

    /**
     * Labels (may be empty if none are assigned)
     */
    public List<String> labels;

    NodeOrTempOrRelVar(ASTNode node) {
        super(node);
        this.labels = getLabels(node);
    }

    protected NodeOrTempOrRelVar(final Collection<String> labels) {
        this.labels = (null != labels) ? new ArrayList<>(labels) : new ArrayList<>();
    }

    private static List<String> getLabels(ASTNode n) {
        if (n instanceof NodePattern) {
            final NodePattern np = (NodePattern) n;
            if (CollectionUtils.isNotEmpty(np.labels)) return np.labels.stream().map(ln -> ln.name).collect(Collectors.toList());
        }
        else if (n instanceof RelationshipPattern) {
            final RelationshipPattern rp = (RelationshipPattern) n;
            if (CollectionUtils.isNotEmpty(rp.types)) return rp.types.stream().map(rtn -> rtn.name).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
