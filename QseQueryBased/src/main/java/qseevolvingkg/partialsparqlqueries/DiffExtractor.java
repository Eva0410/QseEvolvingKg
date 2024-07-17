package qseevolvingkg.partialsparqlqueries;

import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.filebased.Parser;
import cs.qse.filebased.SupportConfidence;
import cs.utils.Constants;
import cs.utils.Tuple2;
import cs.utils.Tuple3;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

//a lot of code is copied from QSE, with some adaptions
public class DiffExtractor {
    public String filePathAdded;
    public String filePathDeleted;
    public Parser parser;
    public HashMap<Integer, Integer> addedConstraints = new HashMap<>();
    public HashMap<Node, EntityData> addedEntityData = new HashMap<>();

    public Integer supportThreshold;
    public Double confidenceThreshold;

    public DiffExtractor(String filePathAdded, String filePathDeleted, Parser parser, Integer support, Double confidence) {
        this.filePathAdded = filePathAdded;
        this.filePathDeleted = filePathDeleted;
        this.parser = parser;
        this.supportThreshold = support;
        this.confidenceThreshold = confidence;
    }

    public void extractFromFile() {
        //Do entity extraction for added statements
        parser.setRdfFilePath(filePathAdded);
        parser.entityExtraction();
        //parse deleted and added statements
        this.entityConstraintsExtractionForDiff();
        this.entityExtractionDeletedEntries(); //order is important
        parser.statsComputer.computeSupportConfidence(new HashMap<>(), parser.classEntityCount); //update confidence
    }

