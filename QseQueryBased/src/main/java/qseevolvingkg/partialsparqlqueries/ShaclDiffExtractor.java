package qseevolvingkg.partialsparqlqueries;

import qseevolvingkg.partialsparqlqueries.comparator.*;
import qseevolvingkg.partialsparqlqueries.utils.ConfigManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ShaclDiffExtractor {
    private static final Logger LOGGER = Logger.getLogger(ShaclDiffExtractor.class.getName());

    public static void main(String[] args) {
        SparqlShapeValidator.setupLogger();

        MetaComparator metaComparator = new MetaComparator();
        String dataSetName1 = ConfigManager.getProperty("dataSetNameBase");
        String dataSets = ConfigManager.getProperty("objectsToCheck");//versionName, filePathAdded, filePathDeleted, newfilePath|versionName...
        var dataSetsToCheck = dataSets.split("|");
        String pruningThresholds = ConfigManager.getProperty("pruningThresholds");
        String filePath1 = ConfigManager.getProperty("filePathBase");
        String parentDirectory = System.getProperty("user.dir")+ File.separator;
        String logPath = parentDirectory + "Output" + File.separator + "compareLogsDiff" + File.separator;
        var doMetaComparison = Boolean.parseBoolean(ConfigManager.getProperty("doMetaComparison")); //filePath must be given, when meta comparison is true
        if(doMetaComparison) {
            ShapeComparatorQseFileBased comparatorQSETwice = new ShapeComparatorQseFileBased(filePath1, "", dataSetName1, "", logPath);
            ComparisonDiff comparisonDiff = comparatorQSETwice.runQseFirstTime(pruningThresholds, Paths.get("Output", "DiffExtractor"));
            ShapeComparatorDiff comparatorDiff = new ShapeComparatorDiff(filePath1, "", "","", dataSetName1, "", logPath);

            for (var currentDataset : dataSetsToCheck) {
                var nextDataSetName = currentDataset.split(",")[0];
                var filePathAdded = currentDataset.split(",")[1];
                var filePathDeleted = currentDataset.split(",")[2];
                var compeleteFilePath = currentDataset.split(",")[3];

                comparatorDiff.filePath2 = compeleteFilePath;
                comparatorDiff.filePathAdded = filePathAdded;
                comparatorDiff.filePathDeleted = filePathDeleted;
                comparatorQSETwice.dataSetName2 = nextDataSetName;
                comparatorDiff.dataSetName2 = nextDataSetName;

                //todo fix
                metaComparator.diffQse = comparatorQSETwice.doComparisonForFollowingVersion(pruningThresholds, comparisonDiff);
                metaComparator.diffAlgorithm = comparatorDiff.doComparison(pruningThresholds, comparatorQSETwice);
                ComparatorUtils.exportComparisonToFile(logPath+dataSetName1+"_"+currentDataset+ File.separator + "Meta", metaComparator.compareEditedAndDeleted());
            }
        }
        else {
            //Todo fix
            ShapeComparatorDiff comparatorDiff = new ShapeComparatorDiff(filePath1, "", dataSetName1, "", "", "", logPath);
            comparatorDiff.doFullComparisonForMultipleVersions(pruningThresholds, dataSetsToCheck);
        }
    }
}