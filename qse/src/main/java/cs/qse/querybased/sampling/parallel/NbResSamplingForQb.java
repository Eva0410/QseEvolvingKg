//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.querybased.sampling.parallel;

import cs.qse.common.EntityData;
import cs.qse.common.encoders.ConcurrentStringEncoder;
import cs.qse.common.encoders.NodeEncoder;
import cs.qse.filebased.sampling.BinaryNode;
import cs.qse.filebased.sampling.ReservoirSampling;
import cs.utils.Utils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.semanticweb.yars.nx.Node;

public class NbResSamplingForQb implements ReservoirSampling {
    Map<Integer, EntityData> entityDataMapContainer;
    Map<Integer, List<Integer>> sampledEntitiesPerClass;
    Map<Integer, Integer> reservoirCapacityPerClass;
    NodeEncoder nodeEncoder;
    ConcurrentStringEncoder encoder;

    public NbResSamplingForQb(Map<Integer, EntityData> entityDataMapContainer, Map<Integer, List<Integer>> sampledEntitiesPerClass, Map<Integer, Integer> reservoirCapacityPerClass, NodeEncoder nodeEncoder, ConcurrentStringEncoder encoder) {
        this.entityDataMapContainer = entityDataMapContainer;
        this.sampledEntitiesPerClass = sampledEntitiesPerClass;
        this.reservoirCapacityPerClass = reservoirCapacityPerClass;
        this.nodeEncoder = nodeEncoder;
        this.encoder = encoder;
    }

    public void sample(Node[] nodes) {
        int subjID = this.nodeEncoder.encode(nodes[0]);
        int objID = this.encoder.encode(nodes[2].getLabel());
        EntityData entityData = (EntityData)this.entityDataMapContainer.get(subjID);
        if (entityData == null) {
            entityData = new EntityData();
        }

        entityData.getClassTypes().add(objID);
        this.entityDataMapContainer.put(subjID, entityData);
        ((List)this.sampledEntitiesPerClass.get(objID)).add(subjID);
    }

    public void replace(int candidateIndex, Node[] nodes) {
        int objID = this.encoder.encode(nodes[2].getLabel());
        int currSize = ((List)this.sampledEntitiesPerClass.get(objID)).size();
        if (candidateIndex < currSize) {
            int candidateNodeLeft = -1;
            int candidateNodeRight = -1;
            int scopeCandidateNodeLeft = 999999999;
            int scopeCandidateNodeRight = 999999999;
            if (candidateIndex != 0) {
                candidateNodeLeft = (Integer)((List)this.sampledEntitiesPerClass.get(objID)).get(candidateIndex - 1);
                if (this.entityDataMapContainer.get(candidateNodeLeft) != null) {
                    scopeCandidateNodeLeft = ((EntityData)this.entityDataMapContainer.get(candidateNodeLeft)).getClassTypes().size();
                }
            }

            if (candidateIndex != currSize - 1) {
                candidateNodeRight = (Integer)((List)this.sampledEntitiesPerClass.get(objID)).get(candidateIndex + 1);
                if (this.entityDataMapContainer.get(candidateNodeRight) != null) {
                    scopeCandidateNodeRight = ((EntityData)this.entityDataMapContainer.get(candidateNodeRight)).getClassTypes().size();
                }
            }

            int candidateNode = (Integer)((List)this.sampledEntitiesPerClass.get(objID)).get(candidateIndex);
            if (this.entityDataMapContainer.get(candidateNode) != null) {
                int scopeCandidateNode = ((EntityData)this.entityDataMapContainer.get(candidateNode)).getClassTypes().size();
                BinaryNode node = new BinaryNode(candidateNode, scopeCandidateNode);
                node.left = new BinaryNode(candidateNodeLeft, scopeCandidateNodeLeft);
                node.right = new BinaryNode(candidateNodeRight, scopeCandidateNodeRight);
                BinaryNode min = Utils.getNodeWithMinimumScope(node, node.left, node.right);
                candidateNode = min.id;
                Iterator var13 = ((EntityData)this.entityDataMapContainer.get(candidateNode)).getClassTypes().iterator();

                while(var13.hasNext()) {
                    Integer obj = (Integer)var13.next();
                    if (this.sampledEntitiesPerClass.containsKey(obj)) {
                        ((List)this.sampledEntitiesPerClass.get(obj)).remove(candidateNode);
                    }
                }

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
                Iterator var16 = this.sampledEntitiesPerClass.entrySet().iterator();

                while(var16.hasNext()) {
                    Map.Entry<Integer, List<Integer>> entry = (Map.Entry)var16.next();
                    Integer k = (Integer)entry.getKey();
                    List<Integer> v = (List)entry.getValue();
                    if (v.contains(candidateNode)) {
                        System.out.println("Class " + k + " : " + this.encoder.decode(k) + " has candidate " + candidateNode);
                    }
                }
            }
        }

    }

    public void resizeReservoir(int entitiesSeen, int entitiesInReservoir, Integer maxEntityThreshold, Integer targetSamplingPercentage, int objID) {
        double newCapacityB = (double)(targetSamplingPercentage * entitiesInReservoir);
        double currentRatio = (double)entitiesInReservoir / (double)entitiesSeen * 100.0;
        if ((int)newCapacityB < maxEntityThreshold && currentRatio <= (double)targetSamplingPercentage) {
            this.reservoirCapacityPerClass.put(objID, (int)newCapacityB);
        }

    }
}
