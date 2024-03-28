package org.cytosm.cypher2sql.lowering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.cytosm.common.gtop.GTopInterface;
import org.cytosm.common.gtop.implementation.relational.ImplementationNode;
import org.cytosm.cypher2sql.lowering.exceptions.Cypher2SqlException;
import org.cytosm.cypher2sql.lowering.sqltree.ScopeSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelect;
import org.cytosm.cypher2sql.lowering.sqltree.WithSelect;
import org.cytosm.cypher2sql.lowering.sqltree.visitor.Walk;
import org.cytosm.cypher2sql.lowering.typeck.constexpr.ConstVal;
import org.cytosm.cypher2sql.lowering.typeck.expr.Expr;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprFn;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprTree;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprVar;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprWalk;
import org.cytosm.cypher2sql.lowering.typeck.types.PathType;
import org.cytosm.cypher2sql.lowering.typeck.var.AliasVar;
import org.cytosm.cypher2sql.lowering.typeck.var.NodeVar;
import org.cytosm.cypher2sql.lowering.typeck.var.PathVar;
import org.cytosm.cypher2sql.lowering.typeck.var.Var;

/**
 * This class collect passes that transform functions
 * found into SQL equivalent. It contains passes that
 * will process individually each functions name. Some
 * will only affect the expression tree where they are
 * located, others needs to be ran conjointly to others
 * at specific point in the translation steps.
 *
 * For more details on each specific pass look at their
 * documentation.
 *
 */
public class TransformFunctions {

    /**
     * Look for all function COUNT uses and turn them as appropriate
     * into either a
     * @param tree is the SQL tree where COUNT will be updated.
     * @param gTopInterface is the implementation gTop.
     */
    public static void convertCypherCountFn(ScopeSelect tree, GTopInterface gTopInterface)
            throws Cypher2SqlException
    {
        HashMap<ScopeSelect, Boolean> scopeIsLeaf = new HashMap<>();
        Walk.walkSQLNode(new ScopeSelectIsLeaf(scopeIsLeaf), tree);
        Walk.walkSQLNode(new CountVisitor(scopeIsLeaf, gTopInterface), tree);
    }

    private static class ScopeSelectIsLeaf extends Walk.BaseSQLNodeVisitor {

        private final HashMap<ScopeSelect, Boolean> scopeIsLeaf;
        private ScopeSelect parentScope;

        ScopeSelectIsLeaf(HashMap<ScopeSelect, Boolean> scopeIsLeaf) {
            this.scopeIsLeaf = scopeIsLeaf;
        }

        @Override
        public void visitScopeSelect(ScopeSelect scopeSelect) throws Cypher2SqlException {
            ScopeSelect parentScope = this.parentScope;
            this.switchScopeSelect(scopeSelect);
            super.visitScopeSelect(scopeSelect);
            this.parentScope = parentScope;
        }

        private void switchScopeSelect(ScopeSelect newScope) {
            if (parentScope != null) {
                this.scopeIsLeaf.put(parentScope, false);
            }
            this.scopeIsLeaf.put(newScope, true);
            this.parentScope = newScope;
        }
    }

    private static class CountVisitor extends Walk.BaseSQLNodeVisitor {

        private static final String COUNT = "count";
        private final HashMap<ScopeSelect, Boolean> scopeIsLeaf;
        private final GTopInterface gtop;

        CountVisitor(HashMap<ScopeSelect, Boolean> scopeIsLeaf, GTopInterface gtop) {
            this.scopeIsLeaf = scopeIsLeaf;
            this.gtop = gtop;
        }

        @Override
        public void visitSimpleSelect(SimpleSelect simpleSelect) throws Cypher2SqlException {
            // In a simple select that does not represent a Cypher RETURN
            // (only the ScopeSelect "ret" can and it is not captured by that pass)
            // we always go from the cypher name count to the SQL count.

            NameFnExpr namer = new NameFnExpr(gtop, COUNT, ExprFn.Name.COUNT);
            for (Expr e : simpleSelect.exportedItems) {
                ExprWalk.walk(namer, e);
            }
        }

        @Override
        public void visitScopeSelect(ScopeSelect scopeSelect) throws Cypher2SqlException {
            NameFnExpr namer;
            if (this.scopeIsLeaf.get(scopeSelect)) {
                namer = new NameFnExpr(gtop, COUNT, ExprFn.Name.COUNT);
            }
            else {
                namer = new NameFnExpr(gtop, COUNT, ExprFn.Name.SUM);
            }
            for (Expr e : scopeSelect.ret.exportedItems) {
                ExprWalk.walk(namer, e);
            }
            for (WithSelect withQuery : scopeSelect.withQueries) {
                visitWithSelect(withQuery);
            }
        }
    }

