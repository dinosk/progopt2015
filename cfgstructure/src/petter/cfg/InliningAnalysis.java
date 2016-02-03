package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.RenamingVisitor;


public class InliningAnalysis extends AbstractVisitor{

    private CompilationUnit cu;
    private TransitionFactory tf;
    private ArrayList<Procedure> methodsToInline;
    private HashMap<Procedure, HashMap<Integer, Variable>> procVarMap;
    private ArrayList<MethodCall> alreadyInlinedMC;
    private ArrayList<Assignment> alreadyInlinedAS;
    private boolean reachedEnd;
    public boolean hasWork;
    Procedure currProc;

    public InliningAnalysis(CompilationUnit cu, ArrayList<Procedure> methodsToInline, HashMap<Procedure, HashMap<Integer, Variable>> procVarMap){
        super(true); // forward reachability
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.methodsToInline = methodsToInline;
        methodsToInline.remove(cu.getProcedure("main"));
        methodsToInline.remove(cu.getProcedure("$init"));
        System.out.println("Will inline the following methods:"+methodsToInline);
        this.procVarMap = procVarMap;
        this.alreadyInlinedAS = new ArrayList<Assignment>();
        this.alreadyInlinedMC = new ArrayList<MethodCall>();
        this.reachedEnd = false;
    }

    public State renameVars(State os, Procedure p, Variable toReturn){
        Iterator<Transition> outEdges = os.getOutIterator();
        Assignment assignment = null;
        boolean addNew = false;
        while(outEdges.hasNext()){
            Transition outEdge = outEdges.next();
            if(outEdge instanceof Assignment){
                assignment = (Assignment) outEdge;
                System.out.println(assignment.getLhs().toString());
                System.out.println(assignment.getRhs().toString());
                if(assignment.getLhs().toString() == "return"){
                    if(toReturn != null)
                        assignment.setLhs(toReturn);
                    outEdges.remove();
                    addNew = true;
                }
                else {
                    if(assignment.getLhs() instanceof Variable) {
                        Variable lhs = (Variable) assignment.getLhs();
                        lhs.accept(new RenamingVisitor(p));
                        assignment.setLhs(lhs);

                        Expression rhs = assignment.getRhs();
                        rhs.accept(new RenamingVisitor(p));
                        assignment.setRhs(rhs);
                    }
                }
            }
            else if(outEdge instanceof GuardedTransition){
                System.out.println("renaming a GuardedTransition");
                GuardedTransition gt = (GuardedTransition) outEdge;
                gt.getAssertion().accept(new RenamingVisitor(p));
            }
        }
        if(addNew){
            os.addOutEdge(assignment);
        }
        return os;
    }

