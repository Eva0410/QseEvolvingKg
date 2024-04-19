////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by FernFlower decompiler)
////
//
//package cs.mg;
//
//import cs.qse.common.EntityData;
//import cs.qse.common.encoders.StringEncoder;
//import cs.utils.Constants;
//import cs.utils.FilesUtil;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//import java.util.Map.Entry;
//import java.util.stream.Collectors;
//import org.jgrapht.alg.connectivity.ConnectivityInspector;
//import org.jgrapht.graph.DefaultDirectedGraph;
//import org.jgrapht.graph.DefaultEdge;
//import org.jgrapht.traverse.BreadthFirstIterator;
//import org.semanticweb.yars.nx.Node;
//import org.semanticweb.yars.nx.parser.NxParser;
//import org.semanticweb.yars.nx.parser.ParseException;
//
//public class MembershipGraph {
//    DefaultDirectedGraph<Integer, DefaultEdge> membershipGraph;
//    StringEncoder encoder;
//    Map<Integer, List<Set<Integer>>> membershipSets;
//    Integer membershipGraphRootNode;
//    Map<Integer, Integer> classInstanceCount;
//    public Map<Node, EntityData> entityDataHashMap;
//
//    public MembershipGraph(boolean devMode) {
//        this.importGraphRelatedData();
//        new HashMap();
//        this.membershipGraph.vertexSet().forEach((v) -> {
//        });
//    }
//
//    public MembershipGraph(StringEncoder stringEncoder, Map<Node, EntityData> entityDataHashMap, Map<Integer, Integer> classEntityCount) {
//        this.membershipGraph = new DefaultDirectedGraph(DefaultEdge.class);
//        this.encoder = stringEncoder;
//        this.entityDataHashMap = entityDataHashMap;
//        this.classInstanceCount = classEntityCount;
//    }
//
//    public void createMembershipSets() {
//        List<Set<Integer>> instanceToClass = entityDataHashMap.values().stream().map(EntityData::getClassTypes).collect(Collectors.toList());
//        this.membershipSets = instanceToClass.stream()
//                .collect(Collectors.groupingBy(Set::size)).entrySet().stream().sorted(Map.Entry.comparingByKey())
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
//                    throw new AssertionError();
//                }, LinkedHashMap::new));
//    }
//
//    public void createMembershipGraph() {
//        ArrayList<Integer> rootNodesOfSubGraphs = new ArrayList();
//
//        try {
//            this.membershipSets.forEach((mSize, mSets) -> {
//                if (mSize == 1) {
//                    mSets.forEach((set) -> {
//                        DefaultDirectedGraph var10001 = this.membershipGraph;
//                        Objects.requireNonNull(var10001);
//                        set.forEach(var10001::addVertex);
//                    });
//                } else if (mSize > 1) {
//                    mSets.forEach((set) -> {
//                        HashMap<Integer, Integer> memberFrequency = new HashMap();
//                        set.forEach((element) -> {
//                            memberFrequency.put(element, (Integer)this.classInstanceCount.get(element));
//                            if (!this.membershipGraph.containsVertex(element)) {
//                                this.membershipGraph.addVertex(element);
//                            }
//
//                        });
//                        Integer[] sortedElementsOfMemberSet = (Integer[])this.getKeysOfMapSortedByValues(memberFrequency).keySet().toArray(new Integer[0]);
//
//                        for(int i = 1; i < sortedElementsOfMemberSet.length; ++i) {
//                            if (((Integer)memberFrequency.get(sortedElementsOfMemberSet[i - 1])).equals(memberFrequency.get(sortedElementsOfMemberSet[i]))) {
//                                this.membershipGraph.addEdge(sortedElementsOfMemberSet[i - 1], sortedElementsOfMemberSet[i]);
//                                this.membershipGraph.addEdge(sortedElementsOfMemberSet[i], sortedElementsOfMemberSet[i - 1]);
//                            } else {
//                                this.membershipGraph.addEdge(sortedElementsOfMemberSet[i - 1], sortedElementsOfMemberSet[i]);
//                            }
//                        }
//
//                    });
//                }
//
//            });
//            ConnectivityInspector<Integer, DefaultEdge> connectivityInspector = new ConnectivityInspector(this.membershipGraph);
//            connectivityInspector.connectedSets().stream().sorted(Comparator.comparingInt(Set::size)).forEach((subGraphVertices) -> {
//                boolean flag = false;
//                if (subGraphVertices.size() > 1) {
//                    Iterator var4 = subGraphVertices.iterator();
//
//                    Integer v;
//                    while(var4.hasNext()) {
//                        v = (Integer)var4.next();
//                        if (this.membershipGraph.inDegreeOf(v) == 0) {
//                            rootNodesOfSubGraphs.add(v);
//                            flag = true;
//                        }
//                    }
//
//                    if (!flag) {
//                        var4 = subGraphVertices.iterator();
//
//                        while(var4.hasNext()) {
//                            v = (Integer)var4.next();
//                            if (this.membershipGraph.inDegreeOf(v) == 1) {
//                                rootNodesOfSubGraphs.add(v);
//                                break;
//                            }
//                        }
//                    }
//                } else if (subGraphVertices.size() == 1) {
//                    rootNodesOfSubGraphs.addAll(subGraphVertices);
//                }
//
//            });
//            this.setMembershipGraphRootNode(this.encoder.encode("ROOT_NODE"));
//            this.membershipGraph.addVertex(this.membershipGraphRootNode);
//            Iterator var3 = rootNodesOfSubGraphs.iterator();
//
//            while(var3.hasNext()) {
//                Integer node = (Integer)var3.next();
//                this.membershipGraph.addEdge(this.membershipGraphRootNode, node);
//            }
//        } catch (Exception var5) {
//            var5.printStackTrace();
//        }
//
//    }
//
//    public void visualizeMg() {
//        (new MembershipGraphVisualizer()).createIntegerNodesGraph(this.membershipGraph);
//    }
//
//    public void membershipGraphCompression(Integer threshold) {
//        int node = this.membershipGraphRootNode;
//        System.out.println("Membership Graph Vertices Before Normalization: " + this.membershipGraph.vertexSet().size());
//        this.getFocusNodesViaBFS(node, threshold).forEach((focusNode) -> {
//        });
//        System.out.println("Membership Graph Vertices After Normalization: " + this.membershipGraph.vertexSet().size());
//    }
//
//    private List<Integer> getDirectChildrenOfNode(Integer node) {
//        List<Integer> directChildren = new ArrayList();
//        this.membershipGraph.outgoingEdgesOf(node).forEach((edge) -> {
//            directChildren.add((Integer)this.membershipGraph.getEdgeTarget(edge));
//        });
//        return directChildren;
//    }
//
//    private Integer getNumberOfChildrenOfNode(Integer node) {
//        List<Integer> directChildren = new ArrayList();
//        this.membershipGraph.outgoingEdgesOf(node).forEach((edge) -> {
//            directChildren.add((Integer)this.membershipGraph.getEdgeTarget(edge));
//        });
//        return directChildren.size();
//    }
//
//    private Integer getGraphSizeViaBFS(Integer startNode) {
//        BreadthFirstIterator<Integer, DefaultEdge> bfsIterator = new BreadthFirstIterator(this.membershipGraph, startNode);
//
//        int size;
//        for(size = 0; bfsIterator.hasNext(); ++size) {
//            bfsIterator.next();
//        }
//
//        return size;
//    }
//
//    private List<Integer> getFocusNodesViaBFS(Integer startNode, Integer threshold) {
//        BreadthFirstIterator<Integer, DefaultEdge> bfsIterator = new BreadthFirstIterator(this.membershipGraph, startNode);
//        List<Integer> focusNodes = new ArrayList();
//
//        while(bfsIterator.hasNext()) {
//            int child = (Integer)bfsIterator.next();
//            if (this.membershipGraph.outDegreeOf(child) > threshold) {
//                focusNodes.add(child);
//            }
//        }
//
//        return focusNodes;
//    }
//
//    public void exportGraphRelatedData() {
//        FilesUtil.deleteFile(Constants.MG_VERTICES_FILE);
//        FilesUtil.deleteFile(Constants.MG_VERTICES_FILE);
//        FilesUtil.deleteFile(Constants.MG_ENCODED_TABLE_FILE);
//        FilesUtil.deleteFile(Constants.MG_ENCODED_R_TABLE_FILE);
//        this.membershipGraph.vertexSet().forEach((vertex) -> {
//            FilesUtil.writeToFileInAppendMode(String.valueOf(vertex), Constants.MG_VERTICES_FILE);
//        });
//        this.membershipGraph.edgeSet().forEach((defaultEdge) -> {
//            Object var10000 = this.membershipGraph.getEdgeSource(defaultEdge);
//            String v = "" + var10000 + "|" + this.membershipGraph.getEdgeTarget(defaultEdge);
//            FilesUtil.writeToFileInAppendMode(v, Constants.MG_EDGES_FILE);
//        });
//        this.encoder.getTable().forEach((k, v) -> {
//            FilesUtil.writeToFileInAppendMode(String.valueOf(k) + "|<http://www.schema.hng.root> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + v + " . ", Constants.MG_ENCODED_TABLE_FILE);
//        });
//        this.encoder.getReverseTable().forEach((k, v) -> {
//            FilesUtil.writeToFileInAppendMode("<http://www.schema.hng.root> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + k.toString() + " . |" + v.toString(), Constants.MG_ENCODED_R_TABLE_FILE);
//        });
//    }
//
//    public void importGraphRelatedData() {
//        List<String[]> vertices = FilesUtil.readCsvAllDataOnceWithPipeSeparator(Constants.MG_VERTICES_FILE);
//        List<String[]> edges = FilesUtil.readCsvAllDataOnceWithPipeSeparator(Constants.MG_EDGES_FILE);
//        DefaultDirectedGraph<Integer, DefaultEdge> graph = new DefaultDirectedGraph(DefaultEdge.class);
//        vertices.forEach((v) -> {
//            graph.addVertex(Integer.valueOf(v[0]));
//        });
//        edges.forEach((edge) -> {
//            graph.addEdge(Integer.valueOf(edge[0]), Integer.valueOf(edge[1]));
//        });
//        List<String[]> encodedTable = FilesUtil.readCsvAllDataOnceWithPipeSeparator(Constants.MG_ENCODED_TABLE_FILE);
//        List<String[]> encodedReversedTable = FilesUtil.readCsvAllDataOnceWithPipeSeparator(Constants.MG_ENCODED_R_TABLE_FILE);
//        HashMap<Integer, String> table = new HashMap();
//        HashMap<String, Integer> reverseTable = new HashMap();
//        encodedTable.forEach((line) -> {
//            try {
//                Node[] nodes = NxParser.parseNodes(String.valueOf(line[1]));
//                table.put(Integer.valueOf(line[0]), nodes[2].getLabel());
//            } catch (ParseException var3) {
//                var3.printStackTrace();
//            }
//
//        });
//        encodedReversedTable.forEach((line) -> {
//            try {
//                Node[] nodes = NxParser.parseNodes(line[0]);
//                reverseTable.put(nodes[2].getLabel(), Integer.valueOf(line[1]));
//            } catch (ParseException var3) {
//                var3.printStackTrace();
//            }
//
//        });
//        this.membershipGraph = graph;
//        this.encoder = new StringEncoder(table.size(), table, reverseTable);
//    }
//
//    public Integer getMembershipGraphRootNode() {
//        return this.membershipGraphRootNode;
//    }
//
//    public Map<Node, EntityData> getEntityDataHashMap() {
//        return this.entityDataHashMap;
//    }
//
//    public Map<Integer, List<Set<Integer>>> getMembershipSets() {
//        return this.membershipSets;
//    }
//
//    public DefaultDirectedGraph<Integer, DefaultEdge> getMembershipGraph() {
//        return this.membershipGraph;
//    }
//
//    private Map<Integer, Integer> getKeysOfMapSortedByValues(HashMap<Integer, Integer> map) {
//        List<Map.Entry<Integer, Integer>> entries = new ArrayList(map.entrySet());
//        entries.sort(new Comparator<Map.Entry<Integer, Integer>>() {
//            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
//                return ((Integer)o2.getValue()).compareTo((Integer)o1.getValue());
//            }
//        });
//        Map<Integer, Integer> sortedMap = new LinkedHashMap();
//        Iterator var4 = entries.iterator();
//
//        while(var4.hasNext()) {
//            Map.Entry<Integer, Integer> entry = (Map.Entry)var4.next();
//            sortedMap.put((Integer)entry.getKey(), (Integer)entry.getValue());
//        }
//
//        return sortedMap;
//    }
//
//    public void setMembershipGraphRootNode(Integer membershipGraphRootNode) {
//        this.membershipGraphRootNode = membershipGraphRootNode;
//    }
//}
