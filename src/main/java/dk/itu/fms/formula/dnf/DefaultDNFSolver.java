package dk.itu.fms.formula.dnf;

import java.util.*;

import dk.itu.fms.formula.Clause;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;


/**
 * This solver is initialised to compute only positive prime implicates
 * of the DNF used to create the solver.
 *
 */
public class DefaultDNFSolver implements DNFSolver {

    private ISolver solver;
    private IConstr lastConstraint;
    private Clause[] dnf;
    private VecInt vec;
    private IVec clauses;

    public DefaultDNFSolver(DNF dnf){
        this.dnf = dnf.getClauses();
        initSolver(new HashSet<Integer>());
    }

    public DefaultDNFSolver(DNF dnf, int v) {
        this(dnf, v, new HashSet<Integer>());
    }

    public DefaultDNFSolver(DNF dnf, int v, Set<Integer> eliminate) {
        Set<Clause> clauses = new HashSet<Clause>();

        for(Clause clause : dnf){
            if(!clause.containsLiteral(-v))
                clauses.add(clause);
        }

        this.dnf = new Clause[clauses.size()];

        int i = 0;
        for(Clause clause : clauses)
            this.dnf[i++] = clause;
        initSolver(eliminate);

    }

    private boolean initSolver(Set<Integer> eliminate) {


        solver = SolverFactory.newDefault();
        clauses = new Vec();
        int x = 0;
        for(Clause clause : dnf) {
            VecInt vec = new VecInt();
            for(int i : clause){
                if(i > 0 && !eliminate.contains(i))
                    vec.push(Math.abs(i));
            }
            if(vec.isEmpty())
                return false;
            clauses.insertFirst(vec);
        }
        try {
            solver.addAllClauses(clauses);
        } catch (ContradictionException e) {
            return false;

        }

        vec = new VecInt();
        for(int i = solver.nVars(); i > 0; i--)
            vec.insertFirst(i);

        return true;
    }

    @Override
    public boolean addConstraint(int k) {
        try {
            lastConstraint = solver.addAtMost(vec, k);
        } catch (ContradictionException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean addClause(int[] a) {
        IVecInt vec = new VecInt(a);
        try {
            reset();
            solver.addClause(vec);
            clauses.insertFirst(vec);
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
            // TODO Auto-generated catch block
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

    public void reset() {
        solver = SolverFactory.newDefault();
        try {
            solver.addAllClauses(clauses);
        } catch (ContradictionException e) {
        }
    }

}
