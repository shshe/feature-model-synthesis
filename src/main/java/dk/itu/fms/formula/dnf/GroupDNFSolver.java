package dk.itu.fms.formula.dnf;

import java.util.*;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dk.itu.fms.formula.Clause;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;


/**
 * This solver is initialised to compute only positive prime implicates
 * of the DNF used to create the solver.  
 *
 */
public class GroupDNFSolver implements DNFSolver {

	private ISolver solver = SolverFactory.newDefault();
    private List<IConstr> lastConstrs = new LinkedList<IConstr>();
    private IVecInt literals;

    // Clauses that contain -key
//    private Multimap<Integer, IConstr> negClauses = HashMultimap.create();
	
    public GroupDNFSolver(DNF dnf, int v) {
        for(Clause clause : dnf)
            if(!clause.containsLiteral(-v)) // FIXME
                addClause(clause);

        this.literals = new VecInt();
        for(int i = solver.nVars(); i > 0; i--)
            this.literals.insertFirst(i);
    }

	@Override
	public boolean addConstraint(int k) {
		try {
			lastConstrs.add(solver.addAtMost(literals, k));
		} catch (ContradictionException e) {
			return false;
		}
		return true;
	}

    public boolean addAssumptions(int[] vs) {
        try {
            IConstr constr = solver.addClause(new VecInt(vs));
            if (constr != null)
                lastConstrs.add(constr);
        }
        catch (ContradictionException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void reset() {
        for (IConstr constr : lastConstrs) {
            solver.removeConstr(constr);
        }
        this.lastConstrs = new LinkedList<IConstr>();
    }

    @Override
    public boolean addClause(int[] a) {
        return addClause(new DNFClause(a));
    }

    public boolean addClause(Iterable<Integer> clause) {
		try {
            VecInt vec = new VecInt();
            for(int i : clause) {
                if(i > 0)
                    vec.push(Math.abs(i));
            }
			solver.addClause(vec);

            if(vec.isEmpty())
                System.err.println("Warning, empty clause after variable elimination");

//            Add mapping from variable to clauses that contain its negation
//            for (int i : clause)
//                if (i < 0)
//                    negClauses.put(-i, constr);

		} catch (ContradictionException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isSatisfiable() {
		try {
			return solver.isSatisfiable();
		} catch (TimeoutException e) {
			e.printStackTrace();
			return false;	
		}
	}

	@Override
	public int[] model() {
		return solver.model();
	}

	@Override
	public int numberOfVars() {
		return solver.nVars();
	}

}
