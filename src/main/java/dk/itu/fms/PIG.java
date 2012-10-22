package dk.itu.fms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import dk.itu.fms.formula.Clause;
import dk.itu.fms.formula.cnf.CNF;
import dk.itu.fms.formula.cnf.CNFClause;


public class PIG {

    protected CNF T; //containsClause constant time
    private boolean computed;
    protected Hashtable<Clause, Set<Clause>> complements;
    protected Hashtable<Clause, Clause> bestComplement;

    public PIG(CNF T){
        this.T = T;
        for(Clause c : T)
            c.setCost(Integer.MAX_VALUE);
        complements = new Hashtable<Clause, Set<Clause>>();
        bestComplement = new Hashtable<Clause,Clause>();
    }

    protected CNF computePrimeImplicates(){
        PriorityQueue<Clause> S = initS();
        if(S.isEmpty())
            return T;
        //4
        Clause ci;
        while(!S.isEmpty()){
            ci = null;
            boolean returnTo4 = true;
            while(returnTo4){
                do {
                    if (S.isEmpty())
                        return T;
                    //4.1
                    ci = S.poll();
                    //4.2
                } while (!T.containsClause(ci)); // return to 4
                //4.3
                if(bestComplement.get(ci) == null || !T.containsClause(bestComplement.get(ci))){
                    Clause b = bestComplement.remove(ci);
                    if(b != null)
                        complements.get(ci).remove(b);
                    // recompute minCost
                    if(!complements.get(ci).isEmpty()){
                        minCost(ci);
                        // and reschedule ci
                        S.add(ci);
                    }
                }
                else
                    returnTo4 = false;
            }

            if(ci != null){ // should not be possible to be false
                //4.4
                CNFClause cj = (CNFClause) bestComplement.remove(ci);
                CNFClause resolvent = cj.computeResolvent( (CNFClause) ci);
                complements.get(ci).remove(cj);
                //4.5
                if (resolvent.getLiterals().isEmpty())
                    return new CNF(); // return false;
                //4.6
                if(T.addClause(resolvent)){ // det er her der bliver fjernet clauses fra T
                    //4.7 if resolvent survives forwardSubsumption
                    complements.put(resolvent, complement(resolvent));
                    if(!complements.get(resolvent).isEmpty())
                        S.add(resolvent);
                }
                //4.8 reschedule ci
                if(!complements.get(ci).isEmpty()){
                    minCost(ci);
                    S.add(ci);
                }

            }
        }
        computed = true;
        return T;
    }

    protected PriorityQueue<Clause> initS() {
        //1+2
        List<Clause> S = new ArrayList<Clause>();
        //T is a HashSet iterator does not provide a specific order
        Clause[] tmpT = T.getClauses();
        for(int i = 0; i < tmpT.length; i++){
            Set<Clause> complement = complementInit(i, tmpT);
            if (!complement.isEmpty()){
                S.add(tmpT[i]);
                complements.put(tmpT[i], complement);
            }
        }
        //3

        if(!S.isEmpty()){
            PriorityQueue<Clause> sortedS = new PriorityQueue<Clause>(S.size(), new PIG.CostComparator<Clause>());
            sortedS.addAll(S);
            return sortedS;
        }
        else return new PriorityQueue<Clause>();
    }

    private void minCost(Clause ci) {
        for(Clause cj : complements.get(ci)){
            int cost = cost(ci,cj);
            setMinCost(ci, cj, cost);
        }
    }

    protected void setMinCost(Clause ci, Clause cj, int cost){
        if(bestComplement.get(ci) == null || cost < ci.getCost()){
            ci.setCost(cost);
            bestComplement.put(ci, cj);
        }
    }

    protected class CostComparator<T extends Clause> implements Comparator<T> {

        public int compare(T ci, T cj) {

            return ci.getCost() - cj.getCost();
        }

    }

