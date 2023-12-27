//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils.graphdb;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;

public class GraphDBUtils {
    KBManagement kbManager = new KBManagement();
    Repository repository;
    RepositoryConnection repositoryConnection;

    public GraphDBUtils() {
        this.repository = this.kbManager.initGraphDBRepository();
        this.repositoryConnection = this.repository.getConnection();
    }

    public GraphDBUtils(String graphdbUrl, String graphdbRepository) {
        this.repository = this.kbManager.initGraphDBRepository(graphdbUrl, graphdbRepository);
        this.repositoryConnection = this.repository.getConnection();
    }

    public List<BindingSet> runSelectQuery(String query) {
        List<BindingSet> result = new ArrayList();

        try {
            TupleQuery tupleQuery = this.repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult classesQueryResult = tupleQuery.evaluate();
            Objects.requireNonNull(result);
            classesQueryResult.forEach(result::add);
            classesQueryResult.close();
        } catch (Exception var5) {
            var5.printStackTrace();
            if (this.repositoryConnection.isActive()) {
                this.repositoryConnection.rollback();
            }
        }

        return result;
    }

    public TupleQueryResult evaluateSelectQuery(String query) {
        TupleQueryResult tupleQueryResult = null;

        try {
            if (!this.repositoryConnection.isActive()) {
                this.repositoryConnection = this.repository.getConnection();
            }

            TupleQuery tq = this.repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            tupleQueryResult = tq.evaluate();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return tupleQueryResult;
    }

    public Boolean runAskQuery(String query) {
        Boolean result = null;

        try {
            BooleanQuery queryResult = this.repositoryConnection.prepareBooleanQuery(query);
            result = queryResult.evaluate();
        } catch (Exception var4) {
            var4.printStackTrace();
            if (this.repositoryConnection.isActive()) {
                this.repositoryConnection.rollback();
            }
        }

        return result;
    }

    public GraphQueryResult runConstructQuery(String query) {
        GraphQueryResult resultantTriples = null;

        try {
            GraphQuery queryResult = this.repositoryConnection.prepareGraphQuery(query);
            resultantTriples = queryResult.evaluate();
        } catch (Exception var4) {
            var4.printStackTrace();
            if (this.repositoryConnection.isActive()) {
                this.repositoryConnection.rollback();
            }
        }

        return resultantTriples;
    }

    public void runConstructQuery(String query, String address) {
        try {
            GraphQuery queryResult = this.repositoryConnection.prepareGraphQuery(query);
            GraphQueryResult resultantTriples = queryResult.evaluate();
            FileWriter fileWriter = new FileWriter(address, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            resultantTriples.forEach((statement) -> {
                Resource var10001 = statement.getSubject();
                printWriter.println("<" + var10001 + "> <" + statement.getPredicate() + "> <" + statement.getObject() + "> .");
            });
            printWriter.close();
        } catch (Exception var7) {
            var7.printStackTrace();
            if (this.repositoryConnection.isActive()) {
                this.repositoryConnection.rollback();
            }
        }

    }

    public void runUpdateQuery(String query) {
        try {
            Update updateQuery = this.repositoryConnection.prepareUpdate(query);
            updateQuery.execute();
        } catch (Exception var3) {
            var3.printStackTrace();
            if (this.repositoryConnection.isActive()) {
                this.repositoryConnection.rollback();
            }
        }

    }

    public void runGraphQuery(String query, String address) {
        try {
            GraphQuery graphQuery = this.repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, query);
            GraphQueryResult graphQueryResult = graphQuery.evaluate();
            OutputStream out = new FileOutputStream(address, true);
            RDFWriter writer = new NTriplesWriter(out);
            graphQuery.evaluate(writer);
            graphQueryResult.close();
        } catch (Exception var7) {
            var7.printStackTrace();
            if (this.repositoryConnection.isActive()) {
                this.repositoryConnection.rollback();
            }
        }

    }

    public List<BindingSet> runSelectQueryWithTimeOut(String query) {
        List<BindingSet> result = new ArrayList();

        try {
            TupleQuery tupleQuery = this.repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            tupleQuery.setMaxExecutionTime(300);
            TupleQueryResult classesQueryResult = tupleQuery.evaluate();
            Objects.requireNonNull(result);
            classesQueryResult.forEach(result::add);
            classesQueryResult.close();
        } catch (Exception var5) {
            if (this.repositoryConnection.isActive()) {
                this.repositoryConnection.rollback();
            }
        }

        return result;
    }

    public void updateQueryExecutor(String query) {
        try {
            this.repositoryConnection.begin();
            Update updateOperation = this.repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, query);
            updateOperation.execute();
            this.repositoryConnection.commit();
        } catch (Exception var3) {
            var3.printStackTrace();
            if (this.repositoryConnection.isActive()) {
                this.repositoryConnection.rollback();
            }
        }

    }
}
