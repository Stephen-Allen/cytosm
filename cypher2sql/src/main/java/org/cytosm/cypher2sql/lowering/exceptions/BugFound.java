package org.cytosm.cypher2sql.lowering.exceptions;

/**
 * Raise this exception when a bug is found.
 */
public class BugFound extends Cypher2SqlException {

    public BugFound(String message) {
        super(message);
    }

    public BugFound(String message, Exception e) {
        super(message, e);
    }

    public BugFound(Exception e) {
        super(e);
    }
}