    //only used in initS()
    protected Set<Clause> compsInit(int i, Clause[] tmpT){
        Set<Clause> comps = new HashSet<Clause>();
        Clause ci = tmpT[i];
        Collection<Integer> LHSi = ci.getPositiveLiterals();
        Collection<Integer> RHSi = ci.getNegativeLiterals();
        for(int j = i+1; j < T.size(); j++){
            Clause cj = tmpT[j];
            Collection<Integer> LHSj = cj.getPositiveLiterals();
            Collection<Integer> RHSj = cj.getNegativeLiterals();
            LHSj.retainAll(RHSi);
            RHSj.retainAll(LHSi);
            if(LHSj.size() + RHSj.size() == 1){
                setMinCost(ci, cj, cost(ci,cj));
                comps.add(cj);
            }
        }
        return comps;
    }

    public  Set<Clause> comps(Clause ci){
        Set<Clause> comps = new HashSet<Clause>();
        Collection<Integer> LHSi = ci.getPositiveLiterals();
        Collection<Integer> RHSi = ci.getNegativeLiterals();
        for(Clause cj : T){
            Collection<Integer> LHSj = cj.getPositiveLiterals();
            Collection<Integer> RHSj = cj.getNegativeLiterals();
            LHSj.retainAll(RHSi);
            RHSj.retainAll(LHSi);
            if(LHSj.size() + RHSj.size() == 1){
                setMinCost(ci, cj, cost(ci,cj));
                comps.add(cj);
            }
        }
        return comps;
    }

    protected Set<Clause> complementInit(int i, Clause[] tmpT){
        Set<Clause> comps = compsInit(i, tmpT);
        return preemptForwardSubsumption(tmpT[i], new ArrayList<Clause>(comps));
        //return comps;
    }

    protected Set<Clause> complement(Clause c){
        // Need to implement "Preempt forward subsumption"
        Set<Clause> comps = comps(c);
        return preemptForwardSubsumption(c, new ArrayList<Clause>(comps));

        //return comps(c);
    }

    protected int cost(Clause ci, Clause cj){
        Set<Integer> LHSi = ci.getPositiveLiterals();
        Set<Integer> LHSj = cj.getPositiveLiterals();
        Set<Integer> RHSi = ci.getNegativeLiterals();
        Set<Integer> RHSj = cj.getNegativeLiterals();
        LHSi.retainAll(LHSj);
        RHSi.retainAll(RHSj);
        return ci.size() + cj.size() - LHSi.size() - RHSi.size();

    }
    /**
     * precondition: clauses are subsumption free.
     * @param ci
     * @param clauses
     * @return
     */
    public Set<Clause> preemptForwardSubsumption(Clause ci, ArrayList<Clause> clauses){
        Clause[] tmp = new Clause[clauses.size()];
        Set<Clause> toRemove = new HashSet<Clause>();
        int i = 0;
        for(Clause clause : clauses){
            tmp[i++] = clause.difference(ci);
        }
        for(i = 0; i < tmp.length; i++){
            for(int j = i+1; j < tmp.length; j++){
                if(tmp[i].contains(tmp[j])){
                    toRemove.add(clauses.get(i));
                }
                else if(tmp[j].contains(tmp[i]))
                    toRemove.add(clauses.get(j));
            }
        }
        clauses.removeAll(toRemove);
        return new HashSet<Clause>(clauses);
    }

    public CNF getPrimeImplicates(){
        if(!computed)
            T = computePrimeImplicates();
        return T;
    }

    public Set<Clause> getPositivePrimeImplicates(){
        Set<Clause> primes = new HashSet<Clause>();
        for(Clause prime : getPrimeImplicates()){
            if(prime.isPositive()){
                primes.add(prime);
            }
        }
        return primes;
    }
    public List<Clause> getOrGroups(){
        List<Clause> primes = new ArrayList<Clause>();
        for(Clause prime : getPrimeImplicates()){
            //stupid to get copy here...
            if(prime.getNegativeLiterals().isEmpty() && prime.size() > 1){
                primes.add(prime);
            }
        }
        return primes;
    }
}