    public void entityExtractionDeletedEntries() {
        try {
            Files.lines(Path.of(filePathDeleted)).forEach(line -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    if (nodes[1].toString().equals(parser.getTypePredicate())) {
                        int objID = parser.getStringEncoder().encode(nodes[2].getLabel());
                        EntityData entityData = parser.entityDataHashMap.get(nodes[0]);
                        if (entityData != null) {
                            entityData.getClassTypes().remove(objID); //todo problem if there are multiple type statements?
                            if(entityData.getClassTypes().isEmpty())
                                parser.entityDataHashMap.remove(nodes[0]);
                            parser.classEntityCount.merge(objID, -1, Integer::sum);
                            if(parser.classEntityCount.get(objID)==0)
                                parser.classEntityCount.remove(objID);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
  }

    //copied from QSE with adaptions
    public void entityConstraintsExtractionForDiff() {
        try {
            doEntityExtractionForAddedOrDeleted(filePathAdded, true);
            doEntityExtractionForAddedOrDeleted(filePathDeleted, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doEntityExtractionForAddedOrDeleted(String filePath, Boolean added) throws IOException {
        Files.lines(Path.of(filePath)).forEach(line -> {
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
                    objTypesIDs = parseIriTypeObject(objTypesIDs, prop2objTypeTuples, nodes, entityNode, propID, added);
                }
                // Object is of type literal, e.g., xsd:String, xsd:Integer, etc.
                else {
                    parseLiteralTypeObject(objTypesIDs, entityNode, objectType, propID, added);
                }
                // for each type (class) of current entity -> append the property and object type in classToPropWithObjTypes HashMap
                updateClassToPropWithObjTypesMap(objTypesIDs, entityNode, propID, added);
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    public void parseLiteralTypeObject(Set<Integer> objTypes, Node subject, String objectType, int propID, Boolean added) {
        Set<Tuple2<Integer, Integer>> prop2objTypeTuples;
        int objID = parser.getStringEncoder().encode(objectType);
        //objTypes = Collections.singleton(objID); Removed because the set throws an UnsupportedOperationException if modification operation (add) is performed on it later in the loop
        objTypes.add(objID);
        prop2objTypeTuples = Collections.singleton(new Tuple2<>(propID, objID));
        addEntityToPropertyConstraints(prop2objTypeTuples, subject, added);
    }

    public Set<Integer> parseIriTypeObject(Set<Integer> objTypesIDs, Set<Tuple2<Integer, Integer>> prop2objTypeTuples, Node[] nodes, Node subject, int propID, Boolean added) {
        EntityData currEntityData = parser.entityDataHashMap.get(nodes[2]);
        if (currEntityData != null && currEntityData.getClassTypes().size() != 0) {
            objTypesIDs = currEntityData.getClassTypes();
            for (Integer node : objTypesIDs) { // get classes of node2
                prop2objTypeTuples.add(new Tuple2<>(propID, node));
            }
            addEntityToPropertyConstraints(prop2objTypeTuples, subject, added);
        }
        /*else { // If we do not have data this is an unlabelled IRI objTypes = Collections.emptySet(); }*/
        else {
            int objID = parser.getStringEncoder().encode(Constants.OBJECT_UNDEFINED_TYPE);
            objTypesIDs.add(objID);
            prop2objTypeTuples = Collections.singleton(new Tuple2<>(propID, objID));
            addEntityToPropertyConstraints(prop2objTypeTuples, subject, added);
        }
        return objTypesIDs;
    }

    public void addEntityToPropertyConstraints(Set<Tuple2<Integer, Integer>> prop2objTypeTuples, Node subject, Boolean added) {
        EntityData currentEntityData = parser.entityDataHashMap.get(subject);
        if (currentEntityData == null) {
            currentEntityData = new EntityData();
        }
        if(added) {
            //Add Property Constraint and Property cardinality
            for (Tuple2<Integer, Integer> tuple2 : prop2objTypeTuples) {
                currentEntityData.addPropertyConstraint(tuple2._1, tuple2._2);
                if (Main.extractMaxCardConstraints) {
                    currentEntityData.addPropertyCardinality(tuple2._1);
                }
            }
        }
        else {
            for (Tuple2<Integer, Integer> tuple2 : prop2objTypeTuples) {
                removePropertyCount(currentEntityData, tuple2._1, tuple2._2);
            }
        }

        parser.entityDataHashMap.put(subject, currentEntityData);
    }

    public void removePropertyCount(EntityData entityData, Integer propertyID, Integer classID) {
        EntityData.PropertyData pd = entityData.propertyConstraintsMap.get(propertyID);
        if (pd != null) {
            pd.objTypesCount.put(classID, pd.objTypesCount.getOrDefault(classID, 0) - 1);
            if(pd.objTypesCount.get(classID)==0) {
                pd.objTypesCount.remove(classID);
                pd.objTypes.remove(classID);
            }
        }
    }

    private void updateClassToPropWithObjTypesMap(Set<Integer> objTypesIDs, Node entityNode, int propID, boolean added) {
        EntityData entityData = parser.entityDataHashMap.get(entityNode);
        if (entityData != null) {
            addedEntityData.put(entityNode,entityData);
            for (Integer entityTypeID : entityData.getClassTypes()) {
                Map<Integer, Set<Integer>> propToObjTypes = parser.classToPropWithObjTypes.computeIfAbsent(entityTypeID, k -> new HashMap<>());
                Set<Integer> classObjTypes = propToObjTypes.computeIfAbsent(propID, k -> new HashSet<>());

                if(added)  {
                    classObjTypes.addAll(objTypesIDs);
                    propToObjTypes.put(propID, classObjTypes);
                    parser.classToPropWithObjTypes.put(entityTypeID, propToObjTypes);
                    addedConstraints.put(entityTypeID, propID);

                    //update support
                    for (var classObjType : objTypesIDs) {
                        Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3<>(entityTypeID, propID, classObjType);
                        SupportConfidence sc = parser.statsComputer.shapeTripletSupport.get(tuple3);
                        if (sc == null) {
                            parser.statsComputer.shapeTripletSupport.put(tuple3, new SupportConfidence(1));
                        } else {
                            sc.setSupport(sc.getSupport()+1);
                            parser.statsComputer.shapeTripletSupport.put(tuple3, sc);
                        }
                    }
                }
                else {
                    //delete all irrelevant classes
                    var objTypesIDsToDelete = new HashSet<Integer>();
                    for(var objTypeID : objTypesIDs) {
                        //support is not relevant here
                        if(entityData.propertyConstraintsMap.get(propID).objTypesCount.getOrDefault(objTypeID,0)<=0) {
                            objTypesIDsToDelete.add(objTypeID);
                        }
                    }

                    //update support
                    for (var classObjType : objTypesIDs) {
                        Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3<>(entityTypeID, propID, classObjType);
                        SupportConfidence sc = parser.statsComputer.shapeTripletSupport.get(tuple3);
                        if(sc != null && objTypesIDsToDelete.contains(classObjType)){
                            sc.setSupport(sc.getSupport()-1);
                            if(sc.getSupport()==0) {
                                parser.statsComputer.shapeTripletSupport.remove(tuple3);

                                //update classToPropWithObjTypes
                                classObjTypes.addAll(objTypesIDs);
                                classObjTypes.removeAll(objTypesIDsToDelete); //removing them from objTypesIDs has side effects on EntityDataHashMap
                                propToObjTypes.put(propID, classObjTypes);
                                parser.classToPropWithObjTypes.put(entityTypeID, propToObjTypes);
                            }
                            else
                                parser.statsComputer.shapeTripletSupport.put(tuple3, sc);
                        }
                    }

                    //modify classToPropWithObjTypes if everything for a property or a class was deleted
                    var anyMatchForProperty = parser.statsComputer.shapeTripletSupport.keySet().stream().anyMatch(e -> e._1.equals(entityTypeID) && e._2.equals(propID));
                    if(!anyMatchForProperty) {
                        parser.classToPropWithObjTypes.get(entityTypeID).remove(propID);
                        if(parser.classToPropWithObjTypes.get(entityTypeID).isEmpty())
                            parser.classToPropWithObjTypes.remove(entityTypeID);
                    }

                    //delete entries now, before they were needed for SupportConfidence-Values
                    for(var entry : entityData.propertyConstraintsMap.entrySet()) {
                        if(entry.getValue().objTypes.isEmpty()) {
                            entityData.propertyConstraintsMap.remove(entry.getKey());
                        }
                    }
                }
            }
        }
    }
}
