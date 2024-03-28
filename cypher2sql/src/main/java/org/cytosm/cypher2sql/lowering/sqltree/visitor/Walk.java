package org.cytosm.cypher2sql.lowering.sqltree.visitor;

import java.util.ArrayList;
import java.util.List;

import org.cytosm.cypher2sql.lowering.exceptions.Cypher2SqlException;
import org.cytosm.cypher2sql.lowering.sqltree.BaseSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SQLNode;
import org.cytosm.cypher2sql.lowering.sqltree.ScopeSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleOrScopeSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelect;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelectWithInnerJoins;
import org.cytosm.cypher2sql.lowering.sqltree.SimpleSelectWithLeftJoins;
import org.cytosm.cypher2sql.lowering.sqltree.UnionSelect;
import org.cytosm.cypher2sql.lowering.sqltree.WithSelect;
import org.cytosm.cypher2sql.lowering.sqltree.join.InnerJoin;
import org.cytosm.cypher2sql.lowering.sqltree.join.LeftJoin;
import org.cytosm.cypher2sql.lowering.typeck.expr.Expr;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprWalk;

/**
 * Walking utilities.
 */
public class Walk {

    // ==================================================================
    //                          VISITORs
    // ==================================================================

    public interface SQLNodeVisitor<E extends Throwable> {
        void visitLeftJoin(LeftJoin leftJoin) throws E;
        void visitInnerJoin(InnerJoin innerJoin) throws E;
        void visitSimpleSelect(SimpleSelect simpleSelect) throws E;
        void visitScopeSelect(ScopeSelect scopeSelect) throws E;
        void visitUnionSelect(UnionSelect unionSelect) throws E;
        void visitWithSelect(WithSelect withSelect) throws E;
    }

    public static class BaseSQLNodeVisitor implements SQLNodeVisitor<Cypher2SqlException> {

        @Override
        public void visitLeftJoin(LeftJoin leftJoin) throws Cypher2SqlException {}

        @Override
        public void visitInnerJoin(InnerJoin innerJoin) throws Cypher2SqlException {}

        @Override
        public void visitSimpleSelect(SimpleSelect simpleSelect) throws Cypher2SqlException {
            if (simpleSelect instanceof SimpleSelectWithInnerJoins) {
                for (InnerJoin innerJoin: ((SimpleSelectWithInnerJoins) simpleSelect).joins) {
                    visitInnerJoin(innerJoin);
                }
            } else {
                for (LeftJoin leftJoin: ((SimpleSelectWithLeftJoins) simpleSelect).joins) {
                    visitLeftJoin(leftJoin);
                }
            }
        }

        @Override
        public void visitScopeSelect(ScopeSelect scopeSelect) throws Cypher2SqlException {
            for (WithSelect withQuery : scopeSelect.withQueries) {
                visitWithSelect(withQuery);
            }
            walkSQLNode(this, scopeSelect.ret);
        }

        @Override
        public void visitUnionSelect(UnionSelect unionSelect) throws Cypher2SqlException {
            for (SimpleOrScopeSelect s: unionSelect.unions) {
                walkSQLNode(this, s);
            }
        }

        @Override
        public void visitWithSelect(WithSelect withSelect) throws Cypher2SqlException {
            walkSQLNode(this, withSelect.subquery);
        }
    }

    public static abstract class BaseVisitorAndExprVisitor implements SQLNodeVisitor<Cypher2SqlException> {

        protected abstract ExprWalk.Visitor makeExprVisitor();

        @Override
        public void visitLeftJoin(LeftJoin leftJoin) throws Cypher2SqlException {}

        @Override
        public void visitInnerJoin(InnerJoin innerJoin) throws Cypher2SqlException {}

        @Override
        public void visitSimpleSelect(SimpleSelect simpleSelect) throws Cypher2SqlException {
            ExprWalk.Visitor visitor = makeExprVisitor();
            for (Expr e : simpleSelect.exportedItems) {
                ExprWalk.walk(visitor, e);
            }

            if (simpleSelect.whereCondition != null) {
                ExprWalk.walk(visitor, simpleSelect.whereCondition);
            }
            for (SimpleSelect.OrderItem oi : simpleSelect.orderBy) {
                ExprWalk.walk(visitor, oi.item);
            }

            if (simpleSelect instanceof SimpleSelectWithInnerJoins) {
                for (InnerJoin j : ((SimpleSelectWithInnerJoins) simpleSelect).joins) {
                    ExprWalk.walk(visitor, j.condition);
                }
            }
            else {
                for (LeftJoin j : ((SimpleSelectWithLeftJoins) simpleSelect).joins) {
                    ExprWalk.walk(visitor, j.condition);
                }
            }
        }

        @Override
        public void visitScopeSelect(ScopeSelect scopeSelect) throws Cypher2SqlException {
            for (WithSelect withQuery : scopeSelect.withQueries) {
                visitWithSelect(withQuery);
            }
            walkSQLNode(this, scopeSelect.ret);
        }

        @Override
        public void visitUnionSelect(UnionSelect unionSelect) throws Cypher2SqlException {
            for (SimpleOrScopeSelect s: unionSelect.unions) {
                walkSQLNode(this, s);
            }
        }

        @Override
        public void visitWithSelect(WithSelect withSelect) throws Cypher2SqlException {
            walkSQLNode(this, withSelect.subquery);
        }
    }