    private static class NameFnExpr extends ExprWalk.BaseVisitor {

        private final String fnCypherName;
        private final ExprFn.Name fnName;
        private final GTopInterface gtop;

        NameFnExpr(GTopInterface gtop, String fnCypherName, ExprFn.Name fnName) {
            this.gtop = gtop;
            this.fnCypherName = fnCypherName;
            this.fnName = fnName;
        }

        @Override
        public void visitFn(ExprFn expr) {
            if (expr.cypherName.equalsIgnoreCase(fnCypherName)) {
                expr.name = fnName;
            }
            // FIXME: Is is always a correct way of folding the argument?
            expr.args = expr.args.stream()
                    .map(x -> {
                        if (x instanceof ExprVar) {
                            return new ExprTree.PropertyAccess(getIdForVar(((ExprVar) x).var), x);
                        }
                        return x;
                    }).collect(Collectors.toList());
        }

        private String getIdForVar(Var var) {
            var = AliasVar.resolveAliasVar(var);
            if (var instanceof NodeVar) {
                NodeVar nodeVar = (NodeVar) var;
                List<ImplementationNode> nodes = gtop.getImplementationNodesByType(nodeVar.labels.get(0));
                // FIXME: We should make sure that we always have *exactly* one node returned here.
                return nodes.get(0).getId().get(0).getColumnName();
            }
            throw new RuntimeException(
                    "Attempt to access the ID column on a variable" +
                    "that is not a Node."
            );
        }
    }


    /**
     * Transform all uses of length(p) into the length of the path.
     *
     * @param tree is the tree that where expression will be changed.
     * @throws Cypher2SqlException is thrown if an error is encountered
     */
    public static void convertPathLength(ScopeSelect tree) throws Cypher2SqlException {
        Walk.walkSQLNode(new PathLengthVisitor(), tree);
    }

    private static class PathLengthVisitor extends Walk.BaseSQLNodeVisitor {

        @Override
        public void visitSimpleSelect(SimpleSelect simpleSelect) throws Cypher2SqlException {
            PathLengthFolder folder = new PathLengthFolder();
            List<Expr> foldedExportItems = new ArrayList<>();
            for (Expr e : simpleSelect.exportedItems) {
                Expr fold = ExprWalk.fold(folder, e);
                foldedExportItems.add(fold);
            }
            simpleSelect.exportedItems = foldedExportItems;

            simpleSelect.whereCondition = ExprWalk.fold(folder, simpleSelect.whereCondition);
        }
    }

    private static class PathLengthFolder extends ExprWalk.IdentityFolder<Cypher2SqlException> {

        private static final String LENGTH = "length";

        @Override
        public Expr foldFn(ExprFn expr) throws Cypher2SqlException {
            if (LENGTH.equalsIgnoreCase(expr.cypherName)) {
                if (expr.args.size() == 1) {
                    Expr arg = expr.args.get(0);
                    if (arg instanceof ExprVar && ((ExprVar) arg).var.type() instanceof PathType) {
                        PathVar pathVar = (PathVar) AliasVar.resolveAliasVar(((ExprVar) arg).var);
                        return new ConstVal.LongVal(pathVar.length);
                    }
                }
            }
            return expr;
        }


    }

    /**
     * Transform all functions that have a straightforward translation into an equivalent SQL function
     *
     * @param tree is the tree that where expression will be changed.
     * @throws Cypher2SqlException is thrown if an error is encountered
     */
    public static void convertPassThroughFunctions(ScopeSelect tree) throws Cypher2SqlException {
        Walk.walkSQLNode(new PassThroughVisitor(), tree);
    }

    private static class PassThroughVisitor extends Walk.BaseSQLNodeVisitor {

        @Override
        public void visitSimpleSelect(SimpleSelect simpleSelect) throws Cypher2SqlException {
            PassThroughFolder folder = new PassThroughFolder();
            List<Expr> foldedExportItems = new ArrayList<>();
            for (Expr e : simpleSelect.exportedItems) {
                Expr fold = ExprWalk.fold(folder, e);
                foldedExportItems.add(fold);
            }
            simpleSelect.exportedItems = foldedExportItems;

            simpleSelect.whereCondition = ExprWalk.fold(folder, simpleSelect.whereCondition);
        }

        private static class PassThroughFolder extends ExprWalk.IdentityFolder<Cypher2SqlException> {
            @Override
            public Expr foldFn(ExprFn expr) throws Cypher2SqlException {
                if (expr.cypherName == null) return expr;
                switch (expr.cypherName.toLowerCase()) {
                    case "tolower" : return new ExprFn(ExprFn.Name.LOWER, expr.args);
                    case "toupper" : return new ExprFn(ExprFn.Name.UPPER, expr.args);
                    default: return expr;
                }
            }
        }
    }


}
