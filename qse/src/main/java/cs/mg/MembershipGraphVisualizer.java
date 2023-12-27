//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.mg;

import cs.qse.common.encoders.NodeEncoder;
import cs.qse.common.encoders.StringEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import org.eclipse.rdf4j.model.util.Values;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class MembershipGraphVisualizer {
    public MembershipGraphVisualizer() {
    }

    public void createIntegerNodesGraph(DefaultDirectedGraph<Integer, DefaultEdge> directedGraph) {
        Neo4jGraph neo = new Neo4jGraph();
        directedGraph.iterables().vertices().forEach((vertex) -> {
            neo.addNode(String.valueOf(vertex));
        });
        directedGraph.iterables().edges().forEach((edge) -> {
            int source = (Integer)directedGraph.getEdgeTarget(edge);
            int target = (Integer)directedGraph.getEdgeSource(edge);
            neo.connectNodes(String.valueOf(source), String.valueOf(target));
        });
    }

    public void createStringNodesGraph(DefaultDirectedGraph<String, DefaultEdge> directedGraph) {
        Neo4jGraph neo = new Neo4jGraph();
        directedGraph.iterables().vertices().forEach(neo::addNode);
        directedGraph.iterables().edges().forEach(edge -> {
            String source = directedGraph.getEdgeTarget(edge);
            String target = directedGraph.getEdgeSource(edge);
            neo.connectNodes(source, target);
        });
    }

    public void createEncodedIRIsNodesGraph(DefaultDirectedGraph<Integer, DefaultEdge> directedGraph, NodeEncoder encoder) {
        Neo4jGraph neo = new Neo4jGraph();
        directedGraph.iterables().vertices().forEach((vertex) -> {
            neo.addNode(encoder.decode(vertex).getLabel());
        });
        directedGraph.iterables().edges().forEach((edge) -> {
            int source = (Integer)directedGraph.getEdgeTarget(edge);
            int target = (Integer)directedGraph.getEdgeSource(edge);
            neo.connectNodes(encoder.decode(source).getLabel(), encoder.decode(target).getLabel());
        });
    }

    public void createEncodedShortenIRIsNodesGraph(DefaultDirectedGraph<Integer, DefaultEdge> directedGraph, StringEncoder encoder) {
        Neo4jGraph neo = new Neo4jGraph();
        directedGraph.iterables().vertices().forEach((vertex) -> {
            neo.addNode(Values.iri(encoder.decode(vertex)).getLocalName());
        });
        directedGraph.iterables().edges().forEach((edge) -> {
            int source = (Integer)directedGraph.getEdgeTarget(edge);
            int target = (Integer)directedGraph.getEdgeSource(edge);
            neo.connectNodes(Values.iri(encoder.decode(target)).getLocalName(), Values.iri(encoder.decode(source)).getLocalName());
        });
    }

    public void createBfsTraversedEncodedIRIsNodesGraph(DefaultDirectedGraph<Integer, DefaultEdge> directedGraph, NodeEncoder encoder, Integer hng_root) {
        Neo4jGraph neo = new Neo4jGraph();
        directedGraph.iterables().vertices().forEach((vertex) -> {
            neo.addNode(encoder.decode(vertex).getLabel());
        });
        HashSet<Integer> visited = new HashSet();
        LinkedList<Integer> queue = new LinkedList();
        int node = hng_root;
        queue.add(node);
        visited.add(node);

        while(queue.size() != 0) {
            node = (Integer)queue.poll();
            int finalNode = node;
            Iterator var9 = directedGraph.outgoingEdgesOf(node).iterator();

            while(var9.hasNext()) {
                DefaultEdge edge = (DefaultEdge)var9.next();
                Integer child = (Integer)directedGraph.getEdgeTarget(edge);
                if (!visited.contains(child)) {
                    neo.connectNodes(encoder.decode(finalNode).getLabel(), encoder.decode(child).getLabel());
                    queue.add(child);
                    visited.add(child);
                }
            }
        }

    }

    public void createBfsTraversedEncodedShortenIRIsNodesGraph(DefaultDirectedGraph<Integer, DefaultEdge> directedGraph, StringEncoder encoder, Integer hng_root) {
        Neo4jGraph neo = new Neo4jGraph();
        directedGraph.iterables().vertices().forEach((vertex) -> {
            neo.addNode(Values.iri(encoder.decode(vertex)).getLocalName());
        });
        HashSet<Integer> visited = new HashSet();
        LinkedList<Integer> queue = new LinkedList();
        int node = hng_root;
        queue.add(node);
        visited.add(node);

        while(queue.size() != 0) {
            node = (Integer)queue.poll();
            int finalNode = node;
            Iterator var9 = directedGraph.outgoingEdgesOf(node).iterator();

            while(var9.hasNext()) {
                DefaultEdge edge = (DefaultEdge)var9.next();
                Integer child = (Integer)directedGraph.getEdgeTarget(edge);
                if (!visited.contains(child)) {
                    neo.connectNodes(Values.iri(encoder.decode(finalNode)).getLocalName(), Values.iri(encoder.decode(child)).getLocalName());
                    queue.add(child);
                    visited.add(child);
                }
            }
        }

    }
}
