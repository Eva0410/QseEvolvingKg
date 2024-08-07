package shacldiffextractor.diff;

import cs.Main;
import cs.qse.filebased.Parser;
import shape_comparator.data.ExtractedShapes;
import sparqlshapechecker.comparator.EditedShapesComparisonObject;
import sparqlshapechecker.utils.RegexUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DiffShapeGenerator {
    Map<Integer, Map<Integer, Set<Integer>>> oldShapesMap;
    Map<Integer, Map<Integer, Set<Integer>>> newShapesMap;
    Parser parser;
    public Map<Integer, Map<Integer, Set<Integer>>> diffShapes;
    public ExtractedShapes diffExtractedShapes;
    HashMap<Integer, Set<Integer>> editedShapes;
    public HashMap<Integer, Set<Integer>> deletedShapes = new HashMap<>();
    public ExtractedShapes resultExtractedShapes;
    public ArrayList<String> deletedNodeShapeNames = new ArrayList<>();
    public ArrayList<String> deletedPropertyShapeNames = new ArrayList<>();
    public ArrayList<String> addedNodeShapeNames = new ArrayList<>();
    public ArrayList<String> addedPropertyShapeNames = new ArrayList<>();
    public ArrayList<EditedShapesComparisonObject> editedNodeShapes = new ArrayList<>();
    public ArrayList<EditedShapesComparisonObject> editPropertyShpaes = new ArrayList<>();

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
        diffExtractedShapes.setNodeShapes(parser.shapesExtractor.getNodeShapes(),false);
        diffExtractedShapes.fileContentPath = parser.shapesExtractor.getOutputFileAddress();
        diffExtractedShapes.getFileAsString(true);
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
                    var propertyShape = nodeShape.get().propertyShapes.stream().filter(ps -> ps.pathAsIri.toString().equals(parser.getStringEncoder().decode(propertyShapeClass))).findFirst();
                    if (propertyShape.isPresent()) {
                        fileContent = RegexUtils.deleteIriFromString(propertyShape.get().iri.toString(), fileContent, false);
                        fileContent = RegexUtils.deletePropertyShapeReferenceWithIriFromString(propertyShape.get().iri.toString(), fileContent, false);
                        nodeShape.get().propertyShapes.remove(propertyShape.get());
                        deletedPropertyShapeNames.add(propertyShape.get().iri.toString());
                    }
                }
                if (nodeShape.get().propertyShapes.isEmpty()) {
                    fileContent = RegexUtils.deleteIriFromString(nodeShape.get().getIri().toString(), fileContent, false);
                    resultExtractedShapes.nodeShapes.remove(nodeShape.get());
                    deletedNodeShapeNames.add(nodeShape.get().getIri().toString());
                }
            }
        }
        return fileContent;
    }

    public String mergeAddedShapesToOrginialFileAsString() {
        var originalFile = resultExtractedShapes.getFileAsString(false);
        originalFile += diffExtractedShapes.prefixLines;

        for(var addedNodeShape : diffExtractedShapes.getNodeShapes()) {
            var existingNodeShape = resultExtractedShapes.getNodeShapes().stream().filter(ns -> ns.getIri().equals(addedNodeShape.getIri())).findFirst();
            if(existingNodeShape.isPresent()) {
                var nodeShape = existingNodeShape.get();
                var nodeShapeAsText = RegexUtils.getShapeAsString(nodeShape.getIri().toString(), resultExtractedShapes.getFileAsString(false));
                var nodeShapeAsTextUpdated = nodeShapeAsText;

                for(var newPropertyShape : addedNodeShape.propertyShapes) {
                    var existingPropertyShape = existingNodeShape.get().propertyShapes.stream().filter(ps -> ps.iri.equals(newPropertyShape.iri)).findFirst();
                    if(existingPropertyShape.isPresent()) {
                        nodeShape.propertyShapes.remove(existingPropertyShape.get());
                        nodeShape.propertyShapes.add(newPropertyShape);
                        var oldPropertyShapeText = RegexUtils.getShapeAsString(existingPropertyShape.get().iri.toString(), originalFile);
                        originalFile = RegexUtils.deleteIriFromString(existingPropertyShape.get().iri.toString(), originalFile, false);
                        var newPropertyShapeText = RegexUtils.getShapeAsString(newPropertyShape.iri.toString(), diffExtractedShapes.getFileAsString(false));
                        originalFile += newPropertyShapeText;
                        editPropertyShpaes.add(new EditedShapesComparisonObject(newPropertyShape.iri.toString(), newPropertyShapeText, oldPropertyShapeText));
                    }
                    else {
                        nodeShape.propertyShapes.add(newPropertyShape);
                        addedPropertyShapeNames.add(newPropertyShape.iri.toString());
                        originalFile+=RegexUtils.getShapeAsString(newPropertyShape.iri.toString(), diffExtractedShapes.getFileAsString(false));
                        nodeShapeAsTextUpdated = RegexUtils.insertAfter(nodeShapeAsText, ";", "<http://www.w3.org/ns/shacl#property> <"+newPropertyShape.iri.toString()+"> ;");
                    }
                }

                originalFile = RegexUtils.deleteIriFromString(nodeShape.getIri().toString(), originalFile, nodeShape.errorDuringGeneration);
                originalFile += nodeShapeAsTextUpdated;
                editedNodeShapes.add(new EditedShapesComparisonObject(nodeShape.getIri().toString(), nodeShapeAsTextUpdated, nodeShapeAsText));
            }
            else {
                resultExtractedShapes.nodeShapes.add(addedNodeShape);
                addedNodeShapeNames.add(addedNodeShape.getIri().toString());
                originalFile+=RegexUtils.getShapeAsString(addedNodeShape.getIri().toString(), diffExtractedShapes.getFileAsString(false));
                for (var ps : addedNodeShape.propertyShapes) {
                    originalFile+=RegexUtils.getShapeAsString(ps.iri.toString(), diffExtractedShapes.getFileAsString(false));
                    addedPropertyShapeNames.add(ps.iri.toString());
                }
            }
        }

        var newFilePath = Main.outputFilePath+"merged.ttl";
        RegexUtils.saveStringAsFile(originalFile, newFilePath);
        parser.shapesExtractor.prettyFormatTurtle(newFilePath);
        return parser.shapesExtractor.getOutputFileAddress();
    }
}