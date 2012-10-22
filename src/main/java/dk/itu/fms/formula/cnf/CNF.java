package dk.itu.fms.formula.cnf;

import dk.itu.fms.PIG;
import dk.itu.fms.formula.Clause;
import dk.itu.fms.formula.Formula;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class CNF extends Formula {
	
	//List of clauses in which literal appear
	private Hashtable<Integer, Set<CNFClause>> clauseSet = new Hashtable<Integer, Set<CNFClause>>();
	//watched literals
	private Hashtable<Integer, Set<CNFClause>> wcl = new Hashtable<Integer, Set<CNFClause>>();

	private CNFSolver solver;
	// this variable indicates if there has been made changes to the formula which requires the
	// solver to be instantiated again.
	private boolean changed;

	public CNF(){
		solver = new DefaultCNFSolver();
		changed = true;
	}

	public CNF(Collection<CNFClause> clauses) {
		solver = new DefaultCNFSolver();
		addAllClauses(clauses);
	}

	public void changeSolver(CNFSolver solver){
		this.solver = solver;
		changed = true;
	}

	private void initSolver() {
		solver = solver.initSolver(this); 
		changed = false;
	}

	public CNF clone() {
		CNF clone = new CNF();
		for(Clause clause : this.clauses){
			CNFClause clauseClone = new CNFClause(clause.getLiterals());
			clone.clauses.add(clauseClone);
			clone.deadFeatures = new HashSet<Integer>(this.deadFeatures);
			for(int i : clauseClone){
				if(clone.clauseSet.get(i) == null){
					Set<CNFClause> set = new HashSet<CNFClause>();
					set.add(clauseClone);
					clone.clauseSet.put(i, set);
					clone.variables.add(Math.abs(i));
				}
				else{
					clone.clauseSet.get(i).add(clauseClone);
				}
			}
			clone.markWatchedLiteral(clauseClone);
		}
		return clone;
	}

	public boolean addClause(CNFClause clause) {
		if(!isForwardSubsumed(clause)){
			backwardSubsumedBy(clause);
			clauses.add(clause);
			for(int i : clause){
				if(clauseSet.get(i) == null){
					Set<CNFClause> set = new HashSet<CNFClause>();
					set.add(clause);
					clauseSet.put(i, set);
					variables.add(Math.abs(i));
				}
				else{
					clauseSet.get(i).add(clause);
				}
			}
			markWatchedLiteral(clause);
			changed = true;
			return true;
		}
		return false; 	
	}

	public void addAllClauses(Collection<CNFClause> clauses) {
		for(CNFClause clause : clauses )
			addClause(clause);
	}

	public boolean removeClause(CNFClause clause){
		if(this.clauses.remove(clause)){
			for(int i : clause){
				clauseSet.get(i).remove(clause);
				if(wcl.get(i) != null)
					wcl.get(i).remove(clause);
				if(wcl.get(-i) != null)
					wcl.get(-i).remove(clause);
				if(clauseSet.get(i).isEmpty() && (clauseSet.get(-i) == null || clauseSet.get(-i).isEmpty()))
					variables.remove(Math.abs(i));
			}
			changed = true;
			return true;
		}
		return false;
	}

	public void removeAllClauses(Set<CNFClause> clauses){
		for(CNFClause c : clauses){
			removeClause(c);
		}
	}

	public boolean isSatisfiable() {
		if(changed)
			initSolver();
		if(changed)
			return false;
		return solver.isSatisfiable();
	}

	/**
	 * @param i
	 * @return true if the formula is satisfiable assuming the literal i is assigned true.
	 */
	public boolean isSatisfiable(int i) {
		if(changed)
			initSolver();
		if(changed)
			return false;
		return solver.isSatisfiable(i);
	}

	/**
	 * 
	 * @return true if the formula is satisfiable assuming the the literals specified in assume are all true.
	 */
	public boolean isSatisfiable(int[] assume) {
		if(changed)
			initSolver();
		if(changed)
			return false;
		return solver.isSatisfiable(assume);
	}

	public void removeDeadFeatures() {
		//compute the dead features
		if(solver == null || changed)
			initSolver();
		for(int i : this.getVariables()){
			// check satisfiability assuming i is true;
			if(!isSatisfiable(i))
				deadFeatures.add(i);
		}
		//and remove them
		removeDeadFeature(deadFeatures);	
	}

	private void removeDeadFeature(Set<Integer> df) {
		for(int i : df){
			variables.remove(i);
			if(clauseSet.get(-i) != null){
				//clauses.removeAll(clauseSet.remove(-i));
				removeAllClauses(new HashSet<CNFClause>(clauseSet.get(-i)));
			}
			if(clauseSet.get(i) != null){
				for(CNFClause c : clauseSet.get(i)){
					if(clauses.remove(c)){
						CNFClause resolvent = c.computeResolvent(new CNFClause(new int[]{-i}));
						if(!resolvent.getLiterals().isEmpty())
							addClause(resolvent);
					}
					clauseSet.remove(i);
				}
			}
		}
		if(!df.isEmpty())
			changed = true;
	}


	/**
	 * @param i
	 * @param j
	 * @return true if i implies j in the formula.
	 */
	public boolean implication(int i, int j) {
		if(solver == null || changed)
			initSolver();
		if(!isSatisfiable(new int[]{i, -j}))
			return true;
		else 
			return false;
	}

	/**
	 * Should only be called after a call to implication or isSatisfiable
	 * @param i
	 * @return the truth value assigned to variable i in the current assignment.
	 */
	public boolean assignment(int i){
		return solver.model(i);
	}

	/**
	 * @return true if i and j are mutually exclusive in the formula.
	 */
	public boolean isMutex(int i, int j){
		if(implication(i,-j))
			return true;
		return false;
	}

	public Set<Clause> getOrGroups(int v){
		Set<Clause> result = new HashSet<Clause>();
		CNF clone = this.clone();
		clone.addClause(new CNFClause(new int[]{v}));
		for(Clause prime : new PIG(clone).getPositivePrimeImplicates()){
			if(prime.size() > 1)
				result.add(prime);
		}
		return result;
	}

	public Set<CNFClause> getClauseSet(int literal){
		return clauseSet.get(literal);
	}

	public Formula eliminateVariables(Set<Integer> vars){
		return VariableElimination.variableElimination(this, vars);
	}
	

	/**
	 * Removes all clause in this cnf which are backward subsumed by the clause c.
	 * @param c
	 */
	private void backwardSubsumedBy(CNFClause c){
		HashSet<Clause> subsumed = new HashSet<Clause>(clauses);
		for(Integer i : c){
			if(clauseSet.get(i) != null)
				subsumed.retainAll(clauseSet.get(i));
			else{
				subsumed = new HashSet<Clause>();
				break;
			}
		}
		for(Clause s : subsumed){
			removeClause((CNFClause) s);
		}
	}


	/**
	 * 
	 * @param c
	 * @return true if the clause c is forward subsumed.
	 */
	private boolean isForwardSubsumed(CNFClause c){
		HashSet<Integer> marked = new HashSet<Integer>();
		for(Integer i : c){
			marked.add(i);
			Set<CNFClause> wc = wcl.get(i);
			if(wc != null){
				//wc.retainAll(clauses);
				
				Iterator<CNFClause> itr = wc.iterator();
				while(itr.hasNext()){
					int i1 = 0;
					CNFClause c1 = itr.next();
					for(Integer j : c1){
						if(!marked.contains(j)){
							i1 = j;
							break;
						}
					}
					if(i1 == 0){
						return true;
					}
					else{
						itr.remove();
						wc = wcl.get(i1);
						if(wc != null)
							wc.add(c1);
						else{
							Set<CNFClause> set = new HashSet<CNFClause>();
							set.add(c1);
							wcl.put(i1, set);
						}
					}
				}
			}
		}
		return false;
	}

	private void markWatchedLiteral(CNFClause clause){
		int watched = clause.getRandomLiteral();
		if(wcl.get(watched) == null){
			Set<CNFClause> set = new HashSet<CNFClause>();
			set.add(clause);
			wcl.put(watched, set);
		}
		else {
			wcl.get(watched).add(clause);
		}
	}
}
