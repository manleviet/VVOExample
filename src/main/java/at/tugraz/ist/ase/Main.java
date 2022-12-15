package at.tugraz.ist.ase;

import at.tugraz.ist.ase.heuristics.ValueOrdering;
import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.heuristics.selector.MFVVOValueSelector;
import at.tugraz.ist.ase.heuristics.selector.MFVVOVariableSelector;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.chocosolver.solver.search.strategy.Search.intVarSearch;

/**
 * First example
 */
public class Main {
    public static void main(String[] args) {
        // 1. Create a Model
        Model model = new Model("testing choco");
        // 2. Create variables
//        IntVar x = model.intVar("X", 0, 5);
//        IntVar y = model.intVar("Y", 0, 5);
//        // 3. Create and post constraints thanks to the model
//        model.element(x, new int[]{5,0,4,1,3,2}, y).post();
//        // 3b. Or directly through variables
//        x.add(y).lt(5).post();
        IntVar x = model.intVar("I", 0, 5, false);
        IntVar y = model.intVar("R", 0, 10, false);
        model.element(y, new int[]{0, 2, 4, 6, 7}, x).post();

        IntVar[] vars = new IntVar[]{x, y};

        // create a value ordering for y
        ValueOrdering valueOrdering = new ValueOrdering("R");
        valueOrdering.setIntVar(y);
        valueOrdering.setOrderedValue(0);
        valueOrdering.setOrderedValue(6);
        valueOrdering.setOrderedValue(4);
        valueOrdering.setOrderedValue(2);
        valueOrdering.setOrderedValue(7);

        // create a value variable ordering
        ValueVariableOrdering vvo = new ValueVariableOrdering();
        List<String> varOrdering = new LinkedList<>();
        List<IntVar> intVars = new LinkedList<>();
        // add the variable to the variable ordering
        varOrdering.add(y.getName());
        intVars.add(y);
        varOrdering.add(x.getName());
        intVars.add(x);
        // set the variable ordering
        vvo.setVarOrdering(varOrdering);
        vvo.setIntVarOrdering(intVars);
        vvo.setValueOrdering(valueOrdering);

        Solver solver = model.getSolver();

//        solver.setSearch(Search.inputOrderUBSearch(x, y));
        solver.setSearch(intVarSearch(
                new MFVVOVariableSelector(vvo.getIntVarOrdering()),
                new MFVVOValueSelector(vvo.getValueOrdering()),
                // variables to branch on
                vars
//                ((IIntVarKB)kb).getIntVars()
        ));

        //Add a plugin to print solutions
        AtomicInteger solutionCounter = new AtomicInteger();
        solver.plugMonitor((IMonitorSolution) () -> {
            solutionCounter.getAndIncrement();

            System.out.println("Solution " + solutionCounter.get() + ": " + x + " " + y);
        });

//        if(solver.solve()){
//            System.out.println("The solver has found the solution");
//        } else {
//            System.out.println("The solver has proved the problem has no solution");
//        }

//        solver.printStatistics();
//        solver.printFeatures();
//        solver.showSolutions();
        solver.findAllSolutions();
    }
}