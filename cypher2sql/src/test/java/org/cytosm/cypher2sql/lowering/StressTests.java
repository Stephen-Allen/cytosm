package org.cytosm.cypher2sql.lowering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.cytosm.common.gtop.GTopInterface;
import org.cytosm.common.gtop.RelationalGTopInterface;
import org.cytosm.cypher2sql.PassAvailables;
import org.junit.jupiter.api.Test;

/**
 * Put here any tests that stress a particular feature or
 * that should work unconditionally.
 *
 */
class StressTests {

    private GTopInterface getGTopInterface() throws IOException {
        Path path = Path.of("src", "test", "resources", "northwind.gtop");
        String jsonInString = Files.readString(path);
        return new RelationalGTopInterface(jsonInString);
    }


    @Test
    void matchWithWithWithWithWith() throws Exception {
        GTopInterface gTopInterface = getGTopInterface();
        String query =
                "MATCH (person:Employees) " +
                        "WITH person " +
                        "WITH person " +
                        "WITH person " +
                        "WITH person " +
                        "WITH person " +
                        "RETURN person.firstName";
        PassAvailables.cypher2sqlOnExpandedPaths(gTopInterface, query);
    }

    @Test
    void matchWithMatchWithWithWith() throws Exception {
        GTopInterface gTopInterface = getGTopInterface();
        String query =
                "MATCH (person:Employees) " +
                        "WITH person " +
                        "MATCH (person) " +
                        "WITH person " +
                        "WITH person " +
                        "WITH person " +
                        "RETURN person.firstName";
        PassAvailables.cypher2sqlOnExpandedPaths(gTopInterface, query);
    }
}
