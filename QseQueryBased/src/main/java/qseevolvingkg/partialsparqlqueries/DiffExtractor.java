package qseevolvingkg.partialsparqlqueries;

import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.filebased.Parser;
import cs.qse.filebased.SupportConfidence;
import cs.utils.Constants;
import cs.utils.Tuple2;
import cs.utils.Tuple3;
import cs.utils.Utils;
import org.apache.commons.lang3.time.StopWatch;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DiffExtractor {
    public String filePathAdded;
    public String filePathDeleted;
    public Parser parser;
    public HashMap<Integer, Integer> addedConstraints = new HashMap<>();
    public HashMap<Node, EntityData> addedEntityData = new HashMap<>();

    public Integer support;

    public DiffExtractor(String filePathAdded, String filePathDeleted, Parser parser, Integer support) {
        this.filePathAdded = filePathAdded;
        this.filePathDeleted = filePathDeleted;
        this.parser = parser;
        this.support = support;
    }

    public void extractFromFile() {
        //Do entity extraction for added statements
        parser.setRdfFilePath(filePathAdded);
        parser.entityExtraction();
        this.entityConstraintsExtraction();
        parser.statsComputer.computeSupportConfidence(new HashMap<>(), parser.classEntityCount); //update confidence
    }

    //copied from QSE with adaptions
    public void entityConstraintsExtraction() {
        try {
            Files.lines(Path.of(filePathAdded)).forEach(line -> {
                try {
                    //Declaring required sets
                    Set<Integer> objTypesIDs = new HashSet<>(10);
                    Set<Tuple2<Integer, Integer>> prop2objTypeTuples = new HashSet<>(10);

                    // parsing <s,p,o> of triple from each line as node[0], node[1], and node[2]
                    Node[] nodes = NxParser.parseNodes(line);
                    Node entityNode = nodes[0];
                    String objectType = parser.extractObjectType(nodes[2].toString());
                    int propID = parser.getStringEncoder().encode(nodes[1].getLabel());

                    // object is an instance or entity of some class e.g., :Paris is an instance of :City & :Capital
                    if (objectType.equals("IRI")) {
                        objTypesIDs = parser.parseIriTypeObject(objTypesIDs, prop2objTypeTuples, nodes, entityNode, propID);
                    }
                    // Object is of type literal, e.g., xsd:String, xsd:Integer, etc.
                    else {
                        parser.parseLiteralTypeObject(objTypesIDs, entityNode, objectType, propID);
                    }
                    // for each type (class) of current entity -> append the property and object type in classToPropWithObjTypes HashMap
                    updateClassToPropWithObjTypesMap(objTypesIDs, entityNode, propID);
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateClassToPropWithObjTypesMap(Set<Integer> objTypesIDs, Node entityNode, int propID) {
        EntityData entityData = parser.entityDataHashMap.get(entityNode);
        if (entityData != null) {
            addedEntityData.put(entityNode,entityData);
            for (Integer entityTypeID : entityData.getClassTypes()) {
                Map<Integer, Set<Integer>> propToObjTypes = parser.classToPropWithObjTypes.computeIfAbsent(entityTypeID, k -> new HashMap<>());
                Set<Integer> classObjTypes = propToObjTypes.computeIfAbsent(propID, k -> new HashSet<>());
                classObjTypes.addAll(objTypesIDs);
                propToObjTypes.put(propID, classObjTypes);
                parser.classToPropWithObjTypes.put(entityTypeID, propToObjTypes);
                addedConstraints.put(entityTypeID, propID);

                //update support
                for (var clasObjType : objTypesIDs) {
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3<>(entityTypeID, propID, clasObjType);
                    SupportConfidence sc = parser.statsComputer.shapeTripletSupport.get(tuple3);
                    if (sc == null) {
                        parser.statsComputer.shapeTripletSupport.put(tuple3, new SupportConfidence(1));
                    } else {
                        sc.setSupport(sc.getSupport()+1);
                        parser.statsComputer.shapeTripletSupport.put(tuple3, sc);
                    }
                }
            }
        }
    }
}
