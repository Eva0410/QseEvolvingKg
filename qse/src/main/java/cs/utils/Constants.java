//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils;

import cs.Main;

public class Constants {
    public static String SHAPES_NAMESPACE = "http://shaclshapes.org/";
    public static String SHACL_NAMESPACE = "http://www.w3.org/ns/shacl#";
    public static String MEMBERSHIP_GRAPH_ROOT_NODE = "<http://www.schema.hng.root> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.schema.hng.root#HNG_Root> .";
    public static String RDF_TYPE = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
    public static String INSTANCE_OF = "<http://www.wikidata.org/prop/direct/P31>";
    public static String INSTANCE_OF_ = "<http://www.wikidata.org/prop/direct/P31>";
    public static String SUB_CLASS_OF = "<http://www.w3.org/2000/01/rdf-schema#subClassOf>";
    public static String OBJECT_UNDEFINED_TYPE = "http://shaclshapes.org/object-type/undefined";
    public static String CONFIDENCE = "http://shaclshapes.org/confidence";
    public static String SUPPORT = "http://shaclshapes.org/support";
    public static String MG_VERTICES_FILE;
    public static String MG_EDGES_FILE;
    public static String MG_ENCODED_TABLE_FILE;
    public static String MG_ENCODED_R_TABLE_FILE;
    public static String FILTERED_DATASET;
    public static String SUBCLASSOF_DATASET;
    public static String TEMP_DATASET_FILE;
    public static String TEMP_DATASET_FILE_2;
    public static String TEMP_DATASET_FILE_3;
    public static String EXPERIMENTS_RESULT;
    public static String EXPERIMENTS_RESULT_CUSTOM;
    public static String EXPERIMENTS_RESULT_MIN_CARD;
    public static String RUNTIME_LOGS;
    public static String SAMPLING_LOGS;
    public static String THE_LOGS;

    public Constants() {
    }

    static {
        MG_VERTICES_FILE = Main.outputFilePath + "/" + Main.datasetName + "_mg_vertices.csv";
        MG_EDGES_FILE = Main.outputFilePath + "/" + Main.datasetName + "_mg_edges.csv";
        MG_ENCODED_TABLE_FILE = Main.outputFilePath + "/" + Main.datasetName + "_mg_encoded_table.csv";
        MG_ENCODED_R_TABLE_FILE = Main.outputFilePath + "/" + Main.datasetName + "_mg_encoded_table_reverse.csv";
        FILTERED_DATASET = Main.outputFilePath + "/" + Main.datasetName + "_filtered.nt";
        SUBCLASSOF_DATASET = Main.outputFilePath + "/" + Main.datasetName + "_subclassOf.nt";
        TEMP_DATASET_FILE = Main.outputFilePath + "/" + Main.datasetName + "-shape-props-stats.csv";
        TEMP_DATASET_FILE_2 = Main.outputFilePath + "/shape-props-extended-stats.csv";
        TEMP_DATASET_FILE_3 = Main.outputFilePath + "/shape-props-with-class-count-stats.csv";
        EXPERIMENTS_RESULT = Main.outputFilePath + Main.datasetName + ".csv";
        EXPERIMENTS_RESULT_CUSTOM = Main.outputFilePath + Main.datasetName + "_stacked.csv";
        EXPERIMENTS_RESULT_MIN_CARD = Main.outputFilePath + Main.datasetName + "_min_card.csv";
        RUNTIME_LOGS = Main.outputFilePath + Main.datasetName + "_RUNTIME_LOGS.csv";
        SAMPLING_LOGS = Main.outputFilePath + Main.datasetName + "_SAMPLING_LOGS.csv";
        THE_LOGS = Main.outputFilePath + Main.datasetName + "_THE_LOGS.csv";
    }
}
