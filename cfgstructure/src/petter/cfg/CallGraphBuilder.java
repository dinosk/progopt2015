package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;


public class CallGraphBuilder extends AbstractVisitor{
    
    CompilationUnit cu;
    private HashMap<Procedure, ArrayList<Procedure>> callGraph;
    private ArrayList<State> visited;
    public CallGraphBuilder(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.callGraph = new HashMap<Procedure, ArrayList<Procedure>>();
        Iterator<Procedure> methodIterator = cu.iterator();
        while(methodIterator.hasNext()){
            Procedure nextProc = methodIterator.next();
            if(nextProc.getName().equals("main") || nextProc.getName().equals("$init"))continue;
            this.callGraph.put(nextProc, new ArrayList<Procedure>());
            this.enter(nextProc);
        }
        this.visited = new ArrayList<State>(); 
    }


    public boolean visit(Procedure s){
        this.visited.clear();
        return true;
    }
    
    public boolean visit(Assignment s){
        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            Procedure caller = s.getSource().getMethod();
            // System.out.println("These should be equal: "+caller+" "+currentProc);
            Procedure callee = cu.getProcedure(mc.getName());
            ArrayList<Procedure> calledProcs = this.callGraph.get(caller);
            calledProcs.add(callee);
            this.callGraph.put(caller, calledProcs);
        }
        return true;
    }

    public boolean visit(MethodCall m){
        Procedure caller = m.getDest().getMethod();
        Procedure callee = cu.getProcedure(m.getCallExpression().getName());
        // System.out.println("These should be equal: "+caller+" "+currentProc);
        ArrayList<Procedure> calledProcs = this.callGraph.get(caller);
        calledProcs.add(callee);
        this.callGraph.put(caller, calledProcs);
        return true;
    }

    public boolean visit(State s){
        System.out.println("Visiting "+s);
        if(visited.contains(s))return false;
        visited.add(s);
        return true;
    }

    public HashMap<Procedure, ArrayList<Procedure>> getCallGraph(){
        this.fullAnalysis();
        return this.callGraph;
    }

    public ArrayList<Procedure> getLeafProcs(){
        this.fullAnalysis();
        ArrayList<Procedure> leafProcs = new ArrayList<Procedure>();
        System.out.println("Call graph looks like: "+this.callGraph);
        for(Procedure proc : this.callGraph.keySet()){
            if(this.callGraph.get(proc).isEmpty()){
                leafProcs.add(proc);
            }
        }
        return leafProcs;
    }
}
