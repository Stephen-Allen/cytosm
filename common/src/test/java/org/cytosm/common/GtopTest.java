package org.cytosm.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cytosm.common.gtop.GTop;
import org.cytosm.common.gtop.GTopInterface;
import org.cytosm.common.gtop.RelationalGTopInterface;
import org.cytosm.common.gtop.abstraction.AbstractionEdge;
import org.cytosm.common.gtop.abstraction.AbstractionLevelGtop;
import org.cytosm.common.gtop.abstraction.AbstractionNode;
import org.cytosm.common.gtop.implementation.graphmetadata.BackendSystem;
import org.cytosm.common.gtop.implementation.graphmetadata.GraphMetadata;
import org.cytosm.common.gtop.implementation.graphmetadata.StorageLayout;
import org.cytosm.common.gtop.implementation.relational.Attribute;
import org.cytosm.common.gtop.implementation.relational.EdgeAttribute;
import org.cytosm.common.gtop.implementation.relational.ImplementationEdge;
import org.cytosm.common.gtop.implementation.relational.ImplementationLevelGtop;
import org.cytosm.common.gtop.implementation.relational.ImplementationNode;
import org.cytosm.common.gtop.implementation.relational.NodeIdImplementation;
import org.cytosm.common.gtop.implementation.relational.RestrictionClauses;
import org.cytosm.common.gtop.implementation.relational.TraversalHop;
import org.cytosm.common.gtop.implementation.relational.TraversalPath;
import org.cytosm.common.gtop.io.SerializationInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GtopTest {

    @Test
    void emptyGTopSerializationProcess() throws IOException {

        // produce abstraction section;
        List<AbstractionNode> aNodes = new ArrayList<>();
        List<AbstractionEdge> aEdges = new ArrayList<>();
        AbstractionLevelGtop abslevel = new AbstractionLevelGtop(aNodes, aEdges);

        // produce implementation section
        List<ImplementationNode> iNodes = new ArrayList<>();
        List<ImplementationEdge> iEdges = new ArrayList<>();
        GraphMetadata gdata = new GraphMetadata(StorageLayout.IGNORETIME, BackendSystem.RELATIONAL);
        ImplementationLevelGtop implevel = new ImplementationLevelGtop(gdata, iNodes, iEdges);

        GTop gTop = new GTop(abslevel, implevel);

        ObjectMapper mapper = new ObjectMapper();

        // Object to JSON in String
        String jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(gTop);

        System.out.println(jsonInString);

        GTop gTop2 = mapper.readValue(jsonInString, GTop.class);

        // assert they are the same
        assertEquals(gTop.getVersion(), gTop2.getVersion());
        assertEquals(gTop.getAbstractionLevel(), gTop2.getAbstractionLevel());
        assertEquals(gTop.getImplementationLevel(), gTop2.getImplementationLevel());
    }

    @Test
    void filledGtop() throws IOException {
        // produce abstraction section;
        List<AbstractionNode> aNodes = new ArrayList<>();
        List<AbstractionEdge> aEdges = new ArrayList<>();
        AbstractionNode aNode1 = new AbstractionNode(List.of("node1"), List.of("nodeAttribute1"));
        AbstractionNode aNode2 = new AbstractionNode(List.of("node2"), List.of("nodeAttribute2"));
        AbstractionEdge aEdge =
                new AbstractionEdge(List.of("edge1"), List.of("edgeAttribute1"), List.of("node1"),
                    List.of("node2"), true);
        aNodes.add(aNode1);
        aNodes.add(aNode2);
        aEdges.add(aEdge);

        AbstractionLevelGtop abslevel = new AbstractionLevelGtop(aNodes, aEdges);

        // produce implementation section
        List<ImplementationNode> iNodes = new ArrayList<>();
        List<ImplementationEdge> iEdges = new ArrayList<>();

        List<Attribute> node1Attributes = List.of(new Attribute("attributeColumn", "kibe", "VARCHAR(10)"));
        List<RestrictionClauses> node1Restrictions = List.of(new RestrictionClauses());
        ImplementationNode iNode1 =
                new ImplementationNode(List.of("node1"), "node1Table", List.of(new NodeIdImplementation(
                    "node1IdColumnName", "VARCHAR(100)", 1)), node1Attributes, node1Restrictions);

        List<Attribute> node2Attributes = List.of(new Attribute("attributeColumn", "pao", "VARCHAR(10)"));
        ImplementationNode iNode2 =
                new ImplementationNode(List.of("node2"), "node2Table", List.of(new NodeIdImplementation(
                    "node2IdColumnName", "VARCHAR(100)", 1)), node2Attributes, node1Restrictions);

        iNodes.add(iNode1);
        iNodes.add(iNode2);

        List<EdgeAttribute> eAttributes = List.of(new EdgeAttribute());
        List<TraversalPath> paths =
            List.of(new TraversalPath(List.of(new TraversalHop("edgeTable", "edgeSourceTableColumn",
                "edgejoinTableSourceColumn", "edgeJoinTableName", "edgeJoinTableDestinationColumn",
                "edgeDestinationTableColumn", "edgeDestionationTableName", eAttributes, 1, null))));
        ImplementationEdge iEdge = new ImplementationEdge(List.of("edge1"), paths);

        iEdges.add(iEdge);

        GraphMetadata gdata = new GraphMetadata(StorageLayout.IGNORETIME, BackendSystem.RELATIONAL);
        ImplementationLevelGtop implevel = new ImplementationLevelGtop(gdata, iNodes, iEdges);

        /*
         * List<Attribute> attributes = new ArrayList<>(); Attribute field = new Attribute();
         * field.setColumnName("tableName.colName"); field.setDataType("dataType");
         * field.setFieldName("fieldName"); attributes.add(field);
         *
         * List<String> labels = new ArrayList<>(); labels.add("label1"); labels.add("label2");
         *
         * List<Restriction> restrictions = new ArrayList<>(); Restriction restriction = new
         * Restriction("tableName.columnName", "pattern"); restrictions.add(restriction);
         *
         * node.setAttributes(attributes); node.setTableName("tableName");
         * node.setIdColumn("idColumn"); node.setTypes(labels);
         * node.setRestrictions(restrictions); nodes.add(node);
         *
         * Edge edge = new Edge(); edge.setEdgeMappedTable("edgeTable");
         * edge.setDestinationTableColumn("destination"); edge.setSourceTableName("tableName");
         * edge.setSourceTableColumn("columnName"); List<String> types = new ArrayList<>();
         * types.add("myEdgeTable");
         *//*
           *
           * edge.setTypes(types);
           *
           * edges.add(edge);
           */

        GTop gTop = new GTop(abslevel, implevel);

        GTopInterface gInterface = new RelationalGTopInterface(gTop);

        // Object to JSON in String - pretty-printed
        String jsonInString = SerializationInterface.toPrettyString(gTop);

        // Dump to console ...
        System.out.println(jsonInString);

        // Read value from screen
        GTopInterface gInterfaceFromString = new RelationalGTopInterface(jsonInString);

        // Check the integrity of reading value back ...

        // whole levels:
        //Assert.assertEquals(gTop.getAbstractionLevel(), gtopFromString.getAbstractionLevel());

        //Assert.assertEquals(gTop.getImplementationLevel(), gtopFromString.getImplementationLevel());

        // abstract edges:

        assertEquals(gInterface.getAbstractionEdgesByTypes("edge1").get(0).getDestinationType().get(0),
                gInterfaceFromString.getAbstractionEdgesByTypes("edge1").get(0).getDestinationType().get(0));

        assertEquals(gInterface.getAbstractionEdgesByTypes("edge1").get(0).getSourceType().get(0),
                gInterfaceFromString.getAbstractionEdgesByTypes("edge1").get(0).getSourceType().get(0));

        assertEquals(gInterface.getAbstractionEdgesByTypes("edge1").get(0).isDirected(), gInterfaceFromString
                .getAbstractionEdgesByTypes("edge1").get(0).isDirected());

        assertEquals(gInterface.getAbstractionEdgesByTypes("edge1").get(0).getTypes().get(0), gInterfaceFromString
                .getAbstractionEdgesByTypes("edge1").get(0).getTypes().get(0));

        AbstractionNode nod1 = gInterface.getAbstractionNodesByTypes("node1").get(0);
        AbstractionNode nod2 = gInterface.getAbstractionNodesByTypes("node2").get(0);

        List<AbstractionNode> allNodes = gInterface.getAbstractionNodes();
        List<AbstractionNode> sourceNodes =
                gInterface.getSourceNodesForEdge(gInterface.getAbstractionEdgesByTypes("edge1").get(0));
        List<AbstractionNode> destinationNodes =
                gInterface.getDestinationNodesForEdge(gInterface.getAbstractionEdgesByTypes("edge1").get(0));
        List<AbstractionNode> allNodesFromEdge =
                gInterface.getNodesForEdge(gInterface.getAbstractionEdgesByTypes("edge1").get(0));

        assertTrue(allNodes.contains(nod1) && allNodes.contains(nod2));
        assertTrue(sourceNodes.contains(nod1) && sourceNodes.size() == 1);
        assertTrue(destinationNodes.contains(nod2) && destinationNodes.size() == 1);
        assertTrue(allNodesFromEdge.size() == 2 && allNodesFromEdge.contains(nod1)
                && allNodesFromEdge.contains(nod2));

        //Assert.assertEquals(impedg1, impedg2);

        // abstract Nodes:
        assertEquals(gInterface.getAbstractionNodesByTypes("node1"),
                gInterfaceFromString.getAbstractionNodesByTypes("node1"));

        assertNotEquals(gInterface.getAbstractionNodesByTypes("node1"),
                gInterfaceFromString.getAbstractionNodesByTypes("node2"));

        //Assert.assertEquals(gTop.getAbstractionNodes(), gtopFromString.getAbstractionNodes());

        //Assert.assertEquals(impnd1.get(0), impnd2.get(0));

        // Graph metadata
        assertEquals(gTop.getImplementationLevel().getGraphMetadata(), gInterfaceFromString.getGraphMetadata());
    }

}
