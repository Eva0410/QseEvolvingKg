package qseevolvingkg.partialsparqlqueries;

import cs.Main;
import cs.qse.filebased.Parser;
import qseevolvingkg.partialsparqlqueries.shapeobjects.ExtractedShapes;
import qseevolvingkg.partialsparqlqueries.shapeobjects.NodeShape;
import qseevolvingkg.partialsparqlqueries.utils.GraphDbUtils;
import qseevolvingkg.partialsparqlqueries.utils.RegexUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiffShapeGenerator {
    Map<Integer, Map<Integer, Set<Integer>>> oldShapesMap;
    Map<Integer, Map<Integer, Set<Integer>>> newShapesMap;
    Parser parser;
    public Map<Integer, Map<Integer, Set<Integer>>> diffShapes;
    public ExtractedShapes extractedShapes;
    HashMap<Integer, Set<Integer>> editedShapes;
    public HashMap<Integer, Set<Integer>> deletedShapes = new HashMap<>();

    public DiffShapeGenerator(DiffExtractor diffExtractor) {
        this.oldShapesMap = diffExtractor.originalClassToPropWithObjTypes;
        this.newShapesMap = DiffExtractor.deepClone(diffExtractor.parser.classToPropWithObjTypes);
        this.parser = diffExtractor.parser;
        this.editedShapes = diffExtractor.editedShapesMap;
    }

    public ExtractedShapes generateDiffShapesWithQse(Map<Integer, Map<Integer, Set<Integer>>> diffMap) {
        //todo check pruning
        parser.classToPropWithObjTypes = diffMap;
        Main.datasetName = Main.datasetName+"_Diff";
        Main.outputFilePath = Main.outputFilePath+ "Diff" + File.separator;
        parser.extractSHACLShapes(false, false);
        extractedShapes = new ExtractedShapes();
        extractedShapes.setNodeShapes(parser.shapesExtractor.getNodeShapes());
        extractedShapes.fileContentPath = parser.shapesExtractor.getOutputFileAddress();
        extractedShapes.getFileAsString();
        return extractedShapes;
    }

    public HashMap<Integer, Set<Integer>> getDeletedShapes() {
        deletedShapes = new HashMap<>();
        for(var nodeShapeEntry : oldShapesMap.entrySet()) {
            var nodeShapeKey = nodeShapeEntry.getKey();
            if(!newShapesMap.containsKey(nodeShapeKey)) {
                deletedShapes.put(nodeShapeKey, nodeShapeEntry.getValue().keySet());
            }
            else {
                for(var propertyShapeId : nodeShapeEntry.getValue().keySet()) {
                    if(!newShapesMap.get(nodeShapeKey).containsKey(propertyShapeId)) {
                        if(!deletedShapes.containsKey(nodeShapeKey)) {
                            deletedShapes.put(nodeShapeKey, new HashSet<>());
                        }
                        var propEntryDeleted = deletedShapes.get(nodeShapeKey);
                        propEntryDeleted.add(propertyShapeId);
                    }
                }
            }
        }
        return deletedShapes;
    }


    public Map<Integer, Map<Integer, Set<Integer>>> generateDiffMap() {
        diffShapes = new HashMap<>();
        for(var nodeShapeEntry : newShapesMap.entrySet()) {
            var nodeShapeKey = nodeShapeEntry.getKey();
            if(!oldShapesMap.containsKey(nodeShapeKey)) {
                diffShapes.put(nodeShapeKey, nodeShapeEntry.getValue());
            }
            else {
                for(var propertyShapeEntry : nodeShapeEntry.getValue().entrySet()) {
                    if(!oldShapesMap.get(nodeShapeKey).containsKey(propertyShapeEntry.getKey())) {
                        if(!diffShapes.containsKey(nodeShapeKey)) {
                            diffShapes.put(nodeShapeKey, new HashMap<>());
                        }
                        var propEntry = diffShapes.get(nodeShapeKey);
                        propEntry.put(propertyShapeEntry.getKey(), propertyShapeEntry.getValue());
                    }
                }
            }
        }

        //add edited shapes
        for(var nodeShapeEntry : editedShapes.entrySet()) {
            var nodeShapeKey = nodeShapeEntry.getKey();
            if(!diffShapes.containsKey(nodeShapeKey)) {
                diffShapes.put(nodeShapeKey, new HashMap<>());
            }
            var propEntryDiff = diffShapes.get(nodeShapeKey);
            var propEntryDeleted = deletedShapes.getOrDefault(nodeShapeKey, new HashSet<>());

            for(var propertyShapeId : nodeShapeEntry.getValue()) {
                if(!propEntryDiff.containsKey(propertyShapeId) && !propEntryDeleted.contains(propertyShapeId)) {
                    var propEntryNew = newShapesMap.get(nodeShapeKey).get(propertyShapeId);
                    propEntryDiff.put(propertyShapeId, propEntryNew);
                }
            }
            if(propEntryDiff.isEmpty())
                diffShapes.remove(nodeShapeKey);
        }
        return diffShapes;
    }

    public String deleteShapesFromFile(ExtractedShapes originalExtractedShapes, String fileContent) {
        for (var nodeShapeClass : deletedShapes.entrySet()) {
            var nodeShape = originalExtractedShapes.nodeShapes.stream().filter(ns -> ns.targetClass.toString().equals(parser.getStringEncoder().decode(nodeShapeClass.getKey()))).findFirst();
            if (nodeShape.isPresent()) {
                for (var propertyShapeClass : nodeShapeClass.getValue()) {
                    var propertyShape = nodeShape.get().propertyShapes.stream().filter(ps -> ps.path.toString().equals(parser.getStringEncoder().decode(propertyShapeClass))).findFirst();
                    if (propertyShape.isPresent()) {
                        fileContent = RegexUtils.deleteIriFromString(propertyShape.get().iri.toString(), fileContent, false);
                        fileContent = RegexUtils.deletePropertyShapeReferenceWithIriFromString(propertyShape.get().iri.toString(), fileContent, false);
                        nodeShape.get().propertyShapes.remove(propertyShape.get());
                    }
                }
                if (nodeShape.get().propertyShapes.isEmpty()) {
                    fileContent = RegexUtils.deleteIriFromString(nodeShape.get().iri.toString(), fileContent, false);
                    originalExtractedShapes.nodeShapes.remove(nodeShape.get());
                }
            }
        }
        return fileContent;
    }

    public String mergeAddedShapesToOrginialFileAsString(ExtractedShapes originalExtractedShapes) {
        var originalFile = originalExtractedShapes.getFileAsString();
        originalFile += extractedShapes.prefixLines;

        for(var addedNodeShape : extractedShapes.getNodeShapes()) {
            var existingNodeShape = originalExtractedShapes.getNodeShapes().stream().filter(ns -> ns.getIri().equals(addedNodeShape.getIri())).findFirst();
            if(existingNodeShape.isPresent()) {
                var nodeShape = existingNodeShape.get();
                var nodeShapeAsText = RegexUtils.getShapeAsString(nodeShape.iri.toString(), originalExtractedShapes.getFileAsString());
                var nodeShapeAsTextUpdated = nodeShapeAsText;

                for(var newPropertyShape : addedNodeShape.propertyShapes) {
                    var existingPropertyShape = existingNodeShape.get().propertyShapes.stream().filter(ps -> ps.iri.equals(newPropertyShape.iri)).findFirst();
                    if(existingPropertyShape.isPresent()) {
                        nodeShape.propertyShapes.remove(existingPropertyShape.get());
                        nodeShape.propertyShapes.add(newPropertyShape);
                        originalFile = RegexUtils.deleteIriFromString(existingPropertyShape.get().iri.toString(), originalFile, false);
                        originalFile += RegexUtils.getShapeAsString(newPropertyShape.iri.toString(), extractedShapes.getFileAsString());
                    }
                    else {
                        nodeShape.propertyShapes.add(newPropertyShape);
                        originalFile+=RegexUtils.getShapeAsString(newPropertyShape.iri.toString(), extractedShapes.getFileAsString());
                        nodeShapeAsTextUpdated = RegexUtils.insertAfter(nodeShapeAsText, ";", "<http://www.w3.org/ns/shacl#property> <"+newPropertyShape.iri.toString()+"> ;");
                    }
                }

                originalFile = RegexUtils.deleteIriFromString(nodeShape.iri.toString(), originalFile, nodeShape.errorDuringGeneration);
                originalFile += nodeShapeAsTextUpdated;
            }
            else {
                originalExtractedShapes.nodeShapes.add(addedNodeShape);
                originalFile+=RegexUtils.getShapeAsString(addedNodeShape.iri.toString(), extractedShapes.getFileAsString());
                for (var ps : addedNodeShape.propertyShapes) {
                    originalFile+=RegexUtils.getShapeAsString(ps.iri.toString(), extractedShapes.getFileAsString());
                }
            }
        }

        var newFilePath = Main.outputFilePath+"merged.ttl";
        RegexUtils.saveStringAsFile(originalFile, newFilePath);
        parser.shapesExtractor.prettyFormatTurtle(newFilePath);
        return parser.shapesExtractor.getOutputFileAddress();
    }
}
