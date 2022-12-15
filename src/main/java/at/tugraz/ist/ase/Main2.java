package at.tugraz.ist.ase;

import at.tugraz.ist.ase.common.IOUtils;
import at.tugraz.ist.ase.fm.core.AbstractRelationship;
import at.tugraz.ist.ase.fm.core.CTConstraint;
import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.parser.FMParserFactory;
import at.tugraz.ist.ase.fm.parser.FeatureIDEParser;
import at.tugraz.ist.ase.fm.parser.FeatureModelParser;
import at.tugraz.ist.ase.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.heuristics.io.ValueVariableOrderingReader;
import at.tugraz.ist.ase.kb.fm.FMKB;
import lombok.Cleanup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Second example - pizzas.xml
 */
public class Main2 {

    private static FMKB<Feature, AbstractRelationship<Feature>, CTConstraint> kb;
    private static FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> featureModel;

    public static void main(String[] args) throws IOException, FeatureModelParserException {
        // read the feature model
        File fileFM = new File("src/main/resources/pizzas.xml");

        @Cleanup("dispose")
        FeatureModelParser<Feature, AbstractRelationship<Feature>, CTConstraint> parser = FMParserFactory.getInstance().getParser(fileFM.getName());
        featureModel = parser.parse(fileFM);

        // convert the feature model into FMKB
        kb = new FMKB<>(featureModel, true);

        // read the value variable ordering
        InputStream is = IOUtils.getInputStream(Main2.class.getClassLoader(), "vvo.csv");

        ValueVariableOrderingReader reader = new ValueVariableOrderingReader();
        ValueVariableOrdering vvo = reader.read(is, kb);


    }
}
