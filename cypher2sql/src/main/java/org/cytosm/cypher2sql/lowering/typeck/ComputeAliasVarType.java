package org.cytosm.cypher2sql.lowering.typeck;

import java.util.Map;

import org.cytosm.cypher2sql.lowering.exceptions.BugFound;
import org.cytosm.cypher2sql.lowering.exceptions.Cypher2SqlException;
import org.cytosm.cypher2sql.lowering.exceptions.TypeError;
import org.cytosm.cypher2sql.lowering.exceptions.Unimplemented;
import org.cytosm.cypher2sql.lowering.typeck.constexpr.ConstVal;
import org.cytosm.cypher2sql.lowering.typeck.expr.Expr;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprFn;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprTree;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprVar;
import org.cytosm.cypher2sql.lowering.typeck.expr.ExprWalk;
import org.cytosm.cypher2sql.lowering.typeck.types.AType;
import org.cytosm.cypher2sql.lowering.typeck.types.BoolType;
import org.cytosm.cypher2sql.lowering.typeck.types.MapType;
import org.cytosm.cypher2sql.lowering.typeck.types.NodeType;
import org.cytosm.cypher2sql.lowering.typeck.types.NumberType;
import org.cytosm.cypher2sql.lowering.typeck.types.RelType;
import org.cytosm.cypher2sql.lowering.typeck.types.StringType;
import org.cytosm.cypher2sql.lowering.typeck.var.AliasVar;
import org.cytosm.cypher2sql.lowering.typeck.var.Var;

/**
 */
public class ComputeAliasVarType {

    /**
     * Compute the type of all aliasVars that exists in VarDependencies.
     * @param vars is the variable dependency tracker.
     */
    public static void computeAliasVarTypes(VarDependencies vars) throws Cypher2SqlException {
        for (Var x : vars.getAllVariables()) {
            if (x instanceof AliasVar) {
                AliasVar aliasVar = (AliasVar) x;
                computeAliasVarType(aliasVar);
            }
        }
    }

    private static void computeAliasVarType(AliasVar var) throws Cypher2SqlException {
        if (var.type() == null) {
            AType type = ExprWalk.fold(new TypeFolder(), var.aliased);
            var.setVarType(type);
        }
    }

    private static class TypeFolder implements ExprWalk.Folder<AType, Cypher2SqlException> {

        @Override
        public AType foldBinaryOperator(ExprTree.LhsRhs expr) throws Cypher2SqlException {
            // FIXME: this doesn't check the entire expression
            if (expr instanceof ExprTree.GreaterThan ||
                expr instanceof ExprTree.GreaterThanOrEqual ||
                expr instanceof ExprTree.LessThan ||
                expr instanceof ExprTree.LessThanOrEqual ||
                expr instanceof ExprTree.In ||
                expr instanceof ExprTree.Or ||
                expr instanceof ExprTree.Xor ||
                expr instanceof ExprTree.And ||
                expr instanceof ExprTree.Eq ||
                expr instanceof ExprTree.Neq ||
                expr instanceof ExprTree.RegexMatch ||
                expr instanceof ExprTree.StartsWith ||
                expr instanceof ExprTree.EndsWith ||
                expr instanceof ExprTree.Contains) {
                return new BoolType();
            }
            return merge(ExprWalk.fold(this, expr.lhs), ExprWalk.fold(this, expr.rhs));
        }

        @Override
        public AType foldUnaryOperator(ExprTree.Unary expr) throws Cypher2SqlException {
            if (expr instanceof ExprTree.Not ||
                expr instanceof ExprTree.IsNotNull ||
                expr instanceof ExprTree.IsNull) {
                return new BoolType();
            }
            return ExprWalk.fold(this, expr.unary);
        }

        @Override
        public AType foldPropertyAccess(ExprTree.PropertyAccess expr) throws Cypher2SqlException {
            String prop = expr.propertyAccessed;
            AType objType = ExprWalk.fold(this, expr.expression);
            if (objType instanceof MapType) {
                return ((MapType) objType).fields.get(prop);
            }
            // FIXME: Could do better than this
            if (objType instanceof NodeType || objType instanceof RelType) {
                return new StringType();
            }
            // FIXME: Collect errors instead of failing abruptly.
            throw new TypeError("Can't access property of a non object.");
        }

        @Override
        public AType foldVariable(ExprVar expr) throws Cypher2SqlException {
            if (expr.var instanceof AliasVar) {
                return ExprWalk.fold(this, ((AliasVar) expr.var).aliased);
            } else {
                return expr.var.type();
            }
        }

        @Override
        public AType foldListExpr(ExprTree.ListExpr expr) throws Cypher2SqlException {
            throw new Unimplemented();
        }

        @Override
        public AType foldMapExpr(ExprTree.MapExpr expr) throws Cypher2SqlException {
            MapType type = new MapType();
            for (Map.Entry<String, Expr> entry: expr.props.entrySet()) {
                type.fields.put(entry.getKey(), ExprWalk.fold(this, entry.getValue()));
            }
            return type;
        }

        @Override
        public AType foldLiteral(ConstVal.Literal expr) throws Cypher2SqlException {
            if (expr instanceof ConstVal.StrVal) {
                return new StringType();
            } else {
                return new NumberType();
            }
        }

        @Override
        public AType foldFn(ExprFn expr) throws Cypher2SqlException {
            if (expr.cypherName.equalsIgnoreCase("count")) {
                return new NumberType();
            }
            throw new Unimplemented();
        }

        @Override
        public AType foldCaseExpr(ExprTree.CaseExpr expr) throws Cypher2SqlException {
            throw new Unimplemented();
        }

        @Override
        public AType foldAliasExpr(ExprTree.AliasExpr expr) throws Cypher2SqlException {
            throw new BugFound("Congratulations! You have created a bug.");
        }
    }

    private static AType merge(AType one, AType other) throws Cypher2SqlException {
        throw new Unimplemented();
    }
}
