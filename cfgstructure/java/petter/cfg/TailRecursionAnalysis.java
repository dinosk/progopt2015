package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;


public class TailRecursionAnalysis extends AbstractVisitor{

    CompilationUnit cu;
    ArrayList<String> conditions;
    TransitionFactory tf;
    public TailRecursionAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.conditions = new ArrayList<String>();
        this.tf = new TransitionFactory();
    }

    public HashSet<Integer> visit(Assignment s){
        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            Procedure caller = s.getDest().getMethod();
            Procedure callee = cu.getProcedure(mc.getName());
        }
        return true;
    }

    public HashSet<Integer> visit(MethodCall m){
        Procedure caller = m.getDest().getMethod();
        Procedure callee = cu.getProcedure(m.getCallExpression().getName());
        
        ArrayList<State> calleeStates = new ArrayList<State>();
        return true;
    }

    public HashSet<Integer> visit(State s){
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
            }
            for(Transition t : toRemove){
                Transition nop = this.tf.createNop(t.getSource(), s);
                Transition nop2 = this.tf.createNop(s, s.getMethod().getBegin());
                s.addInEdge(nop);
                t.removeEdge();
            }
            s.getMethod().refreshStates();
            return false;
        }
        return true;
    }
}
