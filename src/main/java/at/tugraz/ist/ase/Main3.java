package at.tugraz.ist.ase;

import at.tugraz.ist.ase.ce.Configurator;
import at.tugraz.ist.ase.ce.Requirement;
import at.tugraz.ist.ase.ce.Solution;
import at.tugraz.ist.ase.ce.translator.fm.FMSolutionTranslator;
import at.tugraz.ist.ase.ce.writer.TxtSolutionWriter;
import at.tugraz.ist.ase.common.IOUtils;
import at.tugraz.ist.ase.fm.core.AbstractRelationship;
import at.tugraz.ist.ase.fm.core.CTConstraint;
import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.parser.FMParserFactory;
import at.tugraz.ist.ase.fm.parser.FeatureModelParser;
import at.tugraz.ist.ase.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.heuristics.io.ValueVariableOrderingReader;
import at.tugraz.ist.ase.heuristics.selector.MFVVOValueSelector;
import at.tugraz.ist.ase.heuristics.selector.MFVVOVariableSelector;
import at.tugraz.ist.ase.kb.core.Assignment;
import at.tugraz.ist.ase.kb.core.BoolVariable;
import at.tugraz.ist.ase.kb.core.IntVariable;
import at.tugraz.ist.ase.kb.fm.FMKB;
import com.opencsv.exceptions.CsvValidationException;
import lombok.Cleanup;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.chocosolver.solver.search.strategy.Search.intVarSearch;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Third example - linux-2.6.33.3.xml
 * - How to read a feature model from a file
 * - How to translate the feature model into a CSP knowledge base (FMKB)
 * - How to create a configurator, set writer, requirement, and find solutions
 */
public class Main3 {

    private static FMKB<Feature, AbstractRelationship<Feature>, CTConstraint> kb;
    private static FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> featureModel;

    public static void main(String[] args) throws FeatureModelParserException {
        // read the feature model
        File fileFM = new File("src/main/resources/linux-2.6.33.3.xml");

        @Cleanup("dispose")
        FeatureModelParser<Feature, AbstractRelationship<Feature>, CTConstraint> parser = FMParserFactory.getInstance().getParser(fileFM.getName());
        featureModel = parser.parse(fileFM);

        // convert the feature model into FMKB
        kb = new FMKB<>(featureModel, false); // hasNegativeConstraints = false - we don't need negative constraints

        // create a configurator
        Configurator configurator = new Configurator(kb, true, new FMSolutionTranslator());
        configurator.initializeWithKB();
        configurator.setWriter(new TxtSolutionWriter("./")); // write solutions into the current directory
        configurator.setRequirement(Requirement.requirementBuilder()
                .assignments(List.of(new Assignment("HZ_100", "true")))
                .build()); // set requirement

        // identify 10 solutions
        configurator.find(10, 0);

        configurator.reset(); // reset the configurator
        assert configurator.getNumberSolutions() == 10;

        // print the solutions to the console
        int counter = 0;
        for (Solution s : configurator.getSolutions()) {
            System.out.print(++counter + " " + s + " - ");
        }
    }
}
