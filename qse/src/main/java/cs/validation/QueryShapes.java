//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.validation;

import cs.utils.ConfigManager;
import cs.utils.FilesUtil;
import cs.utils.graphdb.GraphDBUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.eclipse.rdf4j.query.BindingSet;

public class QueryShapes {
    private final GraphDBUtils graphDBUtils = new GraphDBUtils();

    public QueryShapes() {
        this.runQueries();
    }

    public void runQueries() {
        //read classes from file
        String support = "100";
        String confidence = "0.10";
        String fileAddress = ConfigManager.getProperty("config_dir_path") + "pruning/classes.txt";
        List<String> classes = FilesUtil.readAllLinesFromFile(fileAddress);
        AtomicInteger i = new AtomicInteger(1);
        classes.stream().parallel().forEach(classIri -> {
            System.out.println(i + ". " + classIri);
            String query = FilesUtil.readShaclQuery("query")
                    .replace("CLASS_IRI", classIri)
                    .replace("SUPPORT_VAL", support)
                    .replace("CONFIDENCE_VAL", confidence);
            List<BindingSet> result = graphDBUtils.runSelectQuery(query);
            if (result.size() > 1) {
                System.out.println("------------------------------> " + classIri + ": " + result.size());
            }
            i.getAndIncrement();
        });
//        classes.forEach(classIri -> {
//            System.out.println(i + ". " + classIri);
//            String query = FilesUtil.readShaclQuery("query")
//                    .replace("CLASS_IRI", classIri)
//                    .replace("SUPPORT_VAL", support)
//                    .replace("CONFIDENCE_VAL", confidence);
//            List<BindingSet> result = graphDBUtils.runSelectQuery(query);
//            if (result.size() > 1) {
//                System.out.println("------------------------------> " + classIri + ": " + result.size());
//            }
//            i.getAndIncrement();
//        });
    }
}
