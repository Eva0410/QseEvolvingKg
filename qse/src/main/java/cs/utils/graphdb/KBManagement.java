//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils.graphdb;

import cs.utils.ConfigManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;

public class KBManagement {
    private Repository repository;

    public KBManagement() {
    }

    public Repository initGraphDBRepository() {
        try {
            RepositoryManager repositoryManager = new RemoteRepositoryManager(ConfigManager.getProperty("graphdb_url"));
            this.repository = repositoryManager.getRepository(ConfigManager.getProperty("graphdb_repository"));
            this.repository.init();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        return this.repository;
    }

    public Repository initGraphDBRepository(String graphdbUrl, String graphdbRepository) {
        try {
            RepositoryManager repositoryManager = new RemoteRepositoryManager(graphdbUrl);
            this.repository = repositoryManager.getRepository(graphdbRepository);
            this.repository.init();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return this.repository;
    }

    public void shutDownGraphDB() {
        this.repository.shutDown();
    }
}
