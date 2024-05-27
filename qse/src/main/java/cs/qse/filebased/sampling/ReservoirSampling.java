//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.filebased.sampling;

import org.semanticweb.yars.nx.Node;

public interface ReservoirSampling {
    void sample(Node[] var1);

    void replace(int var1, Node[] var2);
}
