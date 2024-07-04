package org.cytosm.cypher2sql.lowering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.cytosm.common.gtop.GTopInterface;
import org.cytosm.common.gtop.RelationalGTopInterface;

/**
 */
public class BaseLDBCTests {

    protected GTopInterface getGTopInterface() throws IOException {
        Path path = Path.of("src", "test", "resources", "ldbc.gtop");
        String jsonInString = Files.readString(path);
        return new RelationalGTopInterface(jsonInString);
    }

}
