package cs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import cs.qse.filebased.Parser;
import cs.qse.filebased.sampling.ReservoirSamplingParser;
import cs.qse.querybased.nonsampling.QbParser;
import cs.qse.querybased.nonsampling.QbParser_TestLiterals;
import cs.qse.querybased.nonsampling.QbParser_TestMultiplyingOutput;
import cs.qse.querybased.sampling.QbSampling;
import cs.qse.querybased.sampling.parallel.ParallelQbSampling;
import cs.utils.ConfigManager;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import cs.utils.Utils;
import cs.validation.QseSHACLValidator;
import org.slf4j.LoggerFactory;

public class Main {
    public static String configPath;
    public static String datasetPath;
    public static String datasetName;
    public static String outputFilePath;
    public static int numberOfClasses;
    public static int numberOfInstances;
    public static int entitySamplingThreshold;
    public static int entitySamplingTargetPercentage;
    public static boolean extractMaxCardConstraints;
    public static boolean isWikiData;
    public static boolean qseFromSpecificClasses;
    
    public static void main(String[] args) throws Exception {
//        testFileBasedBearBFormatting();
//        testWithMultiplyingOutput();
//        testWithLiterals();
        testqbBearB();
    }



    public static void testqbBearB() throws Exception {
        configPath = "C:\\Users\\evapu\\Downloads\\qse-main2\\qse-main\\configTestQBBearB.properties";
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        readConfig();
        String typeProperty = Constants.RDF_TYPE;

        QbParser qbParser = new QbParser(typeProperty);
        qbParser.run();
        //check C:\Users\evapu\Downloads\qse-main2\qse-main\Output\TEMP\qbfilmLiterals\BearB_QSE_FULL_SHACL.ttl
        // -> parentsThingShapeProperty (only literals are printed not object), show sparql query in graphdb
        //-> alongsideAgentShapeProperty empty because shape has no type (Data not available in graph)
    }
    public static void testWithLiterals() throws Exception {
        configPath = "C:\\Users\\evapu\\Downloads\\qse-main2\\qse-main\\configTestLiterals.properties";
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        readConfig();
        String typeProperty = Constants.RDF_TYPE;

        QbParser_TestLiterals qbParser = new QbParser_TestLiterals(typeProperty);
        qbParser.run();
        //check C:\Users\evapu\Downloads\qse-main2\qse-main\Output\TEMP\qbfilmLiterals\film_QSE_FULL_SHACL.ttl -> establishedDateFilmStudioShapeProperty
    }
    public static void testWithMultiplyingOutput() throws Exception {
        configPath = "C:\\Users\\evapu\\Downloads\\qse-main2\\qse-main\\configWithMultiplyingShapes.properties";
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        readConfig();
        String typeProperty = Constants.RDF_TYPE;

        QbParser_TestMultiplyingOutput qbParser = new QbParser_TestMultiplyingOutput(typeProperty);
        qbParser.run();
        //Execute again -> check C:\Users\evapu\Downloads\qse-main2\qse-main\Output\TEMP\qbfilmMultiplying\film_QSE_FULL_SHACL.ttl hasComposerFilmShapeProperty
    }

