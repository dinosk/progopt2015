package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;


public class CallGraphBuilder extends AbstractVisitor{
    
    CompilationUnit cu;
    private HashMap<Procedure, ArrayList<Procedure>> callGraph;
    public CallGraphBuilder(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.callGraph = new HashMap<Procedure, ArrayList<Procedure>>();
        Iterator<Procedure> methodIterator = cu.iterator();
        while(methodIterator.hasNext()){
            this.callGraph.put(methodIterator.next(), new ArrayList<Procedure>());
        }
    }

    public boolean visit(Procedure s){
        // System.out.println("Visiting Procedure: "+s.getName());
        return true;
    }


    public boolean visit(GuardedTransition s){
        // System.out.println("Visiting: if with guard: "+s.getAssertion());
        // System.out.println("b: "+s.getOperator());
        return true;
    }


    public boolean visit(Assignment s){
        // System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
        // System.out.println("original Destination: "+s.getDest());
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
        // method calls need special attention; in this case, we just 
        // continue with analysing the next state and triggering the analysis
        // of the callee
        // System.out.println("Visiting: MethodCall of: "+m.getCallExpression().getName());
        Procedure caller = m.getDest().getMethod();
        Procedure callee = cu.getProcedure(m.getCallExpression().getName());
        // System.out.println("These should be equal: "+caller+" "+currentProc);
        ArrayList<Procedure> calledProcs = this.callGraph.get(caller);
        calledProcs.add(callee);
        this.callGraph.put(caller, calledProcs);
        return true;
    }

    public boolean visit(State s, boolean newflow){
        // System.out.println("Visiting state:"+ s.toString());
        return true;
    }

    public HashMap<Procedure, ArrayList<Procedure>> getCallGraph(){
        this.fullAnalysis();
        return this.callGraph;
    }

    public ArrayList<Procedure> getLeafProcs(){
        this.fullAnalysis();
        ArrayList<Procedure> leafProcs = new ArrayList<Procedure>();
        for(Procedure proc : this.callGraph.keySet()){
            if(this.callGraph.get(proc).isEmpty()){
                leafProcs.add(proc);
            }
        }
        return leafProcs;
    }
}
