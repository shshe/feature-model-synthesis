package dk.itu.fms.formula.dnf;

import dk.itu.fms.formula.Clause;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DNFClause extends Clause {

	private final int hashCode;
	
	public DNFClause(){
		literals = new HashSet<Integer>();
		hashCode = 0;
	}
	
	public DNFClause(int[] array) {
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
	
	public DNFClause(Collection<Integer> col) {
		literals = new HashSet<Integer>(col);
		for(Integer literal : col){
			if(literal > 0)
				posLiterals.add(literal);
			else
				negLiterals.add(-literal);
		}
		hashCode = clacHashCode();
	}
	
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
	
	@Override
	public boolean equals(Object other){
		if(this == other)
			return true;
		if(other == null || this.getClass() != other.getClass())
			return false;
		DNFClause c = (DNFClause) other;
		if (this.size() != c.size())
			return false;
		for(Integer literal : this){
			if (!c.containsLiteral(literal))
				return false;
		}
		return true;
	}

	/**
	 * @param c
	 * @return a clause consisting of the literals contained in this clause but not in c.
	 */
	public Clause difference(Clause c) {
		
		DNFClause tmp = new DNFClause(this.getLiterals());
		tmp.literals.removeAll(c.getLiterals());
		return tmp;

	}

}
