//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.filebased.sampling;

import cs.qse.common.EntityData;
import cs.qse.common.encoders.NodeEncoder;
import cs.qse.common.encoders.StringEncoder;
import java.util.List;
import java.util.Map;
import org.semanticweb.yars.nx.Node;

public class StandardReservoirSampling implements ReservoirSampling {
    Map<Integer, EntityData> entityDataMapContainer;
    Map<Integer, List<Integer>> sampledEntitiesPerClass;
    NodeEncoder nodeEncoder;
    StringEncoder stringEncoder;

    public StandardReservoirSampling(Map<Integer, EntityData> entityDataMapContainer, Map<Integer, List<Integer>> sampledEntitiesPerClass, NodeEncoder nodeEncoder, StringEncoder stringEncoder) {
        this.entityDataMapContainer = entityDataMapContainer;
        this.sampledEntitiesPerClass = sampledEntitiesPerClass;
        this.nodeEncoder = nodeEncoder;
        this.stringEncoder = stringEncoder;
    }

    public void sample(Node[] nodes) {
        int subjID = this.nodeEncoder.encode(nodes[0]);
        int objID = this.stringEncoder.encode(nodes[2].getLabel());
        EntityData entityData = (EntityData)this.entityDataMapContainer.get(subjID);
        if (entityData == null) {
            entityData = new EntityData();
        }

        entityData.getClassTypes().add(objID);
        this.entityDataMapContainer.put(subjID, entityData);
        ((List)this.sampledEntitiesPerClass.get(objID)).add(subjID);
    }

    public void replace(int candidateIndex, Node[] nodes) {
        int objID = this.stringEncoder.encode(nodes[2].getLabel());
        int currSize = ((List)this.sampledEntitiesPerClass.get(objID)).size();
        if (candidateIndex < currSize) {
            int candidateNode = (Integer)((List)this.sampledEntitiesPerClass.get(objID)).get(candidateIndex);
            if (this.entityDataMapContainer.get(candidateNode) != null) {
                ((EntityData)this.entityDataMapContainer.get(candidateNode)).getClassTypes().forEach((obj) -> {
                    if (this.sampledEntitiesPerClass.containsKey(obj)) {
                        ((List)this.sampledEntitiesPerClass.get(obj)).remove(candidateNode);
                    }

                });
                this.entityDataMapContainer.remove(candidateNode);
                boolean status = this.nodeEncoder.remove(candidateNode);
                if (!status) {
                    System.out.println("WARNING::Failed to remove the candidateNode: " + candidateNode);
                }

                int subjID = this.nodeEncoder.encode(nodes[0]);
                EntityData entityData = (EntityData)this.entityDataMapContainer.get(subjID);
                if (entityData == null) {
                    entityData = new EntityData();
                }

                entityData.getClassTypes().add(objID);
                this.entityDataMapContainer.put(subjID, entityData);
                ((List)this.sampledEntitiesPerClass.get(objID)).add(subjID);
            } else {
                System.out.println("WARNING::It's null for candidateNode " + candidateNode);
                this.sampledEntitiesPerClass.forEach((k, v) -> {
                    if (v.contains(candidateNode)) {
                        System.out.println("Class " + k + " : " + this.stringEncoder.decode(k) + " has candidate " + candidateNode);
                    }

                });
            }
        }

    }
}
