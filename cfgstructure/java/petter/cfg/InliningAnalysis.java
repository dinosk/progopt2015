package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;


public class InliningAnalysis extends AbstractPropagatingVisitor<HashSet<Integer>>{

    static HashSet<Integer> lub(HashSet<Integer> b1, HashSet<Integer> b2){
        if (b1==null) return b2;
        if (b2==null) return b1;
        HashSet<Integer> theunion = new HashSet<Integer>();
        theunion.addAll(b1);
        theunion.addAll(b2);
        return theunion;
    }

    CompilationUnit cu;
    ArrayList<String> conditions;
    TransitionFactory tf;
    ArrayList<Procedure> leafProcs;
    public InliningAnalysis(CompilationUnit cu, ArrayList<Procedure> leafProcs){
        super(true); // forward reachability
        this.cu=cu;
        this.conditions = new ArrayList<String>();
        this.tf = new TransitionFactory();
        this.leafProcs = leafProcs;
    }

    public void inline(Procedure caller, Procedure callee, Assignment s){
        System.out.println("Inlining "+callee.getName());
        ArrayList<State> calleeStates = new ArrayList<State>();
        petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
        for(State calleeState : callee.getStates()){
            calleeState.setProcedure(caller);
            calleeStates.add(calleeState);
        }
        State firstState = calleeStates.get(0);
        State lastState = calleeStates.get(calleeStates.size()-1);
        // System.out.println("first: "+firstState);
        // System.out.println("last: "+lastState);

        s.removeEdge();
        Transition nopin = this.tf.createMethodCall(s.getSource(), firstState, mc);
        s.getSource().addOutEdge(nopin);
        Transition nopout = this.tf.createNop(lastState, s.getDest());
        lastState.addOutEdge(nopout);
        lastState.setEnd(false);
        caller.refreshStates();
    }

    public void inline(Procedure caller, Procedure callee, MethodCall m){
        System.out.println("Inlining "+callee.getName());
        ArrayList<State> calleeStates = new ArrayList<State>();

        for(State calleeState : callee.getStates()){
            calleeState.setProcedure(caller);
            calleeStates.add(calleeState);
        }
        State firstState = calleeStates.get(0);
        State lastState = calleeStates.get(calleeStates.size()-1);
        // System.out.println("first: "+firstState);
        // System.out.println("last: "+lastState);

        m.removeEdge();
        Transition nopin = this.tf.createMethodCall(m.getSource(), firstState, m.getCallExpression());
        m.getSource().addOutEdge(nopin);

        Transition nopout = this.tf.createNop(lastState, m.getDest());
        lastState.addOutEdge(nopout);
        lastState.setEnd(false);
        caller.refreshStates();
    }

    public HashSet<Integer> visit(Procedure s, HashSet<Integer> b){
        // System.out.println("Visiting Procedure: "+s.getName());
        if(b == null) b = new HashSet<Integer>();
        return b;
    }


    public HashSet<Integer> visit(GuardedTransition s, HashSet<Integer> b){
        // System.out.println("Visiting: if with guard: "+s.getAssertion());
        // System.out.println("b: "+s.getOperator());
        return b;
    }


    public HashSet<Integer> visit(Assignment s, HashSet<Integer> b){
        // System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
        // System.out.println("original Destination: "+s.getDest());
        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            Procedure caller = s.getDest().getMethod();
            Procedure callee = cu.getProcedure(mc.getName());
            if(leafProcs.contains(callee)){
                inline(caller, callee, s);
            }
        }
        return b;
    }

    public HashSet<Integer> visit(MethodCall m, HashSet<Integer> b){
        // method calls need special attention; in this case, we just 
        // continue with analysing the next state and triggering the analysis
        // of the callee
        // System.out.println("Visiting: MethodCall of: "+m.getCallExpression().getName());
        // System.out.println("original Destination: "+m.getDest());
        
        Procedure caller = m.getDest().getMethod();
        Procedure callee = cu.getProcedure(m.getCallExpression().getName());
        if(leafProcs.contains(callee)){
            inline(caller, callee, m);
        }
        return b;
    }

    public HashSet<Integer> visit(State s, HashSet<Integer> newflow){
        // System.out.println("Visiting state:"+ s.toString());
        HashSet<Integer> oldflow = dataflowOf(s);
        newflow = new HashSet<Integer>();
        HashSet<Integer> newval = lub(oldflow, newflow);
        dataflowOf(s, newval);
        return newval;
    }

    // public static void main(String[] args) throws Exception {
    //     CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
    //     InliningAnalysis ra = new InliningAnalysis(cu);
    //     Procedure __main = cu.getProcedure("main");
    //     ra.enter(__main, null);
    //     ra.fullAnalysis();
    //     Iterator<Procedure> allmethods = ra.cu.iterator();        
    //     while(allmethods.hasNext()){
    //         Procedure proc = allmethods.next();
    //         DotLayout layout = new DotLayout("jpg", proc.getName()+"After.jpg");
    //         for (State s: proc.getStates()){
    //             layout.highlight(s,(ra.dataflowOf(s))+"");
    //         }
    //         layout.callDot(proc);
    //     }
    // }
}
