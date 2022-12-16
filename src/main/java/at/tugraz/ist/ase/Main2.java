package at.tugraz.ist.ase;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.chocosolver.solver.search.strategy.Search.intVarSearch;

/**
 * Second example - pizzas.xml
 * - How to read a feature model from a file
 * - How to translate the feature model into a CSP knowledge base (FMKB)
 * - How to read a value-variable ordering from a file
 */
public class Main2 {

    private static FMKB<Feature, AbstractRelationship<Feature>, CTConstraint> kb;
    private static FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> featureModel;

    public static void main(String[] args) throws IOException, FeatureModelParserException, CsvValidationException {
        // read the feature model
        File fileFM = new File("src/main/resources/pizzas.xml");

        @Cleanup("dispose")
        FeatureModelParser<Feature, AbstractRelationship<Feature>, CTConstraint> parser = FMParserFactory.getInstance().getParser(fileFM.getName());
        featureModel = parser.parse(fileFM);

        // convert the feature model into FMKB
        kb = new FMKB<>(featureModel, false); // hasNegativeConstraints = false - we don't need negative constraints

        // read the value variable ordering
        // use InputStream since the file is located in the resources folder
        InputStream is = IOUtils.getInputStream(Main2.class.getClassLoader(), "pizzas_vvo.csv");

        ValueVariableOrderingReader reader = new ValueVariableOrderingReader();
        ValueVariableOrdering vvo = reader.read(is, kb);

        Solver solver = kb.getModelKB().getSolver();

//        solver.setSearch(Search.inputOrderUBSearch(x, y));
        IntVar[] vars = kb.getVariableList().stream().map(v -> v instanceof IntVariable ? ((IntVariable) v).getChocoVar() : ((BoolVariable) v).getChocoVar()).toArray(IntVar[]::new);
        solver.setSearch(intVarSearch(
                new MFVVOVariableSelector(vvo.getIntVarOrdering()),
                new MFVVOValueSelector(vvo.getValueOrdering()),
                // variables to branch on
                vars
        ));

        //Add a monitor to print solutions
        AtomicInteger solutionCounter = new AtomicInteger();
        solver.plugMonitor((IMonitorSolution) () -> {
            solutionCounter.getAndIncrement();

            String solution = kb.getVariableList().stream()
                    .map(v -> v.getName() + "=" + v.getValue())
                    .collect(Collectors.joining(", "));
            System.out.println("Solution " + solutionCounter.get() + ": " + solution);
        });

//        if(solver.solve()){
//            System.out.println("The solver has found the solution");
//        } else {
//            System.out.println("The solver has proved the problem has no solution");
//        }

        solver.findAllSolutions();
    }
}