    public static abstract class BaseVisitorAndExprFolder implements SQLNodeVisitor<Cypher2SqlException>
    {

        protected abstract ExprWalk.IdentityFolder<Cypher2SqlException> makeExprFolder(SimpleSelect context);

        @Override
        public void visitLeftJoin(LeftJoin leftJoin) throws Cypher2SqlException {}

        @Override
        public void visitInnerJoin(InnerJoin innerJoin) throws Cypher2SqlException {}

        @Override
        public void visitSimpleSelect(SimpleSelect simpleSelect) throws Cypher2SqlException {
            ExprWalk.IdentityFolder<Cypher2SqlException> folder = makeExprFolder(simpleSelect);
            List<Expr> foldedExportItems = new ArrayList<>();
            for (Expr e : simpleSelect.exportedItems) {
                Expr fold = ExprWalk.fold(folder, e);
                foldedExportItems.add(fold);
            }
            simpleSelect.exportedItems = foldedExportItems;

            if (simpleSelect.whereCondition != null) {
                simpleSelect.whereCondition = ExprWalk.fold(folder, simpleSelect.whereCondition);
            }
            for (SimpleSelect.OrderItem oi : simpleSelect.orderBy) {
                oi.item = ExprWalk.fold(folder, oi.item);
            }

            if (simpleSelect instanceof SimpleSelectWithInnerJoins) {
                for (InnerJoin j : ((SimpleSelectWithInnerJoins) simpleSelect).joins) {
                    j.condition = ExprWalk.fold(folder, j.condition);
                }
            }
            else {
                for (LeftJoin j : ((SimpleSelectWithLeftJoins) simpleSelect).joins) {
                    j.condition = ExprWalk.fold(folder, j.condition);
                }
            }
        }

        @Override
        public void visitScopeSelect(ScopeSelect scopeSelect) throws Cypher2SqlException {
            for (WithSelect withQuery : scopeSelect.withQueries) {
                visitWithSelect(withQuery);
            }
            walkSQLNode(this, scopeSelect.ret);
        }

        @Override
        public void visitUnionSelect(UnionSelect unionSelect) throws Cypher2SqlException {
            for (SimpleOrScopeSelect s: unionSelect.unions) {
                walkSQLNode(this, s);
            }
        }

        @Override
        public void visitWithSelect(WithSelect withSelect) throws Cypher2SqlException {
            walkSQLNode(this, withSelect.subquery);
        }
    }

    public static <E extends Throwable> void walkSQLNode(final SQLNodeVisitor<E> visitor, final SQLNode node) throws E {
        if (node instanceof InnerJoin) {
            visitor.visitInnerJoin((InnerJoin) node);
        } else if (node instanceof LeftJoin) {
            visitor.visitLeftJoin((LeftJoin) node);
        } else if (node instanceof SimpleSelect) {
            visitor.visitSimpleSelect((SimpleSelect) node);
        } else if (node instanceof ScopeSelect) {
            visitor.visitScopeSelect((ScopeSelect) node);
        } else if (node instanceof UnionSelect) {
            visitor.visitUnionSelect((UnionSelect) node);
        } else if (node instanceof WithSelect) {
            visitor.visitWithSelect((WithSelect) node);
        }
    }

    // ==================================================================
    //                          FOLDERs
    // ==================================================================


    public interface Folder<T, E extends Throwable> {

        T foldSimpleSelect(SimpleSelect select) throws E;
        T foldScopeSelect(ScopeSelect scopeSelect) throws E;
        T foldUnionSelect(UnionSelect unionSelect) throws E;
        T foldWithSelect(WithSelect withSelect) throws E;
    }

    public static class IdentityFolder<E extends Throwable> implements Folder<BaseSelect, E> {
        @Override
        public BaseSelect foldSimpleSelect(SimpleSelect select) throws E {
            return select;
        }

        @Override
        public BaseSelect foldScopeSelect(ScopeSelect scopeSelect) throws E {
            ScopeSelect result = new ScopeSelect();
            for (WithSelect withSelect: scopeSelect.withQueries) {
                result.withQueries.add((WithSelect) fold(this, withSelect));
            }
            result.ret = (SimpleSelect) fold(this, scopeSelect.ret);
            return result;
        }

        @Override
        public BaseSelect foldUnionSelect(UnionSelect unionSelect) throws E {
            UnionSelect result = new UnionSelect();
            for (SimpleOrScopeSelect child: unionSelect.unions) {
                result.unions.add((SimpleOrScopeSelect) fold(this, child));
            }
            return result;
        }

        @Override
        public BaseSelect foldWithSelect(WithSelect withSelect) throws E {
            WithSelect result = new WithSelect(fold(this, withSelect.subquery));
            result.subqueryName = withSelect.subqueryName;
            return result;
        }
    }

    public static <T, E extends Throwable> T fold(Folder<T, E> folder, SQLNode node) throws E {
        if (node instanceof SimpleSelect) {
            return folder.foldSimpleSelect((SimpleSelect) node);
        } else if (node instanceof ScopeSelect) {
            return folder.foldScopeSelect((ScopeSelect) node);
        } else if (node instanceof UnionSelect) {
            return folder.foldUnionSelect((UnionSelect) node);
        } else if (node instanceof WithSelect) {
            return folder.foldWithSelect((WithSelect) node);
        } else {
            throw new RuntimeException("Unreachable code reached");
        }
    }
}
