package qseevolvingkg.partialsparqlqueries;

import qseevolvingkg.partialsparqlqueries.comparator.ComparatorUtils;
import qseevolvingkg.partialsparqlqueries.comparator.MetaComparator;
import qseevolvingkg.partialsparqlqueries.comparator.ShapeComparatorQSE;
import qseevolvingkg.partialsparqlqueries.comparator.ShapeComparatorSparql;
import qseevolvingkg.partialsparqlqueries.utils.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        setupLogger();

        MetaComparator metaComparator = new MetaComparator();
        String dataSetName1 = ConfigManager.getProperty("dataSetName1");
        String dataSetName2 = ConfigManager.getProperty("dataSetName2");
        String pruningThresholds = ConfigManager.getProperty("pruningThresholds");
        String graphDbUrl = ConfigManager.getProperty("graphDbUrl");
        String parentDirectory = System.getProperty("user.dir")+ File.separator;
        String logPath = parentDirectory + "Output" + File.separator + "compareLogs" + File.separator;
        ShapeComparatorQSE comparatorQSETwice = new ShapeComparatorQSE(graphDbUrl, dataSetName1, dataSetName2, logPath);
        metaComparator.diffQse = comparatorQSETwice.doComparison(pruningThresholds);
        ShapeComparatorSparql comparatorSparql = new ShapeComparatorSparql(graphDbUrl, dataSetName1, dataSetName2, logPath);
        metaComparator.diffSparql = comparatorSparql.doComparison(pruningThresholds, comparatorQSETwice);
        ComparatorUtils.exportComparisonToFile(logPath+dataSetName1+"_"+dataSetName2+ File.separator + "Meta", metaComparator.compare());
    }

    private static void setupLogger() {
        try {
            FileHandler fileHandler;
            fileHandler = new FileHandler("application.log");
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}