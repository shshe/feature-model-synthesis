package dk.itu.fms.formula.cnf;

public interface CNFSolver {

	CNFSolver initSolver(CNF cnf);
	
	/**
	 * @return true if the cnf is satisfiable
	 */
	boolean isSatisfiable();
	
	/**
	 * 
	 * @param i
	 * @return true if the cnf is satisfiable when the literal i is assigned true.
	 */
	boolean isSatisfiable(int i);
	
	/**
	 * 
	 * @param clause
	 * @return true if the cnf is satisfiable when all literals in the clause are
	 * assigned true.
	 */
	boolean isSatisfiable(int[] clause);
	
	/**
	 * 
	 * @param i
	 * @return a satisfying model
	 */
	boolean model(int i);
	
	
}
