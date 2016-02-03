package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.IntegerConstant;


public class TailRecursionAnalysis extends AbstractVisitor{
    CompilationUnit cu;
    ArrayList<String> conditions;
    TransitionFactory tf;
    Procedure currProc;
    boolean reachedEnd;
    HashMap<Procedure, HashMap<Integer, Variable>> procVarMap;

    public void initializeLocalVars(Procedure callee){
        if(callee.initializesLocals)return;
        int size = procVarMap.get(callee).size();
        System.out.println("size of locals:"+procVarMap.get(callee));
        State temp;
        State oldbegin = null;
        for(int id : procVarMap.get(callee).keySet()){
            oldbegin = callee.getBegin();
            temp = new State();
            Transition newLocalInit = tf.createAssignment(temp, oldbegin, procVarMap.get(callee).get(id), new IntegerConstant(0));
            oldbegin.addInEdge(newLocalInit);
            temp.setBegin(true);
            callee.setBegin(temp);
            callee.refreshStates();
        }
        callee.resetTransitions();
        callee.initializesLocals = true;
    }

    public TailRecursionAnalysis(CompilationUnit cu, HashMap<Procedure, HashMap<Integer, Variable>> procVarMap){
        super(true); // forward reachability
        this.cu=cu;
        this.conditions = new ArrayList<String>();
        this.tf = new TransitionFactory();
        this.reachedEnd = false;
        this.procVarMap = procVarMap;
    }

    public boolean visit(Procedure p){
        this.currProc = p;
        return true;
    }

    public boolean visit(State s){
        // System.out.println("Visiting state:"+ s.toString());
        // System.out.println("Is it the last state? "+s.isEnd());
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
                else if(nextTransition instanceof Assignment){
                    Assignment assignment = (Assignment) nextTransition;
                    if(assignment.getRhs().hasMethodCall()){
                        petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) assignment.getRhs();
                        Procedure caller = assignment.getDest().getMethod();
                        Procedure callee = cu.getProcedure(mc.getName());
                        if(caller == callee){
                            System.out.println("========== There is Tail Recursion!");
                            toRemove.add(nextTransition);
                        }
                    }
                }
            }
            initializeLocalVars(currProc);
            for(Transition t : toRemove){
                Transition nop2 = this.tf.createNop(t.getSource(), s.getMethod().getBegin());
                t.getSource().addInEdge(nop2);
                t.removeEdge();
            }
            State newBegin = new State();
            Transition beginNop = this.tf.createNop(newBegin, s.getMethod().getBegin());
            newBegin.addOutEdge(beginNop);
            currProc.setBegin(newBegin);
            currProc.refreshStates();
            currProc.resetTransitions();
            System.out.println("Current begin:"+currProc.getBegin());
            return false;
        }
        return true;
    }
}
