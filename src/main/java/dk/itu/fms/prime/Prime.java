package dk.itu.fms.prime;
import dk.itu.fms.formula.dnf.DNFSolver;

import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of the Prime algorithm optimised to compute only 
 * positive prime implicates of a DNF formula.
 */
public class Prime {

	private DNFSolver solver;
	
	/**
	 * 
	 * @param solver : a solver initialised to compute only positive 
	 * prime implicates of a DNF.
	 */
	public Prime(DNFSolver solver) {
		this.solver = solver;
	}
	
	/**
	 * 
	 * @return a set containing all positive prime implicates of a DNF formula.
	 */
	public Set<Set<Integer>> positivePrimes() {
		// the set of prime implicates
		Set<Set<Integer>> primes = new HashSet<Set<Integer>>();
		while(true){
			Set<Integer> prime = min_prime_implicate();
	        if (prime == null){
	        	return primes;
	        }
	        primes.add(prime);
	        // add the negated prime implicate to the solver, to avoid the recomputation
	        // of the implicate
	        int[] clause = new int[prime.size()];
	        int i = 0;
	        for(int j : prime)
	        	clause[i++] = -j;
	        if(!solver.addClause(clause))
	        	return primes;
		}
	}
	
	/**
	 * 
	 * @return a minimal implicate of the formula currently in the solver
	 */
	private Set<Integer> min_prime_implicate()  {
		// the number of variables in the formula is a trivial upper bound
		// to the problem
		int k = solver.numberOfVars();
        boolean status;
        int[] model = null;
        	while(k > 0){
        		if(!solver.addConstraint(k))
        			status = false;
        		else
        			status = solver.isSatisfiable();
        			
	        	if(status){
	        		model = solver.model();
	        		k = -1;
	        		for(int i = 0; i < model.length; i++){
	        			if(model[i] > 0) k++;
	        		}
	        	} else {
	        		k++;
	        		break;
	        	}
	        }
	        Set<Integer> prime = new HashSet<Integer>();
	        
	        if(model == null)
	        	return null;
			for(int i = 0; i < model.length; i++){
				if(model[i] > 0){
					prime.add(model[i]);
				}
			}
			return prime;
	}
	
}
