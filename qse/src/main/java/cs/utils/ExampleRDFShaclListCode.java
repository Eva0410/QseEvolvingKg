//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils;

import java.util.Arrays;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class ExampleRDFShaclListCode {
    public ExampleRDFShaclListCode() {
    }

    public static void main(String[] args) throws Exception {
        String ns = "http://example.org/";
        IRI favoriteLetters = Values.iri(ns, "favoriteLetters");
        IRI john = Values.iri(ns, "John");
        List<Literal> letters = Arrays.asList(Values.literal("A"), Values.literal("B"), Values.literal("C"));
        Resource head = Values.bnode();
        Model aboutJohn = (Model)RDFCollections.asRDF(letters, head, new LinkedHashModel(), new Resource[0]);
        aboutJohn.add(john, favoriteLetters, head, new Resource[0]);
        ValueFactory vf = SimpleValueFactory.getInstance();
        SimpleValueFactory RD4JFactory = SimpleValueFactory.getInstance();
        ModelBuilder builder = new ModelBuilder();
        builder.add(john, Values.iri(ns, "hasGotten"), RD4JFactory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
        Model model = builder.build();
        aboutJohn.addAll(model);

        try {
            Rio.write(aboutJohn, System.out, RDFFormat.TURTLE);
        } catch (Exception var12) {
            var12.printStackTrace();
        }

    }
}
