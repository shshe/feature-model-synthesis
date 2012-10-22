package dk.itu.fms.formula;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This is an abstract class implementing common methods of the classes CNF and DNF.
 * Furthermore it provides abstract methods needed in the FGBuilder.
 */
public abstract class Formula implements Iterable<Clause>, Cloneable {

	//the set of clauses/cubes in the formula
	protected HashSet<Clause> clauses = new HashSet<Clause>();
	
	// the variables present in the clauses
	protected SortedSet<Integer> variables = new TreeSet<Integer>();
	
	// a set of variables corresponding to the dead features, i.e. variables which
	// are assigned false in every satisfiable assignment of the formula. 
	protected Set<Integer> deadFeatures = new HashSet<Integer>(); 
	
	public int getNumberOfVariables(){
		return variables.size() + deadFeatures.size();
	}
	
	/**
	 * 
	 * @return true if the formula is satisfiable
	 */
	public abstract boolean isSatisfiable();
	
	/**
	 * removes variables corresponding to dead features from the formula and adds 
	 * them to the deadFeatures set.
	 */
	public abstract void removeDeadFeatures();
	
	public int[] getVariables(){
		int[] result = new int[variables.size()];
		int j = 0;
		for(int i :variables)
			result[j++] = i; 
		return result;
	}
	
	public abstract Formula clone();
	
	/**
	 * 
	 * @param i
	 * @param j
	 * @return true if the variable i implies the variable j in the formula.
	 */
	public abstract boolean implication(int i, int j);
	
	/**
	 * 
	 * @param i
	 * @param j
	 * @return true if the variables i and j are mutually exclusive in the formula.
	 */
	public abstract boolean isMutex(int i, int j);
	
	public Set<Integer> getDeadFeatures(){
		return deadFeatures;
	}
	
	public String toString(){
		return clauses.toString();
	}
	
	/**
	 * 
	 * @return the number of clauses in the formula
	 */
	public int size(){
		return clauses.size();
	}
	
	public boolean containsClause(Clause c){
		return clauses.contains(c);
	}
	
	public Clause[] getClauses(){
		Clause[] result = new Clause[clauses.size()];
		int i = 0;
		for(Clause c : clauses)
			result[i++] = c;
		return result;
	}
	/**
	 * 
	 * @return true if the formula contains no clauses
	 */
	public boolean isEmpty(){
		return clauses.isEmpty();
	}
	
	public Iterator<Clause> iterator() {
		return clauses.iterator();
	}
	
	/**
	 * 
	 * @param v
	 * @return a set of clauses representing or-groups of the variable v
	 */
	public abstract Set<Clause> getOrGroups(int v);
	
	/**
	 * 
	 * @param vars
	 * @return an equisatisfiable formula which does not contain the variables
	 * specified in vars. 
	 */
	public abstract Formula eliminateVariables(Set<Integer> vars);
}