    public void initializeLocalVars(Procedure callee) {
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
            callee.setBegin(temp);
            callee.refreshStates();
        }
        callee.resetTransitions();
        callee.initializesLocals = true;
    }

    public void inline(Procedure caller, Procedure callee, Assignment s){
        initializeLocalVars(callee);
        System.out.println("Assignment Inlining: "+callee.getName());
        Variable toReturn = (Variable) s.getLhs();
        ArrayList<State> calleeStates = new ArrayList<State>();
        petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
        
        for(State calleeState : callee.getStates()){
            calleeState = renameVars(calleeState, callee, toReturn);
            System.out.println("changing state "+calleeState.toString());
            calleeState.setProcedure(caller);
            calleeStates.add(calleeState);
        }
        List<State> calleeStatesList = new ArrayList<State>(calleeStates);
        Collections.sort(calleeStatesList, new Comparator<State>(){
            public int compare(State o1, State o2){
                Integer id1 = Integer.parseInt(o1.toString().substring(7));
                Integer id2 = Integer.parseInt(o2.toString().substring(7));
                return id1.compareTo(id2);
            }
        });

        // System.out.println("Callee States: "+calleeStatesList);
        State firstState = callee.getBegin();
        firstState.setBegin(false);
        State lastState = callee.getEnd();
        lastState.setEnd(false);
        // System.out.println("first: "+firstState);
        // System.out.println("last: "+lastState);

        s.removeEdge();
        Transition nopin = this.tf.createNop(s.getSource(), firstState);
        System.out.println("Arxi tou inlining me: "+nopin+" apo "+s.getSource()+" sto "+firstState);
        s.getSource().addOutEdge(nopin);        
        Transition nopout = this.tf.createNop(lastState, s.getDest());
        System.out.println("Telos tou inlining me: "+nopout+" apo "+lastState+" sto "+s.getDest());
        lastState.addOutEdge(nopout);
        lastState.setEnd(false);
        caller.refreshStates();
        caller.resetTransitions();
        this.hasWork = true;
    }

    public void inline(Procedure caller, Procedure callee, MethodCall m){
        System.out.println("Inlining "+callee.getName());
        ArrayList<State> calleeStates = new ArrayList<State>();

        for(State calleeState : callee.getStates()){
            // calleeState = renameVars(calleeState, callee, null);
            // System.out.println("changing state "+calleeState.toString());
            calleeState.setProcedure(caller);
            calleeStates.add(calleeState);
        }
        List<State> calleeStatesList = new ArrayList<State>(calleeStates);
        Collections.sort(calleeStatesList, new Comparator<State>(){
            public int compare(State o1, State o2){
                return o1.toString().compareTo(o2.toString());
            }
        });

        State firstState = callee.getBegin();
        firstState.setBegin(false);
        State lastState = callee.getEnd();
        lastState.setEnd(false);
        // System.out.println("first: "+firstState);
        // System.out.println("last: "+lastState);

        m.removeEdge();
        Transition nopin = this.tf.createNop(m.getSource(), firstState);

        m.getSource().addOutEdge(nopin);
        Transition nopout = this.tf.createNop(lastState, m.getDest());
        lastState.addOutEdge(nopout);
        lastState.setEnd(false);
        System.out.println("Caller States After Inlining: "+caller.getStates());
        caller.refreshStates();
        this.hasWork = true;
    }

    public boolean visit(Procedure s){
        reachedEnd = false;
        currProc = s;
        return true;
    }

    public boolean visit(Assignment s){
        if(currProc.getName().equals("main"))
            System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
        // System.out.println("original Destination: "+s.getDest());
        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            Procedure caller = s.getDest().getMethod();
            Procedure callee = cu.getProcedure(mc.getName());
            if(callee.getName().equals("main"))return true;
            if(methodsToInline.contains(callee) && !alreadyInlinedAS.contains(s)){
                System.out.println("===== Inlining: caller: "+caller.getName()+" callee: "+callee.getName());
                inline(caller, callee, s);
                alreadyInlinedAS.add(s);
            }
        }
        return true;
    }

    public boolean visit(MethodCall m){
        // method calls need special attention; in this case, we just
        // continue with analysing the next state and triggering the analysis
        // of the callee
        // System.out.println("Visiting: MethodCall of: "+m.getCallExpression().getName());
        // System.out.println("original Destination: "+m.getDest());

        Procedure caller = m.getDest().getMethod();
        Procedure callee = cu.getProcedure(m.getCallExpression().getName());
        if(callee.getName().equals("main"))return true;
        if(methodsToInline.contains(callee) && !alreadyInlinedMC.contains(m)){
            // System.out.println("===== Inlining: caller: "+caller.getName()+" callee: "+callee.getName());
            // State s = initializeLocalVars(callee, m.getSource());
            inline(caller, callee, m);
            alreadyInlinedMC.add(m);
        }
        return true;
    }

    public boolean visit(State s){
        if(reachedEnd)return false;
        System.out.println("Visiting state: "+s);
        if(s.isEnd()){
            System.out.println("It is an End and setting reachedEnd=true");
            reachedEnd = true;
            return false;
        }
        return true;
    }

}
