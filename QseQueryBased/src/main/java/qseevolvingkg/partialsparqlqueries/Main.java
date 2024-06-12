package qseevolvingkg.partialsparqlqueries;

import java.io.File;

public class Main {
//    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/qse/src/main/resources";
//    public static final String firstVersionName = "lubm-mini";
//
//    public static final String outputPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/"+firstVersionName+"/";
//    //QSE QueryBases does not calculate confidence, therefore it is always 0 and filtering works with > 0 -> filter to -1
//    public static final String pruningThresholds = "{(-1,10)}"; //only set one threshold - {(<confidence 10% is 0.1>,<support>)}
    public static void main(String[] args) {
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
        metaComparator.diffSparql = comparatorSparql.doComparison(pruningThresholds);
        System.out.println(metaComparator.compare());
        ComparatorUtils.exportComparisonToFile(logPath+dataSetName1+"_"+dataSetName2+ File.separator + "Meta", metaComparator.compare());
    }
}