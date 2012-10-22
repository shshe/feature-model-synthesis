package dk.itu.fms.formula;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An abstract class implementing common properties of CNFClause
 * and DNFClause. 
 *
 */
public abstract class Clause implements Iterable<Integer> {


	protected HashSet<Integer> literals;
	protected ArrayList<Integer> posLiterals = new ArrayList<Integer>();
	protected ArrayList<Integer> negLiterals = new ArrayList<Integer>();
	private int cost = Integer.MAX_VALUE;
	
	/**
	 * Modifying the returned set, does not modify this clause.
	 *  @return the literals occurring in this clause.
	 */
	public Set<Integer> getLiterals(){
		return new HashSet<Integer>(literals);
	}
	
	public Iterator<Integer> iterator() {
		return literals.iterator();
	}
	
	/**
	 * Modifying the returned set, does not modify this clause.
	 * @return the variables occurring as negative literals
	 */
	public Set<Integer> getNegativeLiterals(){
		return new HashSet<Integer>(negLiterals); 
	}
	/**
	 * Modifying the returned set, does not modify this clause.
	 *  @return the variables occurring as positive literals
	 */
	public Set<Integer> getPositiveLiterals(){
		return new HashSet<Integer>(posLiterals);
	}
	
	/**
	 * @param i
	 * @return true if the literal i occurs in this clause.
	 */
	public boolean containsLiteral(int i){
		return literals.contains(i);
	}
	
	/**
	 * @return the number of literals in this clause.
	 */
	public int size() {
		return literals.size();
	}
	
	/**
	 * @return true if no negative literals occur in this clause.
	 */
	public boolean isPositive(){
		return negLiterals.isEmpty();
	}
	
	/**
	 * @param c
	 * @return true if this clause is subsumed by c.
	 */
	public boolean contains(Clause c){
		return literals.containsAll(c.getLiterals());
	}
	
	public int getCost(){
		return cost;
	}
	
	public void setCost(int value){
		cost = value;
	}
	
	public String toString(){
		return literals.toString();
	}
	
	public abstract boolean equals(Object other);
	
	public abstract int hashCode();
	
	/**
	 * @param c
	 * @return a clause consisting of the literals contained in this clause but not in c.
	 */
	public abstract Clause difference(Clause c);
	
	
}
