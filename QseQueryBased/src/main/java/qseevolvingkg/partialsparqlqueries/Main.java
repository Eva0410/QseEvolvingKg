package qseevolvingkg.partialsparqlqueries;

import qseevolvingkg.partialsparqlqueries.comparator.*;
import qseevolvingkg.partialsparqlqueries.utils.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        setupLogger();

        MetaComparator metaComparator = new MetaComparator();
        String dataSetName1 = ConfigManager.getProperty("dataSetNameQSE");
        String dataSets = ConfigManager.getProperty("dataSetsToCheck");
        var dataSetsToCheck = dataSets.split(",");
        String pruningThresholds = ConfigManager.getProperty("pruningThresholds");
        String graphDbUrl = ConfigManager.getProperty("graphDbUrl");
        String parentDirectory = System.getProperty("user.dir")+ File.separator;
        String logPath = parentDirectory + "Output" + File.separator + "compareLogs" + File.separator;
        var doMetaComparison = Boolean.parseBoolean(ConfigManager.getProperty("doMetaComparison"));
        if(doMetaComparison) {
            ShapeComparatorQSE comparatorQSETwice = new ShapeComparatorQSE(graphDbUrl, dataSetName1, "", logPath);
            ComparisonDiff comparisonDiff = comparatorQSETwice.runQseFirstTime(pruningThresholds);
            ShapeComparatorSparql comparatorSparql = new ShapeComparatorSparql(graphDbUrl, dataSetName1, "", logPath);
            for (var dataSetName2 : dataSetsToCheck) {
                comparatorQSETwice.dataSetName2 = dataSetName2;
                comparatorSparql.dataSetName2 = dataSetName2;

                metaComparator.diffQse = comparatorQSETwice.doComparisonForFollowingVersion(pruningThresholds, comparisonDiff);
                metaComparator.diffSparql = comparatorSparql.doComparison(pruningThresholds, comparatorQSETwice);
                ComparatorUtils.exportComparisonToFile(logPath+dataSetName1+"_"+dataSetName2+ File.separator + "Meta", metaComparator.compare());
            }
        }
        else {
            ShapeComparatorSparql comparatorSparql = new ShapeComparatorSparql(graphDbUrl, dataSetName1, "", logPath);
            comparatorSparql.doFullComparisonForMultipleVersions(pruningThresholds, dataSetsToCheck);
        }

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