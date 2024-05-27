//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils;

import cs.utils.graphdb.GraphDBUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.rdf4j.query.BindingSet;

public class StatsCollector {
    private final GraphDBUtils graphDBUtils = new GraphDBUtils();

    public StatsCollector() {
    }

    public void doTheJob() {
        List<String[]> vertices = FilesUtil.readCsvAllDataOnceWithPipeSeparator(Constants.TEMP_DATASET_FILE);

        try {
            FileWriter fileWriter = new FileWriter(new File(Constants.TEMP_DATASET_FILE_2), true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            StopWatch watch = new StopWatch();
            watch.start();
            Iterator var5 = vertices.iterator();

            while(var5.hasNext()) {
                String[] v = (String[])var5.next();
                String query = this.buildQuery(v[0], v[1], v[2]);
                String count = "";

                BindingSet result;
                for(Iterator var9 = this.graphDBUtils.runSelectQuery(query).iterator(); var9.hasNext(); count = result.getValue("val").stringValue()) {
                    result = (BindingSet)var9.next();
                }

                String line = v[0] + "|" + v[1] + "|" + v[2] + "|" + v[3] + "|" + v[4] + "|" + count + "|\"" + query + "\"";
                printWriter.println(line);
            }

            printWriter.close();
            watch.stop();
            PrintStream var10000 = System.out;
            long var10001 = TimeUnit.MILLISECONDS.toSeconds(watch.getTime());
            var10000.println("Time Elapsed StatsCollector : " + var10001 + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        } catch (IOException var11) {
            var11.printStackTrace();
        }

    }

    public void justTestOneQuery() {
        String query = "SELECT ( COUNT( DISTINCT ?s) AS ?val) WHERE {?s a <http://dbpedia.org/ontology/SoccerClub>.?s <http://www.w3.org/2002/07/owl#differentFrom> ?obj .?obj a <http://dbpedia.org/ontology/SoccerClub> ;}";
        String count = "";

        Iterator var3;
        BindingSet result;
        for(var3 = this.graphDBUtils.runSelectQuery(query).iterator(); var3.hasNext(); count = result.getValue("val").stringValue()) {
            result = (BindingSet)var3.next();
        }

        System.out.println("A: " + count);
        query = "PREFIX onto: <http://www.ontotext.com/> SELECT ( COUNT( DISTINCT ?s) AS ?val) FROM onto:explicit WHERE {?s a <http://dbpedia.org/ontology/SoccerClub>.?s <http://www.w3.org/2002/07/owl#differentFrom> ?obj .?obj a <http://dbpedia.org/ontology/SoccerClub> ;}";
        count = "";

        for(var3 = this.graphDBUtils.runSelectQuery(query).iterator(); var3.hasNext(); count = result.getValue("val").stringValue()) {
            result = (BindingSet)var3.next();
        }

        System.out.println("B: " + count);
    }

    private String buildQuery(String classVal, String propVal, String objTypeVal) {
        String query = "";
        if (objTypeVal.contains("<")) {
            query = "PREFIX onto: <http://www.ontotext.com/>SELECT ( COUNT( DISTINCT ?s) AS ?val) FROM onto:explicit WHERE {?s a   <" + classVal + "> . ?s <" + propVal + "> ?obj . FILTER(dataType(?obj) = " + objTypeVal + " ) }";
        } else {
            query = "PREFIX onto: <http://www.ontotext.com/>SELECT ( COUNT( DISTINCT ?s) AS ?val) FROM onto:explicit WHERE {?s a <" + classVal + ">.?s <" + propVal + "> ?obj .?obj a <" + objTypeVal + "> ;}";
        }

        return query;
    }
}
