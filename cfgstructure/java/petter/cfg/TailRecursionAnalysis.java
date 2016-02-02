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
        if(b == null) b = new HashSet<Integer>();
        return b;
    }


    public HashSet<Integer> visit(GuardedTransition s, HashSet<Integer> b){
        return b;
    }


    public HashSet<Integer> visit(Assignment s, HashSet<Integer> b){
        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            Procedure caller = s.getDest().getMethod();
            Procedure callee = cu.getProcedure(mc.getName());
        }
        return b;
    }

    public HashSet<Integer> visit(MethodCall m, HashSet<Integer> b){
        Procedure caller = m.getDest().getMethod();
        Procedure callee = cu.getProcedure(m.getCallExpression().getName());
        
        ArrayList<State> calleeStates = new ArrayList<State>();
        return b;
    }

    public HashSet<Integer> visit(State s, HashSet<Integer> newflow){
        System.out.println("Visiting state:"+ s.toString());
        System.out.println("Is it the last state? "+s.isEnd());
        if(s.isEnd()){
            Iterator<Transition> allIn = s.getInIterator();
            ArrayList<Transition> toRemove = new ArrayList<Transition>();
            ArrayList<Transition> newInEdges = new ArrayList<Transition>();
            ArrayList<Transition> newOutEdges = new ArrayList<Transition>();

            while(allIn.hasNext()){
                Transition nextTransition = allIn.next();
                if(nextTransition instanceof MethodCall){
                    MethodCall mc = (MethodCall) nextTransition;
                    Procedure caller = mc.getDest().getMethod();
                    Procedure callee = cu.getProcedure(mc.getCallExpression().getName());
                    if(caller == callee){
                        System.out.println("========== There is Tail Recursion!");
                        toRemove.add(nextTransition);
                    }
                }
                // else if(nextTransition instanceof Assignment){
                //     Assignment assignment = (Assignment) nextTransition;
                //     if(assignment.getRhs().hasMethodCall()){
                //         petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) assignment.getRhs();
                //         Procedure caller = assignment.getDest().getMethod();
                //         Procedure callee = cu.getProcedure(mc.getName());
                //         if(caller == callee){
                //             System.out.println("========== There is Tail Recursion!");
                //         }
                //     }
                // }
            }
            for(Transition t : toRemove){
                Transition nop = this.tf.createNop(t.getSource(), s);
                Transition nop2 = this.tf.createNop(s, s.getMethod().getBegin());
                s.addInEdge(nop);
                t.removeEdge();
            }
            s.getMethod().refreshStates();
            return null;
        }
        HashSet<Integer> oldflow = dataflowOf(s);
        newflow = new HashSet<Integer>();
        HashSet<Integer> newval = lub(oldflow, newflow);
        dataflowOf(s, newval);
        return newval;
    }
}
