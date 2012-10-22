package dk.itu.fms.formula.cnf;

import java.util.Arrays;

import dk.itu.fms.formula.Clause;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * Adapts the default J4Sat solver to the CNFSolver interface. 
 *
 */
public class DefaultCNFSolver implements CNFSolver{

	ISolver solver;
	
	public DefaultCNFSolver(){	
	}

	@Override
	public CNFSolver initSolver(CNF cnf){
		solver = SolverFactory.newDefault();
		for(Clause clause : cnf){
			VecInt vec = new VecInt();
			for(Integer i : clause)
				vec.push(i);
			try {
				solver.addClause(vec);
			} catch (ContradictionException e) {
				throw new RuntimeException("Initialising solver failed, contradicting clause: " + clause.toString());
			}
		}
		return this;
	}

	@Override
	public boolean isSatisfiable() {
		try {
			return solver.isSatisfiable();
		} catch (TimeoutException e) {
			throw new RuntimeException("TIMEOUT: could not decide the satisfiability of the formula.");
		}
	}
	
	@Override
	public boolean isSatisfiable(int i){
		try {
			return solver.isSatisfiable(new VecInt(new int[]{i}));
		} catch (TimeoutException e) {
			throw new RuntimeException("TIMEOUT: could not decide the satisfiability of the formula, assuming the value of variable " + i + " is true.");
		}
	}
	
	@Override
	public boolean isSatisfiable(int[] clause){
		try {
			return solver.isSatisfiable(new VecInt(clause));
		} catch (TimeoutException e) {
			// TODO 
			System.out.println("TimeOut: could not decide the satisfiability of the formula, " +
					"assuming the value of the variables" + Arrays.toString(clause) + " are all true.");
			return false;
		}
	}

	@Override
	public boolean model(int i){
		return solver.model(i);
	}
}
