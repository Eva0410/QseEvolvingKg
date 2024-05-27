//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils.graphdb;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;

public class ExampleQueryExecutor {
    private final GraphDBUtils graphDBUtils = new GraphDBUtils();
    String sparqlQuery = "select * where { \n\t?s ?p ?o .\n} ";

    public ExampleQueryExecutor() {
    }

    public void runQuery() {
        StopWatch watch = new StopWatch();
        watch.start();
        System.out.println("About to run query: " + this.sparqlQuery);
        System.out.println("Iterating over results:");
        watch.stop();
        PrintStream var10000 = System.out;
        long var10001 = TimeUnit.MILLISECONDS.toSeconds(watch.getTime());
        var10000.println("Time Elapsed runQuery: " + var10001 + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }
}
