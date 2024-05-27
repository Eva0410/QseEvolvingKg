package cs.utils;

import cs.Main;

<<<<<<< HEAD:src/main/java/cs/utils/ConfigManager.java
=======

>>>>>>> RQ4copied:QseEvolvingKgWebApp/notes/demo_bugs_kashif/qse-main2/qse-main/src/main/java/cs/utils/ConfigManager.java
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class is used to configure the input params provided in the config file
 */
public class ConfigManager {
<<<<<<< HEAD:src/main/java/cs/utils/ConfigManager.java
    
=======
>>>>>>> RQ4copied:QseEvolvingKgWebApp/notes/demo_bugs_kashif/qse-main2/qse-main/src/main/java/cs/utils/ConfigManager.java
    public static String getProperty(String property) {
        java.util.Properties prop = new java.util.Properties();
        try {
            if (Main.configPath != null) {
                FileInputStream configFile = new FileInputStream(Main.configPath);
                prop.load(configFile);
                configFile.close();
            } else {
                System.out.println("Config Path is not specified in Main Arg");
            }
            return prop.getProperty(property);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
}