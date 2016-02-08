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
    ArrayList<State> visited;
    boolean fixedPoint;

    // method to create assignments initializing all locals to 0 at procedure start
    public void initializeLocalVars(Procedure callee){
        if(callee.getInitializesLocals())return;
        int size = procVarMap.get(callee).size();
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
        callee.setInitLocals();
    }

    public TailRecursionAnalysis(CompilationUnit cu, HashMap<Procedure, HashMap<Integer, Variable>> procVarMap){
        super(true); // forward reachability
        this.cu=cu;
        this.conditions = new ArrayList<String>();
        this.tf = new TransitionFactory();
        this.reachedEnd = false;
        this.procVarMap = procVarMap;
        this.visited = new ArrayList<State>();
    }

    public boolean visit(Procedure p){
        this.currProc = p;
        this.visited.clear();
        this.fixedPoint = true;
        return true;
    }

    public boolean visit(State s){
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
                            // if recursive call add the edge to the array
                            toRemove.add(nextTransition);
                        }
                    }
                }
            }
            if(!toRemove.isEmpty()){
                // if the array toRemove contains edges we have tail recursion
                initializeLocalVars(s.getMethod());
                for(Transition t : toRemove){
                    Transition nop2 = this.tf.createNop(t.getSource(), t.getSource().getMethod().getBegin());
                    t.getSource().addInEdge(nop2);
                    t.removeEdge();
                    State newBegin = new State();
                    Transition beginNop = this.tf.createNop(newBegin, s.getMethod().getBegin());
                    newBegin.addOutEdge(beginNop);
                    // remove the edge and add new nop to the procedure start
                    t.getSource().getMethod().setBegin(newBegin);
                    t.getSource().getMethod().refreshStates();
                    t.getSource().getMethod().resetTransitions();
                }
                fixedPoint = false;
            }
            return false;
        }
        else{
            if(visited.contains(s))return false;
        }
        visited.add(s);
        return true;
    }
}
