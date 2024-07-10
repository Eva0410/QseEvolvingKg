package qseevolvingkg.partialsparqlqueries.utils;

import qseevolvingkg.partialsparqlqueries.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//Copied from QSE
public class ConfigManager {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static String getProperty(String property) {
        try {
            String configPath = System.getProperty("user.dir")+ File.separator + "config.properties";
            java.util.Properties prop = new java.util.Properties();
            FileInputStream configFile = new FileInputStream(configPath);
            prop.load(configFile);
            configFile.close();
            return prop.getProperty(property);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception occurred", ex);
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