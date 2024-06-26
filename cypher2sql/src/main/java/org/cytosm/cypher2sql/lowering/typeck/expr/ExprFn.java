package org.cytosm.cypher2sql.lowering.typeck.expr;

import java.util.List;
import java.util.stream.Collectors;

import org.cytosm.cypher2sql.lowering.rendering.RenderingContext;

/**
 * Representation of functions. Functions have a specific treatment
 * as variables because the mapping is more complex than the rest
 * of the expression tree.
 *
 * In particular COUNT function in Cypher is transformed in a mixed
 * of COUNT and SUM functions in SQL.
 */
public class ExprFn implements Expr {

    /**
     * The name of the function.
     * We only represent SQL functions here.
     */
    public enum Name {
        COUNT,
        SUM,
        LOWER,
        UPPER,
    }

    /**
     * The function name as when being rendered.
     */
    public Name name;

    /**
     * The original Cypher function name if there's any.
     * Might be null.
     */

    public String cypherName;

    /**
     * The list of arguments.
     */
    public List<Expr> args;

    ExprFn(String cypherName, List<Expr> args) {
        this.cypherName = cypherName;
        this.args = args;
    }

    public ExprFn(Name name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }


    public String toSQLString(RenderingContext ctx) {
        String args = this.args.stream()
                .map(x -> x.toSQLString(ctx))
                .collect(Collectors.joining(", "));

        switch (name) {
            case COUNT: return "count(" + args + ")";
            case SUM: return "sum(" + args + ")";
            case LOWER: return "lower(" + args + ")";
            case UPPER: return "upper(" + args + ")";
            default: throw new RuntimeException("Unimplemented code reached");
        }
    }

}
