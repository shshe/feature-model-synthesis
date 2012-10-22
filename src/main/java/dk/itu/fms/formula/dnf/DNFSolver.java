package dk.itu.fms.formula.dnf;


public interface DNFSolver {

	boolean addConstraint(int k);
	
	boolean addClause(int[] a);
	
	boolean isSatisfiable();
	
	int[] model();
	
	int numberOfVars();

}
