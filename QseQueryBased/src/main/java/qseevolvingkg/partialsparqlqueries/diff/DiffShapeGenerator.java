package qseevolvingkg.partialsparqlqueries.diff;

import cs.Main;
import cs.qse.filebased.Parser;
import qseevolvingkg.partialsparqlqueries.shapeobjects.ExtractedShapes;
import qseevolvingkg.partialsparqlqueries.utils.RegexUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiffShapeGenerator {
    Map<Integer, Map<Integer, Set<Integer>>> oldShapesMap;
    Map<Integer, Map<Integer, Set<Integer>>> newShapesMap;
    Parser parser;
    public Map<Integer, Map<Integer, Set<Integer>>> diffShapes;
    public ExtractedShapes diffExtractedShapes;
    HashMap<Integer, Set<Integer>> editedShapes;
    public HashMap<Integer, Set<Integer>> deletedShapes = new HashMap<>();
    public ExtractedShapes resultExtractedShapes;

    public DiffShapeGenerator(DiffExtractor diffExtractor) {
        this.oldShapesMap = diffExtractor.originalClassToPropWithObjTypes;
        this.newShapesMap = DiffExtractor.deepClone(diffExtractor.parser.classToPropWithObjTypes);
        this.parser = diffExtractor.parser;
        this.editedShapes = diffExtractor.editedShapesMap;
        this.resultExtractedShapes = diffExtractor.originalExtractedShapes;
    }

    public ExtractedShapes generateDiffShapesWithQse() {
        //todo check pruning
        parser.classToPropWithObjTypes = this.diffShapes;
        var oldDataSetName = Main.datasetName;
        var oldOutputPath = Main.outputFilePath;
        Main.datasetName = Main.datasetName+"_Diff";
        Path path = Paths.get(Main.outputFilePath).getParent();
        var diffPath = path.toAbsolutePath() + File.separator + "Diff" + File.separator;
        Main.outputFilePath = diffPath;
        parser.extractSHACLShapes(false, false);
        diffExtractedShapes = new ExtractedShapes();
        diffExtractedShapes.setNodeShapes(parser.shapesExtractor.getNodeShapes());
        diffExtractedShapes.fileContentPath = parser.shapesExtractor.getOutputFileAddress();
        diffExtractedShapes.getFileAsString();
        Main.datasetName = oldDataSetName;
        Main.outputFilePath = oldOutputPath;
        return diffExtractedShapes;
    }

    public void computeDeletedShapes() {
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
    }


    public void generateDiffMap() {
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
    }

    public String deleteShapesFromFile(String fileContent) {
        for (var nodeShapeClass : deletedShapes.entrySet()) {
            var nodeShape = resultExtractedShapes.nodeShapes.stream().filter(ns -> ns.targetClass.toString().equals(parser.getStringEncoder().decode(nodeShapeClass.getKey()))).findFirst();
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
                    resultExtractedShapes.nodeShapes.remove(nodeShape.get());
                }
            }
        }
        return fileContent;
    }

    public String mergeAddedShapesToOrginialFileAsString() {
        var originalFile = resultExtractedShapes.getFileAsString();
        originalFile += diffExtractedShapes.prefixLines;

        for(var addedNodeShape : diffExtractedShapes.getNodeShapes()) {
            var existingNodeShape = resultExtractedShapes.getNodeShapes().stream().filter(ns -> ns.getIri().equals(addedNodeShape.getIri())).findFirst();
            if(existingNodeShape.isPresent()) {
                var nodeShape = existingNodeShape.get();
                var nodeShapeAsText = RegexUtils.getShapeAsString(nodeShape.iri.toString(), resultExtractedShapes.getFileAsString());
                var nodeShapeAsTextUpdated = nodeShapeAsText;

                for(var newPropertyShape : addedNodeShape.propertyShapes) {
                    var existingPropertyShape = existingNodeShape.get().propertyShapes.stream().filter(ps -> ps.iri.equals(newPropertyShape.iri)).findFirst();
                    if(existingPropertyShape.isPresent()) {
                        nodeShape.propertyShapes.remove(existingPropertyShape.get());
                        nodeShape.propertyShapes.add(newPropertyShape);
                        originalFile = RegexUtils.deleteIriFromString(existingPropertyShape.get().iri.toString(), originalFile, false);
                        originalFile += RegexUtils.getShapeAsString(newPropertyShape.iri.toString(), diffExtractedShapes.getFileAsString());
                    }
                    else {
                        nodeShape.propertyShapes.add(newPropertyShape);
                        originalFile+=RegexUtils.getShapeAsString(newPropertyShape.iri.toString(), diffExtractedShapes.getFileAsString());
                        nodeShapeAsTextUpdated = RegexUtils.insertAfter(nodeShapeAsText, ";", "<http://www.w3.org/ns/shacl#property> <"+newPropertyShape.iri.toString()+"> ;");
                    }
                }

                originalFile = RegexUtils.deleteIriFromString(nodeShape.iri.toString(), originalFile, nodeShape.errorDuringGeneration);
                originalFile += nodeShapeAsTextUpdated;
            }
            else {
                resultExtractedShapes.nodeShapes.add(addedNodeShape);
                originalFile+=RegexUtils.getShapeAsString(addedNodeShape.iri.toString(), diffExtractedShapes.getFileAsString());
                for (var ps : addedNodeShape.propertyShapes) {
                    originalFile+=RegexUtils.getShapeAsString(ps.iri.toString(), diffExtractedShapes.getFileAsString());
                }
            }
        }

        var newFilePath = Main.outputFilePath+"merged.ttl";
        RegexUtils.saveStringAsFile(originalFile, newFilePath);
        parser.shapesExtractor.prettyFormatTurtle(newFilePath);
        return parser.shapesExtractor.getOutputFileAddress();
    }
}
