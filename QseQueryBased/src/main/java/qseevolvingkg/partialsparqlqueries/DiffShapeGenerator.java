package qseevolvingkg.partialsparqlqueries;

import com.ontotext.trree.query.functions.afn.E;
import cs.Main;
import cs.qse.filebased.Parser;
import qseevolvingkg.partialsparqlqueries.shapeobjects.ExtractedShapes;
import qseevolvingkg.partialsparqlqueries.shapeobjects.NodeShape;
import qseevolvingkg.partialsparqlqueries.utils.RegexUtils;

import java.io.File;
import java.util.*;

public class DiffShapeGenerator {
    Map<Integer, Map<Integer, Set<Integer>>> oldShapes;
    Map<Integer, Map<Integer, Set<Integer>>> newShapes;
    Parser parser;
    public Map<Integer, Map<Integer, Set<Integer>>> diffShapes;
    public ExtractedShapes extractedShapes;

    public DiffShapeGenerator(DiffExtractor diffExtractor) {
        this.oldShapes = diffExtractor.originalClassToPropWithObjTypes;
        this.newShapes = DiffExtractor.deepClone(diffExtractor.parser.classToPropWithObjTypes);
        parser = diffExtractor.parser;
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

    public Map<Integer, Map<Integer, Set<Integer>>> generateDiffMap() {
        diffShapes = new HashMap<>();
        for(var nodeShapeEntry : newShapes.entrySet()) {
            var nodeShapeKey = nodeShapeEntry.getKey();
            if(!oldShapes.containsKey(nodeShapeKey)) {
                diffShapes.put(nodeShapeKey, nodeShapeEntry.getValue());
            }
            else {
                for(var propertyShapeEntry : nodeShapeEntry.getValue().entrySet()) {
                    if(!oldShapes.get(nodeShapeKey).containsKey(propertyShapeEntry.getKey())) {
                        if(!diffShapes.containsKey(nodeShapeKey)) {
                            diffShapes.put(nodeShapeKey, new HashMap<>());
                        }
                        var propEntry = diffShapes.get(nodeShapeKey);
                        propEntry.put(propertyShapeEntry.getKey(), propertyShapeEntry.getValue());
                    }
                }
            }
        }
        return diffShapes;
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
                for(var ps : addedNodeShape.propertyShapes) {
                    nodeShape.propertyShapes.add(ps);
                    originalFile+=RegexUtils.getShapeAsString(ps.iri.toString(), extractedShapes.getFileAsString());
                    nodeShapeAsTextUpdated = RegexUtils.insertAfter(nodeShapeAsText, ";", "<http://www.w3.org/ns/shacl#property> <"+ps.iri.toString()+"> ;");
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
        return newFilePath;
    }
}
