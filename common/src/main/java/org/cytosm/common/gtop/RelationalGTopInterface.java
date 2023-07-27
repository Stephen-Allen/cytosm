package org.cytosm.common.gtop;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cytosm.common.gtop.abstraction.AbstractionEdge;
import org.cytosm.common.gtop.abstraction.AbstractionNode;
import org.cytosm.common.gtop.implementation.graphmetadata.GraphMetadata;
import org.cytosm.common.gtop.implementation.relational.ImplementationEdge;
import org.cytosm.common.gtop.implementation.relational.ImplementationNode;

/***
 * Relational implementation of gTop.
 *
 *
 */
public class RelationalGTopInterface extends AbstractGTopInterface {


    /**
     * Gtop that is being accessed.
     */
    protected final GTop gtop;

    // Constructors:
    /***
     * Default constructor.
     *
     * @param gtopLoaded loaded gtop
     */
    public RelationalGTopInterface(final GTop gtopLoaded) {
        gtop = gtopLoaded;
    }

    /**
     * Reads GTop from a file.
     *
     * @param fileObj gtop file
     * @throws IOException I/O Exception
     * @throws JsonMappingException JsonMappingException
     * @throws JsonParseException JsonParseException
     */
    public RelationalGTopInterface(final File fileObj) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        gtop = mapper.readValue(fileObj, GTop.class);
    }


    /**
     * Reads Gtop from a string.
     *
     * @param gTopStr gtop file in a string
     */
    public RelationalGTopInterface(final String gTopStr) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        gtop = mapper.readValue(gTopStr, GTop.class);
    }

    @Override
    public String getVersion() {
        return gtop.getVersion();
    }

    @Override
    public GraphMetadata getGraphMetadata() {
        return gtop.getImplementationLevel().getGraphMetadata();
    }

    /**
     * @return the abstraction nodes
     */
    @Override
    @JsonIgnore
    public List<AbstractionNode> getAbstractionNodes() {
        return gtop.getAbstractionLevel().getAbstractionNodes();
    }

    /**
     * @return the edges
     */
    @Override
    @JsonIgnore
    public List<AbstractionEdge> getAbstractionEdges() {
        return gtop.getAbstractionLevel().getAbstractionEdges();
    }

    /**
     * @return the implementation nodes
     */
    @Override
    @JsonIgnore
    public List<ImplementationNode> getImplementationNodes() {
        return gtop.getImplementationLevel().getImplementationNodes();
    }

    /**
     * @return the edges
     */
    @Override
    @JsonIgnore
    public List<ImplementationEdge> getImplementationEdges() {
        return gtop.getImplementationLevel().getImplementationEdges();
    }


}
