package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;


public class TailRecursionAnalysis extends AbstractPropagatingVisitor<HashSet<Integer>>{

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
    public TailRecursionAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.conditions = new ArrayList<String>();
        this.tf = new TransitionFactory();
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

             
            // ArrayList<State> calleeStates = new ArrayList<State>();

            // for(State calleeState : callee.getStates()){
            //     calleeState.setProcedure(caller);
            //     calleeStates.add(calleeState);
            // }
            // State firstState = calleeStates.get(0);
            // State lastState = calleeStates.get(calleeStates.size()-1);
            // // System.out.println("first: "+firstState);
            // // System.out.println("last: "+lastState);

            // s.removeEdge();
            // Transition nopin = this.tf.createMethodCall(s.getSource(), firstState, mc);
            // s.getSource().addOutEdge(nopin);
            // Transition nopout = this.tf.createNop(lastState, s.getDest());
            // lastState.addOutEdge(nopout);
            // lastState.setEnd(false);
            // caller.refreshStates();
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
        
        ArrayList<State> calleeStates = new ArrayList<State>();

        // for(State calleeState : callee.getStates()){
        //     calleeState.setProcedure(caller);
        //     calleeStates.add(calleeState);
        // }
        // State firstState = calleeStates.get(0);
        // State lastState = calleeStates.get(calleeStates.size()-1);
        // // System.out.println("first: "+firstState);
        // // System.out.println("last: "+lastState);

        // m.removeEdge();
        // Transition nopin = this.tf.createMethodCall(m.getSource(), firstState, m.getCallExpression());
        // m.getSource().addOutEdge(nopin);

        // Transition nopout = this.tf.createNop(lastState, m.getDest());
        // lastState.addOutEdge(nopout);
        // lastState.setEnd(false);
        // caller.refreshStates();
        return b;
    }

    public HashSet<Integer> visit(State s, HashSet<Integer> newflow){
        System.out.println("Visiting state:"+ s.toString());
        System.out.println("Is it the last state? "+s.isEnd());
        if(s.isEnd()){
            Iterator<Transition> allIn = s.getInIterator();
            while(allIn.hasNext()){
                if(allIn.next() instanceof MethodCall){
                    System.out.println("========== There is Tail Recursion!");                    
                }
            }
        }
        HashSet<Integer> oldflow = dataflowOf(s);
        newflow = new HashSet<Integer>();
        HashSet<Integer> newval = lub(oldflow, newflow);
        dataflowOf(s, newval);
        return newval;
    }

    // public static void main(String[] args) throws Exception {
    //     CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
    //     TailRecursionAnalysis tr = new TailRecursionAnalysis(cu);
    //     Procedure __main = cu.getProcedure("main");
    //     tr.enter(__main, null);
    //     tr.fullAnalysis();
    //     Iterator<Procedure> allmethods = tr.cu.iterator();
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
