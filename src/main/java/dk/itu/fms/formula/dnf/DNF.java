package dk.itu.fms.formula.dnf;

import dk.itu.fms.formula.Clause;
import dk.itu.fms.formula.Formula;
import dk.itu.fms.prime.Prime;
import org.sat4j.specs.ContradictionException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


public class DNF extends Formula {

	public DNF(){
	}

	public DNF(Collection<DNFClause> clauses){
		addAllClauses(clauses);
		//removeDeadFeatures();
	}
	
	public DNF clone(){
		DNF clone = new DNF();
		for(Clause clause : this.clauses){
			clone.variables = new TreeSet<Integer>(this.variables);
			clone.deadFeatures = new HashSet<Integer>(this.deadFeatures);
			DNFClause clauseClone;
			clauseClone = new DNFClause(clause.getLiterals());
			clone.clauses.add(clauseClone);
		}
		return clone;
	}

	private boolean addClause(DNFClause clause){
		if(clauses.add(clause)){
			for(int i : clause)
				variables.add(Math.abs(i));
			return true;
		}
		return false;
	}
	
	private void addAllClauses(Collection<DNFClause> clauses) {
		for(DNFClause clause : clauses)
			addClause(clause);
	}

	public boolean isSatisfiable(){
		for(Clause clause : this){
			Set<Integer> intersection = clause.getNegativeLiterals();
			intersection.retainAll(clause.getPositiveLiterals());
			if(!intersection.isEmpty())
				return false;
		}
		return true;
	}
	
	public void removeDeadFeatures(){
		//compute dead features
		Set<Integer> df = new HashSet<Integer>();
		for(int i : variables)
			df.add(-i);
		for(Clause c : clauses){
			df.retainAll(c.getLiterals());
		}
		
		for(int i : df)
			deadFeatures.add(-i);
		
		removeDeadFeatures(deadFeatures);
		
	}
	
	private void removeDeadFeatures(Set<Integer> df){
		for(int d : df){
			variables.remove(d);
			Set<Clause> obsolete = new HashSet<Clause>();
			Set<Clause> newClauses = new HashSet<Clause>();
			for(Clause c : clauses){
				Collection<Integer> literals = c.getLiterals();
				if(literals.contains(-d)){
					obsolete.add(c);
					literals.remove(-d);
					newClauses.add(new DNFClause(literals));
				}
			}
			clauses.removeAll(obsolete);
			clauses.addAll(newClauses);
		}	
	}
	
	/**
	 * @return true if i implies j in the formula
	 */
	public boolean implication(int i, int j){
		for(Clause c : clauses){
			if(c.containsLiteral(i) && !c.containsLiteral(j))
				return false;
		}
		return true;
	}

	/**
	 * @return a set of clauses corresponding to or-groups of the variable v.	
	 */
	public Set<Clause> getOrGroups(int v) {
        Set<Set<Integer>> primes = new Prime(new DefaultDNFSolver(this, v)).positivePrimes();

		if(primes == null)
			return null;

		Set<Clause> result = new HashSet<Clause>();
		for(Set<Integer> prime : primes){
			if(prime.size() > 1 && isPositive(prime)){
				result.add(new DNFClause(prime));
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @return true if all integers in the set are positive
	 */
	private boolean isPositive(Set<Integer> set){
		for(int i : set){
			if(i < 0)
				return false;
		}
		return true;
	}
	
	/**
	 * Two literals i and j are mutual exclusive in a DNF iff they occur in each clause with different sign.
	 * @param i
	 * @param j
	 * @return true if the literals i and j are mutual exclusive in this formula.
	 */
	public boolean isMutex(int i, int j){
		for(Clause clause : clauses){
			if(clause.containsLiteral(i) && clause.containsLiteral(j))
				return false;
		}
		return true;
	}

	/**
	 * removes all occurrences of the variables (both positive and negative)in each clause. 
	 * @param vars variables to eliminate
	 * @return returns a DNF not containing the variables specified in parameter vars. 
	 */
	public DNF eliminateVariables(Set<Integer> vars) {
		
		Set<DNFClause> clauses = new HashSet<DNFClause>();
		for(Clause clause : this){
			Set<Integer> reduced = new HashSet<Integer>();
			for(int i : clause){
				if(!vars.contains(Math.abs(i)))
					reduced.add(i);
			}
			if(!reduced.isEmpty())
				clauses.add(new DNFClause(reduced));
		}
		return new DNF(clauses);
	}

}
