package petter.cfg;

import java.io.*;
import java.util.*;

import petter.cfg.*;
import petter.cfg.edges.*;

public class NumOfCallsVisitor extends AbstractVisitor{

    private CompilationUnit cu;
    private HashMap<String, Integer> procCalls;
    private ArrayList<State> visited;

    public NumOfCallsVisitor(HashMap<String, Integer> procCalls){
        super(true); // forward reachability
        this.cu = cu;
        this.procCalls = procCalls;
        this.visited = new ArrayList<State>();
    }

    public HashMap<String, Integer> getProcCalls() {
        return this.procCalls;
    }

    public boolean visit(Assignment s) {
        if(s.getRhs().hasMethodCall()) {
            petter.cfg.expression.MethodCall meth_call = (petter.cfg.expression.MethodCall) s.getRhs();
            int count = this.procCalls.get(meth_call.getName()) + 1;
            this.procCalls.put(meth_call.getName(), count);
        }
        return true;
    }

    public boolean visit(MethodCall s) {
        int count = this.procCalls.get(s.getCallExpression().getName()) + 1;
        this.procCalls.put(s.getCallExpression().getName(), count);
        return true;
    }

    public boolean visit(State s){
        if(visited.contains(s))return false;
        visited.add(s);
        return true;
    }
}
