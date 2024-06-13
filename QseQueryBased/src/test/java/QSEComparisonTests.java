import org.junit.Test;
import qseevolvingkg.partialsparqlqueries.ComparatorUtils;
import qseevolvingkg.partialsparqlqueries.MetaComparator;
import qseevolvingkg.partialsparqlqueries.ShapeComparatorQSE;
import qseevolvingkg.partialsparqlqueries.ShapeComparatorSparql;

import java.io.File;

public class QSEComparisonTests {

    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/qse/src/main/resources";
    public static final String firstVersionName = "film";

    public static final String outputPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/"+firstVersionName+"/";
    //QSE QueryBases does not calculate confidence, therefore it is always 0 and filtering works with > 0 -> filter to -1
    public static final String pruningThresholdsDefault = "{(-1,0)}"; //only set one threshold - {(<confidence 10% is 0.1>,<support>)}
    public static final String graphDbUrl = "http://localhost:7201/";
    public static final String logPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/compareLogs/";


    @Test
    public void basicQseTestWithFilm() {
        ShapeComparatorQSE comparatorQSETwice = new ShapeComparatorQSE(graphDbUrl, "film", "Film-NoGender", logPath);
        comparatorQSETwice.doComparison(pruningThresholdsDefault);
    }
    @Test
    public void basicQseTestWithFilmSparql() {
        ShapeComparatorSparql comparatorSparql = new ShapeComparatorSparql(graphDbUrl, "film", "Film-NoGender", logPath);
        comparatorSparql.doFullComparison(pruningThresholdsDefault);
    }

    @Test
    public void basicFilmTest() {
        MetaComparator metaComparator = new MetaComparator();
        String dataSetName1 = "film";
        String dataSetName2 = "Film-NoGender";
        ShapeComparatorQSE comparatorQSETwice = new ShapeComparatorQSE(graphDbUrl, dataSetName1, dataSetName2, logPath);
        metaComparator.diffQse = comparatorQSETwice.doComparison(pruningThresholdsDefault);
        ShapeComparatorSparql comparatorSparql = new ShapeComparatorSparql(graphDbUrl, dataSetName1, dataSetName2, logPath);
        metaComparator.diffSparql = comparatorSparql.doFullComparison(pruningThresholdsDefault);
        System.out.println(metaComparator.compare());
        ComparatorUtils.exportComparisonToFile(logPath+dataSetName1+"_"+dataSetName2+ File.separator + "Meta", metaComparator.compare());
    }

    @Test
    public void bearBV1V2Test() {
        MetaComparator metaComparator = new MetaComparator();
        String dataSetName1 = "Bear-B-1";
        String dataSetName2 = "Bear-B2";
        String pruningThresholds =  "{(-1,25)}";
        ShapeComparatorQSE comparatorQSETwice = new ShapeComparatorQSE(graphDbUrl, dataSetName1, dataSetName2, logPath);
        metaComparator.diffQse = comparatorQSETwice.doComparison(pruningThresholds);
        ShapeComparatorSparql comparatorSparql = new ShapeComparatorSparql(graphDbUrl, dataSetName1, dataSetName2, logPath);
        metaComparator.diffSparql = comparatorSparql.doFullComparison(pruningThresholds);
        System.out.println(metaComparator.compare());
        ComparatorUtils.exportComparisonToFile(logPath+dataSetName1+"_"+dataSetName2+ File.separator + "Meta", metaComparator.compare());
    }
}
