//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.mg;

import java.net.URISyntaxException;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public class Neo4jGraph {
    private static final String SERVER_ROOT_URI = "bolt://10.92.0.34:7687";
    private final Driver driver = GraphDatabase.driver("bolt://10.92.0.34:7687", AuthTokens.basic("neo4j", "notension12125"));

    public Neo4jGraph() {
    }

    public void addNode(String value) {
        Session session = this.driver.session();

        try {
            session.run("CREATE (:Node {value: $value})", Values.parameters(new Object[]{"value", value}));
        } catch (Throwable var6) {
            if (session != null) {
                try {
                    session.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }
            }

            throw var6;
        }

        if (session != null) {
            session.close();
        }

    }

    public void connectNodes(String source, String target) {
        Session session = this.driver.session();

        try {
            session.run("MATCH\n  (a:Node),\n  (b:Node)\nWHERE a.value = $A AND b.value = $B\nCREATE (a)-[r:Relation {name: a.value + '<->' + b.value}]->(b)", Values.parameters(new Object[]{"A", source, "B", target}));
        } catch (Throwable var7) {
            if (session != null) {
                try {
                    session.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }
            }

            throw var7;
        }

        if (session != null) {
            session.close();
        }

    }

    public static void main(String[] args) throws URISyntaxException {
        Neo4jGraph neo = new Neo4jGraph();
        neo.addNode(String.valueOf(1));
        neo.addNode(String.valueOf(2));
        neo.connectNodes(String.valueOf(1), String.valueOf(2));
    }
}
