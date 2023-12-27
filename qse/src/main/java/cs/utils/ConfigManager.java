//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils;

import cs.Main;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    public ConfigManager() {
    }

    public static String getProperty(String property) {
        Properties prop = new Properties();

        try {
            if (Main.configPath != null) {
                FileInputStream configFile = new FileInputStream(Main.configPath);
                prop.load(configFile);
                configFile.close();
            } else {
                System.out.println("Config Path is not specified in Main Arg");
            }

            return prop.getProperty(property);
        } catch (IOException var3) {
            var3.printStackTrace();
            return null;
        }
    }
}
