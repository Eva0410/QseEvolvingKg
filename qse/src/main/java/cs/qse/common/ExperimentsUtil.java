//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import cs.utils.ConfigManager;
import cs.utils.FilesUtil;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ExperimentsUtil {
    public ExperimentsUtil() {
    }

    public static HashMap<Double, List<Integer>> getSupportConfRange() {
        HashMap<Double, List<Integer>> confSuppMap = new HashMap();
        String fileAddress = ConfigManager.getProperty("config_dir_path") + "pruning/pruning_thresholds.csv";
        List<String[]> config = FilesUtil.readCsvAllDataOnceWithCustomSeparator(fileAddress, ',');
        config.remove(0);
        config.forEach((row) -> {
            Double conf = Double.parseDouble(row[0]);
            List<Integer> supportValues = (List)confSuppMap.computeIfAbsent(conf, (k) -> {
                return new ArrayList();
            });
            supportValues.add(Integer.parseInt(row[1]));
            confSuppMap.put(conf, supportValues);
        });
        return confSuppMap;
    }

    public static HashMap<Double, List<Integer>> getMinCardinalitySupportConfRange() {
        ArrayList<Integer> supportRange = new ArrayList(Arrays.asList(1, 50, 100, 500, 1000));
        HashMap<Double, List<Integer>> confSuppMap = new HashMap();
        confSuppMap.put(0.75, supportRange);
        confSuppMap.put(0.8, supportRange);
        confSuppMap.put(0.85, supportRange);
        confSuppMap.put(0.9, supportRange);
        confSuppMap.put(0.95, supportRange);
        confSuppMap.put(0.99, supportRange);
        return confSuppMap;
    }

    public static HashMap<Integer, String> getCsvHeader() {
        HashMap<Integer, String> header = new HashMap();
        header.put(1, "COUNT_NS");
        header.put(2, "COUNT_PS");
        header.put(3, "COUNT_CC");
        header.put(4, "COUNT_LC");
        header.put(5, "COUNT_MCC");
        return header;
    }

    public static HashMap<Integer, String> getAverageHeader() {
        HashMap<Integer, String> avgHeader = new HashMap();
        avgHeader.put(1, "AVG_PS");
        avgHeader.put(2, "AVG_CC");
        avgHeader.put(3, "AVG_LC");
        avgHeader.put(4, "AVG_MCC");
        return avgHeader;
    }

    public static HashMap<Integer, String> getMinHeader() {
        HashMap<Integer, String> minHeader = new HashMap();
        minHeader.put(1, "MIN_PS");
        minHeader.put(2, "MIN_CC");
        minHeader.put(3, "MIN_LC");
        minHeader.put(4, "MIN_MCC");
        return minHeader;
    }

    public static HashMap<Integer, String> getMaxHeader() {
        HashMap<Integer, String> maxHeader = new HashMap();
        maxHeader.put(1, "MAX_PS");
        maxHeader.put(2, "MAX_CC");
        maxHeader.put(3, "MAX_LC");
        maxHeader.put(4, "MAX_MCC");
        return maxHeader;
    }

    public static void prepareCsvForGroupedStackedBarChart(String fileAddress, String targetFileAddress, boolean skipFirstRow) {
        try {
            FileReader filereader = new FileReader(fileAddress);
            CSVParser parser = (new CSVParserBuilder()).withSeparator(',').build();
            CSVReader csvReader = (new CSVReaderBuilder(filereader)).withCSVParser(parser).build();
            String[] header = csvReader.readNext();
            System.out.println(Arrays.toString(header));
            HashMap<String, Integer> indexMap = new HashMap();

            for(int i = 0; i < header.length; ++i) {
                if (header[i].equals("Confidence")) {
                    indexMap.put("Confidence", i);
                }

                if (header[i].equals("Support")) {
                    indexMap.put("Support", i);
                }

                if (header[i].equals("COUNT_CC")) {
                    indexMap.put("COUNT_CC", i);
                }

                if (header[i].equals("COUNT_LC")) {
                    indexMap.put("COUNT_LC", i);
                }
            }

            String csvHeader = "Confidence,Support,COUNT,TYPE";
            FilesUtil.writeToFileInAppendMode(csvHeader, targetFileAddress);
            List<String[]> lines = csvReader.readAll();
            if (skipFirstRow) {
                lines.remove(0);
            }

            lines.forEach((line) -> {
                String var10000 = line[(Integer)indexMap.get("Confidence")];
                String logA = var10000 + "," + line[(Integer)indexMap.get("Support")] + "," + line[(Integer)indexMap.get("COUNT_CC")] + ",NonLiteral";
                var10000 = line[(Integer)indexMap.get("Confidence")];
                String logB = var10000 + "," + line[(Integer)indexMap.get("Support")] + "," + line[(Integer)indexMap.get("COUNT_LC")] + ",Literal";
                FilesUtil.writeToFileInAppendMode(logA, targetFileAddress);
                FilesUtil.writeToFileInAppendMode(logB, targetFileAddress);
            });
            System.out.println("Done");
        } catch (Exception var10) {
            var10.printStackTrace();
        }

    }
}
