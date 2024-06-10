import org.junit.Test;
import qseevolvingkg.partialsparqlqueries.ShapeComparatorQSE;
import qseevolvingkg.partialsparqlqueries.ShapeComparatorSparql;

public class QSEComparisonTests {

    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/qse/src/main/resources";
    public static final String firstVersionName = "film";

    public static final String outputPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/"+firstVersionName+"/";
    //QSE QueryBases does not calculate confidence, therefore it is always 0 and filtering works with > 0 -> filter to -1
    public static final String pruningThresholds = "{(-1,0)}"; //only set one threshold - {(<confidence 10% is 0.1>,<support>)}
    public static final String graphDbUrl = "http://localhost:7201/";

    @Test
    public void basicQseTestWithFilm() {
        ShapeComparatorQSE comparatorQSETwice = new ShapeComparatorQSE(graphDbUrl, "film", "Film-NoGender", "");
        comparatorQSETwice.doComparison();
    }
    @Test
    public void basicQseTestWithFilmSparql() {
        ShapeComparatorSparql comparatorSparql = new ShapeComparatorSparql(graphDbUrl, "film", "Film-NoGender", "");
        comparatorSparql.doComparison();
    }
}
