package gsd.fms;

import dk.itu.fms.formula.dnf.DNF;

import java.util.LinkedList;
import java.util.List;

public class ImplicationGraph {

    private DNF dnf;
    private List<Integer[]> implications = null;

    public ImplicationGraph(DNF dnf) {
        this.dnf = dnf;
    }

    public List<Integer[]> calcImplications() {
        List<Integer[]> result = new LinkedList<Integer[]>();
        for (int i : dnf.getVariables()) {
            for (int j : dnf.getVariables()) {
                if (i == j) continue;

                System.out.println("Working on: " + i + "," + j);

                if (dnf.implication(i,j))
                    result.add(new Integer[] {i, j});
            }
        }
        this.implications = result;
        return result;
    }

}
