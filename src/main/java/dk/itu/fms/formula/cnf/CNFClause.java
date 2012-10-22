package dk.itu.fms.formula.cnf;

import dk.itu.fms.formula.Clause;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CNFClause extends Clause {
	
	private final int hashCode;
	
	/**
	 * Creates an empty clause
	 */
	public CNFClause(){
		literals = new HashSet<Integer>();
		hashCode = 0;
	}
	
	/**
	 * Creates a clause containing the literals given by the Collection col.
	 * @param col
	 */
	public CNFClause(Collection<Integer> col){
		literals = new HashSet<Integer>(col);
		hashCode = clacHashCode();
		for(Integer literal : col){
			if(literal > 0)
				posLiterals.add(literal);
			else
				negLiterals.add(-literal);
		}
	}
	
	/**
	 * Creates a clause containing the literals given in the array.
	 * @param array
	 */
	public CNFClause (int[] array){
		literals = new HashSet<Integer>();
		for(int i = 0; i < array.length; i++){
			int literal = array[i];
			literals.add(literal);
			if(literal > 0)
				posLiterals.add(literal);
			else
				negLiterals.add(-literal);
		}
		hashCode = clacHashCode();
	}
	
	public int getRandomLiteral(){
		int i = (int) Math.random()*(negLiterals.size()+ posLiterals.size());
		if(i < negLiterals.size() ){
			return -negLiterals.get(i);
		}
		else
			return posLiterals.get(i-negLiterals.size());
	}
	
	/**
	 *  precondition: resolvesToTautology(c) returns false. The clauses contains complementary literals.
	 *  @return the resolvent of this and c.
	 */
	public CNFClause computeResolvent(CNFClause c){
		HashSet<Integer> resolvent = new HashSet<Integer>(this.literals); 
		for(Integer i : c){
			if(!resolvent.remove(-i))
				resolvent.add(i);
		}
		return new CNFClause(resolvent);
	}
	
	/**
	 * @param c
	 * @return true if the resolvent of this clause and c is a tautology.
	 */
	public boolean resolvesToTautology(CNFClause c){
		Set<Integer> neg = getNegativeLiterals();
		Set<Integer> pos = getPositiveLiterals();
		neg.retainAll(c.getPositiveLiterals());
		pos.retainAll(c.getNegativeLiterals());
		return neg.size() + pos.size() > 1;
	}
	
	/**
	 * @param c
	 * @return a clause consisting of the literals contained in this clause but not in c.
	 */
	public Clause difference(Clause c){
		CNFClause tmp = new CNFClause(this.getLiterals());
		tmp.literals.removeAll(c.getLiterals());
		return tmp;
	}

	/**
	 * @param other
	 * @return true if this clause and other clause consist of exactly the same literals.  
	 */
	@Override
	public boolean equals(Object other){
		if(this == other)
			return true;
		if(other == null || this.getClass() != other.getClass())
			return false;
		CNFClause c = (CNFClause) other;
		if (this.size() != c.size())
			return false;
		for(Integer literal : this){
			if (!c.containsLiteral(literal))
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}

	private int clacHashCode(){
		int[] sortedLiterals = new int[literals.size()];
		int j = 0;
		int hashcode = 1;
		for(int i : literals)
			sortedLiterals[j++] = i;
		Arrays.sort(sortedLiterals);
		for(int i : sortedLiterals){
			hashcode = 271*hashcode + i;
		}
		return hashcode;
	}
}
