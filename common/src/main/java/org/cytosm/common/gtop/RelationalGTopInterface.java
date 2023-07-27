package org.cytosm.common.gtop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.cytosm.common.gtop.abstraction.AbstractionEdge;
import org.cytosm.common.gtop.abstraction.AbstractionNode;
import org.cytosm.common.gtop.implementation.relational.ImplementationEdge;
import org.cytosm.common.gtop.implementation.relational.ImplementationNode;

/***
 * Relational implementation of gTop.
 *
 *
 */
public class RelationalGTopInterface extends GTopInterfaceImpl {


    // Constructors:
    /***
     * Default constructor.
     *
     * @param gtopLoaded loaded gtop
     */
    public RelationalGTopInterface(final GTop gtopLoaded) {
        super(gtopLoaded);
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
        super(fileObj);
    }


    /**
     * Reads Gtop from a string.
     *
     * @param gTopStr gtop file in a string
     */
    public RelationalGTopInterface(final String gTopStr) throws IOException {
        super(gTopStr);
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

    /***
     * Finds an Implementation Node by type.
     *
     * @param type
     * @return
     */
    @Override
    @JsonIgnore
    public List<ImplementationNode> getImplementationNodesByType(final String type) {
        List<ImplementationNode> foundNodes = new ArrayList<>();
        if (type == null) {
            return foundNodes;
        }
        foundNodes = gtop
                .getImplementationLevel().getImplementationNodes().stream().filter(node -> node.getTypes().stream()
                        .map(String::toLowerCase).collect(Collectors.toList()).contains(type.toLowerCase()))
                .collect(Collectors.toList());

        return foundNodes;
    }

    /***
     * Finds an Implementation edge by type.
     *
     * @param type
     * @return
     */
    @Override
    @JsonIgnore
    public List<ImplementationEdge> getImplementationEdgeByType(final String type) {
        List<ImplementationEdge> foundEdge = new ArrayList<>();
        if (type == null) {
            return foundEdge;
        }

        foundEdge = gtop
                .getImplementationLevel().getImplementationEdges().stream().filter(edge -> edge.getTypes().stream()
                        .map(String::toLowerCase).collect(Collectors.toList()).contains(type.toLowerCase()))
                .collect(Collectors.toList());

        return foundEdge;
    }

    /***
     * Return the implementations for a given node. The node can be represented in several tables.
     *
     * @param node abstraction node
     * @return
     */
    @Override
    @JsonIgnore
    public List<ImplementationNode> findNodeImplementations(final AbstractionNode node) {
        List<ImplementationNode> implementation = new ArrayList<>();

        // if the implementation matches any of the abstraction level types, append to list.
        implementation = gtop.getImplementationLevel().getImplementationNodes().stream()
                .filter(filteredNode -> !Collections.disjoint(filteredNode.getTypes(), node.getTypes()))
                .collect(Collectors.toList());

        return implementation;
    }



    /***
     * Return the implementations for a given edge.
     *
     * @param edge
     * @return
     */
    @Override
    @JsonIgnore
    public ImplementationEdge findEdgeImplementation(final AbstractionEdge edge) {
        ImplementationEdge implementation = null;

        for (ImplementationEdge analyzedEdge : gtop.getImplementationLevel().getImplementationEdges()) {
            if (!Collections.disjoint(edge.getTypes(), analyzedEdge.getTypes())) {
                implementation = analyzedEdge;
                break;
            }
        }

        return implementation;
    }

}
