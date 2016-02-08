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
    private ArrayList<State> visited;
    private boolean reachedEnd;
    public boolean fixedPoint;
    Procedure currProc;

    public InliningAnalysis(CompilationUnit cu, ArrayList<Procedure> methodsToInline, HashMap<Procedure, HashMap<Integer, Variable>> procVarMap){
        super(true); // forward reachability
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.methodsToInline = methodsToInline;
        methodsToInline.remove(cu.getProcedure("main"));
        methodsToInline.remove(cu.getProcedure("$init"));
        this.procVarMap = procVarMap;
        this.alreadyInlinedAS = new ArrayList<Assignment>();
        this.alreadyInlinedMC = new ArrayList<MethodCall>();
        this.visited = new ArrayList<State>();
    }

    // method to clone all edges of inlined procedure to the caller
    public Transition clone(Transition t, State newSource, State newDest){
        if(t instanceof Assignment){
            Assignment tAssignment = (Assignment) t;
            return this.tf.createAssignment(newSource, newDest, tAssignment.getLhs(), tAssignment.getRhs());
        }
        else if(t instanceof MethodCall){
            MethodCall tMethodCall = (MethodCall) t;
            return this.tf.createMethodCall(newSource, newDest, tMethodCall.getCallExpression());
        }
        else if(t instanceof Nop){
            Nop tNop = (Nop) t;
            return this.tf.createNop(newSource, newDest);
        }
        else if(t instanceof GuardedTransition){
            GuardedTransition tGuard = (GuardedTransition) t;
            return this.tf.createGuard(newSource, newDest, tGuard.getAssertion(), tGuard.getOperator());
        }
        else return null;
    }

    // for all local variables that are inlined rename them to __Procedure.getName()_+Variable.toString()
    // to avoid clashes
    // Swap return statements with the variable getting the result in case of MethodCall in Assignment
    public State renameVars(State os, Procedure p, Variable toReturn){
        Iterator<Transition> outEdges = os.getOutIterator();
        Assignment assignment = null;
        boolean addNew = false;
        while(outEdges.hasNext()){
            Transition outEdge = outEdges.next();
            if(outEdge instanceof Assignment){
                assignment = (Assignment) outEdge;
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
                GuardedTransition gt = (GuardedTransition) outEdge;
                gt.getAssertion().accept(new RenamingVisitor(p));
            }
        }
        if(addNew){
            os.addOutEdge(assignment);
        }
        return os;
    }

    // create assignments in procedure start to assign to the formal parameters the actual arguments
    public void initializeFormalParams(Procedure callee, Assignment s) {
        petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
        List<Integer> formalArgs = callee.getFormalParameters();
        List<Expression> actualArgs = mc.getParamsUnchanged();
        State temp;
        State oldbegin = null;
        for(int i = 0; i < formalArgs.size(); i++) {
            int id = formalArgs.get(i);
            oldbegin = callee.getBegin();
            temp = new State();
            Transition newFormalInit = tf.createAssignment(temp, oldbegin, procVarMap.get(callee).get(id), actualArgs.get(i));
            oldbegin.addInEdge(newFormalInit);
            callee.setBegin(temp);
            callee.refreshStates();
            callee.setInitFormals();
            procVarMap.get(callee).remove(id);
        }
        // runs getTransitions() in Procedure
        callee.resetTransitions();
    }

    public void initializeLocalVars(Procedure callee, Assignment s) {
        if(callee.getInitializesLocals()){
            if(!callee.getInitializesFormals()){
                if(!callee.getFormalParameters().isEmpty()) {
                    initializeFormalParams(callee, s);
                }
            }
            return;
        }

        if(!callee.getFormalParameters().isEmpty()) {
            initializeFormalParams(callee, s);
        }

        int size = procVarMap.get(callee).size(); // without the formal params
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
        callee.setInitLocals();
    }

    // Inline procedure from assignment
    public void inline(Procedure caller, Procedure callee, Assignment s){
        initializeLocalVars(callee, s);
        // System.out.println("Assignment Inlining: "+callee.getName());
        Variable toReturn = (Variable) s.getLhs();
        ArrayList<State> calleeStates = new ArrayList<State>();
        petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();

        State firstState = null;
        State lastState = null;
        State calleeStateCopy = null;

        HashMap<State, State> stateMap = new HashMap<State, State>();
        for(State calleeState : callee.getStates()){
            calleeStateCopy = new State();
            if(calleeState.isBegin()){
                firstState = calleeStateCopy;
                firstState.setBegin(false);
            }
            else if(calleeState.isEnd()){
                lastState = calleeStateCopy;
                lastState.setEnd(false);
            }
            stateMap.put(calleeState, calleeStateCopy);
        }

        for(State calleeState : callee.getStates()){
            for(Transition t : calleeState.getOut()){
                calleeStateCopy = stateMap.get(calleeState);
                State newDest = stateMap.get(t.getDest());
                Transition tClone = clone(t, calleeStateCopy, newDest);
                calleeStateCopy.addOutEdge(tClone);
            }

            calleeStateCopy = renameVars(calleeStateCopy, callee, toReturn);
            calleeStateCopy.setProcedure(caller);
            calleeStates.add(calleeStateCopy);
        }

        s.removeEdge();
        Transition nopin = this.tf.createNop(s.getSource(), firstState);
        s.getSource().addOutEdge(nopin);        
        Transition nopout = this.tf.createNop(lastState, s.getDest());
        lastState.addOutEdge(nopout);
        caller.refreshStates();
        caller.resetTransitions();
        // we inlined a procedure so run another iteration to check for more
        this.fixedPoint = false;
    }

    // in new version of framework this is not used -> all methodcalls are assignments
    public void inline(Procedure caller, Procedure callee, MethodCall m){
        ArrayList<State> calleeStates = new ArrayList<State>();

        for(State calleeState : callee.getStates()){
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

        m.removeEdge();
        Transition nopin = this.tf.createNop(m.getSource(), firstState);

        m.getSource().addOutEdge(nopin);
        Transition nopout = this.tf.createNop(lastState, m.getDest());
        lastState.addOutEdge(nopout);
        lastState.setEnd(false);
        caller.refreshStates();
        this.fixedPoint = false;
    }

    public boolean visit(Procedure s){
        this.fixedPoint = true;
        currProc = s;
        this.visited.clear();
        return true;
    }

    public boolean visit(Assignment s){
        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            Procedure caller = s.getDest().getMethod();
            Procedure callee = cu.getProcedure(mc.getName());
            if(callee.getName().equals("main"))return true;
            // inline all procedures given at instantiation according to the 3 strategies
            if(methodsToInline.contains(callee) && !alreadyInlinedAS.contains(s) && !callee.getName().equals(caller.getName())){
                inline(caller, callee, s);
                alreadyInlinedAS.add(s);
            }
        }
        return true;
    }

    public boolean visit(MethodCall m){
        Procedure caller = m.getDest().getMethod();
        Procedure callee = cu.getProcedure(m.getCallExpression().getName());
        if(callee.getName().equals("main"))return true;
        if(methodsToInline.contains(callee) && !alreadyInlinedMC.contains(m) && !callee.getName().equals(caller.getName())){
            inline(caller, callee, m);
            alreadyInlinedMC.add(m);
        }
        return true;
    }

    public boolean visit(State s){
        if(visited.contains(s))return false;
        visited.add(s);
        return true;
    }

}
