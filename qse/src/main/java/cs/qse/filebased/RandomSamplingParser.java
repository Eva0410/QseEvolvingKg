//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.filebased;

import cs.Main;
import cs.qse.common.EntityData;
import cs.utils.Tuple2;
import cs.utils.Utils;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

public class RandomSamplingParser extends Parser {
    public int randomSamplingThreshold;
    Map<Integer, Integer> sampledClassEntityCount;

    public RandomSamplingParser(String filePath, int expNoOfClasses, int expNoOfInstances, String typePredicate, int entitySamplingThreshold) {
        super(filePath, expNoOfClasses, expNoOfInstances, typePredicate);
        this.sampledClassEntityCount = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.randomSamplingThreshold = entitySamplingThreshold;
    }

    public void run() {
        this.runParser();
    }

    private void runParser() {
        System.out.println("Entity Sampling Threshold : " + this.randomSamplingThreshold);
        this.entityExtraction();
        this.entityConstraintsExtraction();
        this.computeSupportConfidence();
        this.extractSHACLShapes(false, Main.qseFromSpecificClasses);
    }

    public void entityExtraction() {
        StopWatch watch = new StopWatch();
        watch.start();
        Random random = new Random(100L);

        try {
            Files.lines(Path.of(this.rdfFilePath)).forEach((line) -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    if (nodes[1].toString().equals(this.typePredicate)) {
                        int randomNumber = random.nextInt(100);
                        int objID = this.stringEncoder.encode(nodes[2].getLabel());
                        if (randomNumber < this.randomSamplingThreshold) {
                            EntityData entityData = (EntityData)this.entityDataHashMap.get(nodes[0]);
                            if (entityData == null) {
                                entityData = new EntityData();
                            }

                            entityData.getClassTypes().add(objID);
                            this.entityDataHashMap.put(nodes[0], entityData);
                            this.sampledClassEntityCount.merge(objID, 1, Integer::sum);
                        }

                        this.classEntityCount.merge(objID, 1, Integer::sum);
                    }
                } catch (ParseException var7) {
                    var7.printStackTrace();
                }

            });
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        watch.stop();
        Utils.logTime("firstPass:RandomSampling", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        int sum = (Integer)this.classEntityCount.values().stream().reduce(0, Integer::sum);
        int sumSampled = (Integer)this.sampledClassEntityCount.values().stream().reduce(0, Integer::sum);
        System.out.println("No. of Classes: Total: " + NumberFormat.getInstance().format((long)this.classEntityCount.size()));
        System.out.println("No. of Classes Sampled: " + NumberFormat.getInstance().format((long)this.sampledClassEntityCount.size()));
        PrintStream var10000 = System.out;
        String var10001 = NumberFormat.getInstance().format((long)sum);
        var10000.println("Sum of Entities: " + var10001 + " \n Sum of Sampled Entities : " + NumberFormat.getInstance().format((long)sumSampled));
    }

    public void entityConstraintsExtraction() {
        StopWatch watch = new StopWatch();
        watch.start();

        try {
            Files.lines(Path.of(this.rdfFilePath)).filter((line) -> {
                return !line.contains(this.typePredicate);
            }).forEach((line) -> {
                try {
                    Set<Integer> objTypes = new HashSet(10);
                    Set<Tuple2<Integer, Integer>> prop2objTypeTuples = new HashSet(10);
                    Node[] nodes = NxParser.parseNodes(line);
                    Node subject = nodes[0];
                    if (this.entityDataHashMap.get(subject) != null) {
                        String objectType = this.extractObjectType(nodes[2].toString());
                        int propID = this.stringEncoder.encode(nodes[1].getLabel());
                        EntityData entityData;
                        Iterator var9;
                        Integer entityClass;
                        if (!objectType.equals("IRI")) {
                            int objID = this.stringEncoder.encode(objectType);
                            ((Set)objTypes).add(objID);
                            Set<Tuple2<Integer, Integer>> prop2objTypeTuplesx = Collections.singleton(new Tuple2(propID, objID));
                            this.addEntityToPropertyConstraints(prop2objTypeTuplesx, subject);
                        } else {
                            entityData = (EntityData)this.entityDataHashMap.get(nodes[2]);
                            if (entityData != null) {
                                objTypes = entityData.getClassTypes();
                                var9 = ((Set)objTypes).iterator();

                                while(var9.hasNext()) {
                                    entityClass = (Integer)var9.next();
                                    prop2objTypeTuples.add(new Tuple2(propID, entityClass));
                                }

                                this.addEntityToPropertyConstraints(prop2objTypeTuples, subject);
                            }
                        }

                        entityData = (EntityData)this.entityDataHashMap.get(subject);
                        Object classObjTypes;
                        if (entityData != null) {
                            for(var9 = entityData.getClassTypes().iterator(); var9.hasNext(); ((Set)classObjTypes).addAll((Collection)objTypes)) {
                                entityClass = (Integer)var9.next();
                                Map<Integer, Set<Integer>> propToObjTypes = (Map)this.classToPropWithObjTypes.get(entityClass);
                                if (propToObjTypes == null) {
                                    propToObjTypes = new HashMap();
                                    this.classToPropWithObjTypes.put(entityClass, propToObjTypes);
                                }

                                classObjTypes = (Set)((Map)propToObjTypes).get(propID);
                                if (classObjTypes == null) {
                                    classObjTypes = new HashSet();
                                    ((Map)propToObjTypes).put(propID, classObjTypes);
                                }
                            }
                        }
                    }
                } catch (ParseException var13) {
                    var13.printStackTrace();
                }

            });
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        watch.stop();
        Utils.logTime("secondPass:RandomSampling", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }
}
