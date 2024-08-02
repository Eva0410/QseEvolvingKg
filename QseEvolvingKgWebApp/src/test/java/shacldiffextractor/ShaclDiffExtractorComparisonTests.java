package shacldiffextractor;

import org.junit.Test;
import sparqlshapechecker.comparator.ComparatorUtils;
import sparqlshapechecker.comparator.MetaComparator;
import shacldiffextractor.diff.comparator.ShapeComparatorDiff;
import shacldiffextractor.diff.comparator.ShapeComparatorQseFileBased;

import java.io.File;

public class ShaclDiffExtractorComparisonTests {
    public static final String logPath = System.getProperty("user.dir")+"\\Output\\compareLogsDiff\\";

    @Test
    public void peopleTest() {
        MetaComparator metaComparator = new MetaComparator();
        String dataSetName1 = "PeopleV1";
        String dataSetName2 = "PeopleV2";
        String pruningThresholds =  "{(0,0)}";
        var content = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n";
        var contentAdded = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n";
        var contentDeleted = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n";
        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n";

        String filePath = ShaclDiffExtractorUnitTests.generateFile(content);
        String addedPath = ShaclDiffExtractorUnitTests.generateFile(contentAdded);
        String deletedPath = ShaclDiffExtractorUnitTests.generateFile(contentDeleted);
        String newPath = ShaclDiffExtractorUnitTests.generateFile(contentNew);

        ShapeComparatorQseFileBased comparatorQseFileBased = new ShapeComparatorQseFileBased(filePath, newPath, dataSetName1, dataSetName2, logPath);
        metaComparator.diffQse = comparatorQseFileBased.doComparison(pruningThresholds);
        ShapeComparatorDiff comparatorDiff = new ShapeComparatorDiff(filePath, newPath, addedPath, deletedPath, dataSetName1, dataSetName2, logPath);
        metaComparator.diffAlgorithm = comparatorDiff.doFullComparison(pruningThresholds);
        System.out.println(metaComparator.compareAll());
        ComparatorUtils.exportComparisonToFile(logPath+dataSetName1+"_"+dataSetName2+ File.separator + "Meta", metaComparator.compareAll());
    }
}
