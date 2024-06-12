package qseevolvingkg.partialsparqlqueries;

import cs.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

//Copied from QSE
public class ConfigManager {

    public static String getProperty(String property) {
        try {
            String configPath = System.getProperty("user.dir")+ File.separator + "config.properties";
            java.util.Properties prop = new java.util.Properties();
            FileInputStream configFile = new FileInputStream(configPath);
            prop.load(configFile);
                configFile.close();
            return prop.getProperty(property);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String getRelativeResourcesPathFromQse() {
        String configPath = System.getProperty("user.dir");
        File currentDir = new File(configPath);
        File parentDir = currentDir.getParentFile().getParentFile();
        File resourcesPath = new File(parentDir, "qse/src/main/resources");
        return resourcesPath.getAbsolutePath();
    }
}