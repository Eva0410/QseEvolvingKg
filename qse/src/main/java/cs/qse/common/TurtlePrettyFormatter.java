//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common;

import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;

public class TurtlePrettyFormatter {
    String fileAddress;

    public TurtlePrettyFormatter(String fileAddress) {
        this.fileAddress = fileAddress;
    }

    public void format(String outputPath) {
        try {
            System.out.println("Pretty Formatting");
            TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
            OutputStream out = new FileOutputStream(outputPath, false);
            Model model = RDFDataMgr.loadModel(this.fileAddress);
            formatter.accept(model, out);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }
}
