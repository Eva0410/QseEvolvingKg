package qseevolvingkgwebapp.services;

import java.io.File;

public class Utils {
    static String graphDirectory;

    public static String getGraphDirectory() {
        String projectDirectory = System.getProperty("user.dir");
        projectDirectory = projectDirectory + File.separator + "graphs" + File.separator;
        return projectDirectory;
    }

}