    public static void testFileBasedBearBFormatting() throws Exception {
        configPath = "C:\\Users\\evapu\\Downloads\\qse-main2\\qse-main\\configTestBearBFormatting.properties";
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        readConfig();

        //qseExactExecutionWithMinimumParams();
        datasetPath = paramVal("dataset_path");
        datasetName = FilesUtil.getFileName(datasetPath);
        numberOfClasses = Integer.parseInt(paramVal("expected_number_classes")); // expected or estimated numberOfClasses
        numberOfInstances = Integer.parseInt(paramVal("expected_number_of_lines")) / 2; // expected or estimated numberOfInstances
        extractMaxCardConstraints = false;
        isWikiData = false;
        qseFromSpecificClasses = isActivated("qse_specific_classes");
        outputFilePath = paramVal("output_file_path");

        // Run QSE-Exact
        Parser parser = new Parser(datasetPath, numberOfClasses, numberOfInstances, paramVal("instance_type_property"));
        parser.run();
        //check warnings
        //check C:\Users\evapu\Downloads\qse-main2\qse-main\Output\TEMP\bearbTestFormatting\000001_QSE_FULL_SHACL.ttl -> search for http://dbpedia.org/datatype/usDollar
        //Input graph: C:\Users\evapu\Documents\Wien\TU\WINF\MA\defaultGraphs\0000001.nt
    }

//    public static void main(String[] args) throws Exception {
////        configPath = args[0];
////        configPath = "/Users/evapu/Downloads/qse-main2/qse-main/config/";
//        configPath = "C:\\Users\\evapu\\Downloads\\qse-main2\\qse-main\\config.properties";
//        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
//        root.setLevel(Level.INFO);
//        readConfig();
//        String typeProperty = Constants.RDF_TYPE;
//
////        qseExactExecutionWithMinimumParams();
//
//        QbParser qbParser = new QbParser(typeProperty);
//        qbParser.run();
//
//        //benchmark();
//        //new PrecisionRecallComputer();
//    }
    
    
    private static void benchmark() {
        System.out.println("Benchmark Initiated for " + paramVal("dataset_path"));
        Utils.log("Dataset,Method,Second,Minute,SecondTotal,MinuteTotal,MaxCard,DatasetPath");
        Utils.getCurrentTimeStamp();
        try {
            String typeProperty = Constants.RDF_TYPE;
            if (isWikiData) {
                typeProperty = Constants.INSTANCE_OF;
            }
            
            if (isActivated("qse_exact_file")) {
                Parser parser = new Parser(datasetPath, numberOfClasses, numberOfInstances, typeProperty);
                parser.run();
            }
            
            if (isActivated("qse_approximate_file")) {
                ReservoirSamplingParser reservoirSamplingParser = new ReservoirSamplingParser(datasetPath, numberOfClasses, numberOfInstances, typeProperty, entitySamplingThreshold);
                reservoirSamplingParser.run();
            }
            
            if (isActivated("qse_exact_query_based")) {
                QbParser qbParser = new QbParser(typeProperty);
                qbParser.run();
            }
            
            if (isActivated("qse_approximate_query_based")) {
                QbSampling qbSampling = new QbSampling(numberOfClasses, numberOfInstances, typeProperty, entitySamplingThreshold);
                qbSampling.run();
            }
            
            if (isActivated("qse_approximate_parallel_query_based")) {
                int numOfThreads = Integer.parseInt(paramVal("qse_approximate_parallel_qb_threads"));
                ParallelQbSampling parallelQbSampling = new ParallelQbSampling(numberOfClasses, numberOfInstances, typeProperty, entitySamplingThreshold, numOfThreads);
                parallelQbSampling.run();
            }
            if (isActivated("qse_validation")) {
                new QseSHACLValidator(true);
            }
            
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        Utils.getCurrentTimeStamp();
    }
    
    private static void readConfig() {
        datasetPath = paramVal("dataset_path");
        datasetName = paramVal("dataset_name");
        numberOfClasses = Integer.parseInt(paramVal("expected_number_classes")); // expected or estimated numberOfClasses
        numberOfInstances = Integer.parseInt(paramVal("expected_number_of_lines")) / 2; // expected or estimated numberOfInstances
        extractMaxCardConstraints = isActivated("max_cardinality");
        isWikiData = isActivated("is_wikidata");
//        entitySamplingThreshold = Integer.parseInt(paramVal("entity_sampling_threshold"));
//        entitySamplingTargetPercentage = Integer.parseInt(paramVal("entity_sampling_target_percentage"));
        qseFromSpecificClasses = isActivated("qse_specific_classes");
        outputFilePath = paramVal("output_file_path");
    }
    
    private static void qseExactExecutionWithMinimumParams() {
        datasetPath = paramVal("dataset_path");
        datasetName = FilesUtil.getFileName(datasetPath);
        numberOfClasses = Integer.parseInt(paramVal("expected_number_classes")); // expected or estimated numberOfClasses
        numberOfInstances = Integer.parseInt(paramVal("expected_number_of_lines")) / 2; // expected or estimated numberOfInstances
        extractMaxCardConstraints = false;
        isWikiData = false;
        qseFromSpecificClasses = isActivated("qse_specific_classes");
        outputFilePath = paramVal("output_file_path");
        
        // Run QSE-Exact
        Parser parser = new Parser(datasetPath, numberOfClasses, numberOfInstances, paramVal("instance_type_property"));
        parser.run();
    }
    
    private static boolean isActivated(String option) {return Boolean.parseBoolean(ConfigManager.getProperty(option));}
    
    private static String paramVal(String prop) {return ConfigManager.getProperty(prop);}
}
