package org.cytosm.common.gtop;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cytosm.common.gtop.abstraction.AbstractionEdge;
import org.cytosm.common.gtop.abstraction.AbstractionNode;
import org.cytosm.common.gtop.implementation.graphmetadata.GraphMetadata;
import org.cytosm.common.gtop.implementation.relational.ImplementationEdge;
import org.cytosm.common.gtop.implementation.relational.ImplementationNode;

/***
 * Default interface to access a gtop file.
 *
 */
public interface GTopInterface {

    /**
     * @return the version
     */
    String getVersion();

    /**
     * @return the graph metadata for the implementation level
     */
    GraphMetadata getGraphMetadata();

    /**
     * @return the abstraction nodes
     */
    @JsonIgnore
    List<AbstractionNode> getAbstractionNodes();

    /**
     * @return the edges
     */
    @JsonIgnore
    List<AbstractionEdge> getAbstractionEdges();

    /**
     * @return the implementation nodes
     */
    @JsonIgnore
    List<ImplementationNode> getImplementationNodes();

    /**
     * @return the edges
     */
    @JsonIgnore
    List<ImplementationEdge> getImplementationEdges();

    /***
     * @return a deduplicated edge types list.
     */
    @JsonIgnore
    List<String> getAllEdgeTypes();

    /***
     * @return a deduplicated edge types list.
     */
    @JsonIgnore
    List<String> getAllNodeTypes();

    /***
     * Finds the Abstraction Edges for a given types.
     *
     * @param types
     * @return
     */
    @JsonIgnore
    List<AbstractionEdge> getAbstractionEdgesByTypes(final String types);


    /**
     * Finds an abstract node by types.
     *
     * @param types
     * @return
     */
    @JsonIgnore
    List<AbstractionNode> getAbstractionNodesByTypes(final String types);

    /**
     * Finds an Implementation nodes by type.
     *
     * @param type
     * @return
     */
    @JsonIgnore
    List<ImplementationNode> getImplementationNodesByType(final String type);

    /***
     * Finds an Implementation edges by type.
     *
     * @param type
     * @return
     */
    @JsonIgnore
    List<ImplementationEdge> getImplementationEdgeByType(final String type);

    /***
     * Return the implementations for a given node. The node can be represented in several tables.
     *
     * @param node
     * @return
     */
    @JsonIgnore
    List<ImplementationNode> findNodeImplementations(final AbstractionNode node);

    /***
     * Return the abstraction for a given node.
     *
     * @param node
     * @return
     */
    @JsonIgnore
    List<AbstractionNode> findNodeAbstractions(final ImplementationNode node);


    /***
     * Return the implementations for a given edge.
     *
     * @param edge
     * @return
     */
    @JsonIgnore
    ImplementationEdge findEdgeImplementation(final AbstractionEdge edge);

    /**
     * Get the edges that connect nodeA to nodeB, including directed and undirected.
     *
     * @param nodeA nodeA to look for
     * @param nodeB nodeB to look for
     * @return list of Abstraction Edges
     */
    @JsonIgnore
    List<AbstractionEdge> getAllAbstractEdgesBetweenTwoNodes(final AbstractionNode nodeA, final AbstractionNode nodeB);

    /**
     * Get the edges that start or end with node.
     *
     * @param types node types to look for
     * @return list of Abstraction Edges
     */
    @JsonIgnore
    List<AbstractionEdge> getAllAbstractEdgesForNodeTypes(final List<String> types);

    /**
     * Get the edges that connect nodeA to nodeB and are directed.
     *
     * @param sourceNode
     * @param destinationNode
     * @return list of Abstraction Edges
     */
    @JsonIgnore
    List<AbstractionEdge> getDirectedAbstractEdgesBetweenTwoNodes(final AbstractionNode sourceNode,
            final AbstractionNode destinationNode);

    /**
     * Return source node types for a given edge.
     *
     * @param analyzedEdge edge to lookup
     * @return set of two nodes for this edge
     */
    @JsonIgnore
    List<AbstractionNode> getSourceNodesForEdge(final AbstractionEdge analyzedEdge);

    /**
     * Return destination node types for a given edge.
     *
     * @param analyzedEdge edge to lookup
     * @return list node types that this edge directs to
     */
    @JsonIgnore
    List<AbstractionNode> getDestinationNodesForEdge(final AbstractionEdge analyzedEdge);

    /**
     * Return all node types associated with a given edge.
     *
     * @param edge edge to lookup
     * @return List of nodes types associated with this edge
     */
    @JsonIgnore
    List<AbstractionNode> getNodesForEdge(final AbstractionEdge edge);

    /**
     * Generates the abstract equivalent of the implementation.
     *
     * @param node implementation node
     * @return abstraction node equivalent to the implementation
     */
    @JsonIgnore
    AbstractionNode createAbstractionNodeFromImplementation(final ImplementationNode node);

    /**
     * Generates the abstract equivalent of the implementation. Since the implementation has no information about the
     * edge being directed or not, always assume that the edge is undirected. The implementation also has no information
     * about the source type and destination type. Thus this should be added later, if required.
     *
     * @param edge implementation edge
     * @return abstraction edge equivalent to the implementation
     */
    @JsonIgnore
    AbstractionEdge createAbstractionEdgeFromImplementation(final ImplementationEdge edge);
}
