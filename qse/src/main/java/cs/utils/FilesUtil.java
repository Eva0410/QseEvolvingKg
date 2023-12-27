//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import cs.Main;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

public class FilesUtil {
    public FilesUtil() {
    }

    public static void writeToFile(String str, String fileNameAndPath) {
        try {
            FileWriter fileWriter = new FileWriter(new File(fileNameAndPath));
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(str);
            printWriter.close();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public static void writeToFileInAppendMode(String str, String fileNameAndPath) {
        try {
            FileWriter fileWriter = new FileWriter(new File(fileNameAndPath), true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(str);
            printWriter.close();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public static List<String[]> readCsvAllDataOnceWithPipeSeparator(String fileAddress) {
        List<String[]> allData = null;

        try {
            FileReader filereader = new FileReader(fileAddress);
            CSVParser parser = (new CSVParserBuilder()).withSeparator('|').build();
            CSVReader csvReader = (new CSVReaderBuilder(filereader)).withCSVParser(parser).build();
            allData = csvReader.readAll();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return allData;
    }

    public static List<String[]> readCsvAllDataOnceWithCustomSeparator(String fileAddress, char separator) {
        List<String[]> allData = null;

        try {
            FileReader filereader = new FileReader(fileAddress);
            CSVParser parser = (new CSVParserBuilder()).withSeparator(separator).build();
            CSVReader csvReader = (new CSVReaderBuilder(filereader)).withCSVParser(parser).build();
            allData = csvReader.readAll();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return allData;
    }

    public static boolean deleteFile(String fileAddress) {
        File file = new File(fileAddress);
        return file.delete();
    }

    public static String readQuery(String query) {
        String q = null;

        try {
            String queriesDirectory = Main.resourcesPath + "/queries/";
            q = new String(Files.readAllBytes(Paths.get(queriesDirectory + query + ".txt")));
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        return q;
    }

    public static String readShaclQuery(String query) {
        String q = null;

        try {
            String queriesDirectory = Main.resourcesPath + "/shacl_queries/";
            q = new String(Files.readAllBytes(Paths.get(queriesDirectory + query + ".txt")));
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        return q;
    }

    public static String readShaclStatsQuery(String query, String type) {
        String q = null;

        try {
            String queriesDirectory = Main.resourcesPath + "/shacl_stats_queries/" + type + "/";
            q = new String(Files.readAllBytes(Paths.get(queriesDirectory + query + ".txt")));
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return q;
    }

    public static List<String> readAllLinesFromFile(String fileAddress) {
        List<String> allLines = new ArrayList();

        try {
            allLines = Files.readAllLines(Paths.get(fileAddress));
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        return (List)allLines;
    }

    public static String getFileName(String path) {
        File file = new File(path);
        return FilenameUtils.removeExtension(file.getName());
    }
}
