//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common;

import cs.utils.FilesUtil;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class PostConstraintsAnnotator {
    public RepositoryConnection conn;

    public PostConstraintsAnnotator() {
        String fileAddress = "";
        this.conn = Utility.readFileAsRdf4JModel(fileAddress);
        this.addShNodeConstraint();
        (new ShapesExtractor()).writeModelToFile("dbpedia_sh_node_", this.conn);
    }

    public PostConstraintsAnnotator(RepositoryConnection conn) {
        System.out.println("Invoked:: PostConstraintsAnnotator");
        this.conn = conn;
    }

    public void addShNodeConstraint() {
        this.getNodeShapesAndIterativelyProcessPropShapes();
    }

    private void getNodeShapesAndIterativelyProcessPropShapes() {
        TupleQuery query = this.conn.prepareTupleQuery(FilesUtil.readShaclQuery("node_shapes"));

        try {
            TupleQueryResult result = query.evaluate();

            try {
                while(result.hasNext()) {
                    BindingSet solution = (BindingSet)result.next();
                    this.getPropShapesWithDirectShClassAttribute(solution.getValue("nodeShape").stringValue());
                    this.getPropShapesWithEncapsulatedShClassAttribute(solution.getValue("nodeShape").stringValue());
                }
            } catch (Throwable var6) {
                if (result != null) {
                    try {
                        result.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (result != null) {
                result.close();
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    private void getPropShapesWithDirectShClassAttribute(String nodeShape) {
        TupleQuery query = this.conn.prepareTupleQuery(FilesUtil.readShaclQuery("ps_of_ns_direct_sh_class").replace("NODE_SHAPE", nodeShape));

        try {
            TupleQueryResult result = query.evaluate();

            try {
                while(result.hasNext()) {
                    BindingSet solution = (BindingSet)result.next();
                    this.insertShNodeConstraint(solution);
                }
            } catch (Throwable var7) {
                if (result != null) {
                    try {
                        result.close();
                    } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                    }
                }

                throw var7;
            }

            if (result != null) {
                result.close();
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    private void getPropShapesWithEncapsulatedShClassAttribute(String nodeShape) {
        TupleQuery queryA = this.conn.prepareTupleQuery(FilesUtil.readShaclQuery("ps_of_ns_indirect_sh_class").replace("NODE_SHAPE", nodeShape));

        try {
            TupleQueryResult resultA = queryA.evaluate();

            try {
                while(resultA.hasNext()) {
                    BindingSet bindingsA = (BindingSet)resultA.next();
                    TupleQuery queryB = this.conn.prepareTupleQuery(FilesUtil.readShaclQuery("sh_class_indirect_ps").replace("PROPERTY_SHAPE", bindingsA.getValue("propertyShape").stringValue()).replace("NODE_SHAPE", nodeShape));

                    try {
                        TupleQueryResult resultB = queryB.evaluate();

                        try {
                            while(resultB.hasNext()) {
                                BindingSet bindingsB = (BindingSet)resultB.next();
                                this.insertShNodeConstraint(bindingsB);
                            }
                        } catch (Throwable var11) {
                            if (resultB != null) {
                                try {
                                    resultB.close();
                                } catch (Throwable var10) {
                                    var11.addSuppressed(var10);
                                }
                            }

                            throw var11;
                        }

                        if (resultB != null) {
                            resultB.close();
                        }
                    } catch (Exception var12) {
                        var12.printStackTrace();
                    }
                }
            } catch (Throwable var13) {
                if (resultA != null) {
                    try {
                        resultA.close();
                    } catch (Throwable var9) {
                        var13.addSuppressed(var9);
                    }
                }

                throw var13;
            }

            if (resultA != null) {
                resultA.close();
            }
        } catch (Exception var14) {
            var14.printStackTrace();
        }

    }

    private void insertShNodeConstraint(BindingSet bindings) {
        String classVal = bindings.getValue("class").stringValue();
        if (classVal.contains("<")) {
            classVal = classVal.substring(1, classVal.length() - 1);
        }

        if (this.isNodeShape(classVal)) {
            String var10000 = bindings.getValue("propertyShape").stringValue();
            String insertStatement = "INSERT { <" + var10000 + "> <" + SHACL.NODE + "> <" + this.getNodeShape(classVal) + "> }  WHERE { <" + bindings.getValue("propertyShape") + "> a <http://www.w3.org/ns/shacl#PropertyShape> .  }";
            Update updateQuery = this.conn.prepareUpdate(insertStatement);
            updateQuery.execute();
        }

    }

    private boolean isNodeShape(String shaclClassValue) {
        String query = FilesUtil.readShaclQuery("ns_existence").replace("SHACL_CLASS", shaclClassValue);
        return this.conn.prepareBooleanQuery(query).evaluate();
    }

    private String getNodeShape(String shaclClassValue) {
        String nodeShapeIRI = "";
        TupleQuery query = this.conn.prepareTupleQuery(FilesUtil.readShaclQuery("ns").replace("SHACL_CLASS", shaclClassValue));

        try {
            TupleQueryResult resultB = query.evaluate();

            try {
                while(resultB.hasNext()) {
                    BindingSet solution = (BindingSet)resultB.next();
                    nodeShapeIRI = solution.getValue("nodeShape").stringValue();
                }
            } catch (Throwable var8) {
                if (resultB != null) {
                    try {
                        resultB.close();
                    } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                    }
                }

                throw var8;
            }

            if (resultB != null) {
                resultB.close();
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return nodeShapeIRI;
    }
}
