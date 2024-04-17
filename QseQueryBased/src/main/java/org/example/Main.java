package org.example;

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;

public class Main {
    public static void main(String[] args) {
        //TODO do RQ4
        RepositoryManager repositoryManager = new RemoteRepositoryManager("http://localhost:7201/");
        var repo = repositoryManager.getRepository("Bear-B");
        var all = repositoryManager.getAllRepositories();
        repo.init();
        System.out.println("Hello world!");
    }
}