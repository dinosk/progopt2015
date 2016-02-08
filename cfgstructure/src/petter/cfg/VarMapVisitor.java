package petter.cfg;

import java.io.*;
import java.util.*;

import petter.cfg.*;
import petter.cfg.edges.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.FindLocalVarsVisitor;


public class VarMapVisitor extends AbstractVisitor{

    private CompilationUnit cu;
    private HashMap<Procedure, HashMap<Integer, Variable>> procVarMap;
    private Procedure current_proc;
    private ArrayList<State> visited;

    public VarMapVisitor(HashMap<Procedure, HashMap<Integer, Variable>> procVarMap, Procedure proc){
        super(true); // forward reachability
        this.cu = cu;
        this.procVarMap = procVarMap;
        this.current_proc = proc;
        this.visited = new ArrayList<State>();
    }

    public boolean visit(Assignment s) {
        // Run the find locals visitor 
        s.getLhs().accept(new FindLocalVarsVisitor(this.procVarMap, this.current_proc));
        s.getRhs().accept(new FindLocalVarsVisitor(this.procVarMap, this.current_proc));
        return true;
    }

    public boolean visit(State s){
        if(visited.contains(s))return false;
        visited.add(s);
        return true;
    }

}
