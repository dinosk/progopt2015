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

    public InliningAnalysis(CompilationUnit cu, ArrayList<Procedure> methodsToInline, HashMap<Procedure, HashMap<Integer, Variable>> procVarMap){
        super(true); // forward reachability
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.methodsToInline = methodsToInline;
        this.procVarMap = procVarMap;
        this.alreadyInlinedAS = new ArrayList<Assignment>();
        this.alreadyInlinedMC = new ArrayList<MethodCall>();
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
        int size = procVarMap.get(callee).size();
        System.out.println("size of locals:"+procVarMap.get(callee));
        State temp;
        State oldbegin = null;
        for(int id : procVarMap.get(callee).keySet()){
            oldbegin = callee.getBegin();
            temp = new State();
            // temp.setProcedure(callee);
            Transition newLocalInit = tf.createAssignment(temp, oldbegin, procVarMap.get(callee).get(id), new IntegerConstant(0));
            System.out.println("adding assignment:"+newLocalInit+" from "+temp+" to "+oldbegin);
            // temp.addOutEdge(newLocalInit);
            oldbegin.addInEdge(newLocalInit);
            callee.setBegin(temp);
            callee.refreshStates();
            System.out.println("States with initialization: "+callee.getStates());
            System.out.println("New Begin: "+callee.getBegin());
        }
        Iterator<Transition> beginIter = callee.getBegin().getOut().iterator();
        while(beginIter.hasNext()){
            Transition outedge = beginIter.next();
            System.out.println("epomeno out edge: "+outedge+" to dest: "+outedge.getDest());
        }
        // System.out.println()
    }

    public void inline(Procedure caller, Procedure callee, Assignment s){
        initializeLocalVars(callee);
        if(caller.getName().equals("$init")){
            caller = this.cu.getProcedure("main");
        }
        System.out.println("Assignment Inlining: "+callee.getName());
        Variable toReturn = (Variable) s.getLhs();
        ArrayList<State> calleeStates = new ArrayList<State>();
        petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
        // for(State mainState : caller.getStates()){
            // System.out.println("States in main called:"+mainState.getMethod());
        // }
        for(State calleeState : callee.getStates()){
            // calleeState = renameVars(calleeState, callee, toReturn);
            // System.out.println("changing state "+calleeState.toString());
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
        State firstState = calleeStatesList.get(0);
        State lastState = calleeStatesList.get(calleeStatesList.size()-1);
        // System.out.println("first: "+firstState);
        // System.out.println("last: "+lastState);
        System.out.println("~BEFORE:");
        Iterator<Transition> sourceOut = s.getSource().getOut().iterator();
        while(sourceOut.hasNext()){
            Transition outEdge = sourceOut.next();
            System.out.println("source:"+ s.getSource() +" has outedge:"+outEdge+" going to: "+outEdge.getDest());
        }

        s.removeEdge();
        Transition nopin = this.tf.createNop(s.getSource(), firstState);
        s.getSource().addOutEdge(nopin);
        System.out.println("inlining source: "+s.getSource());
        System.out.println("~AFTER:");
        sourceOut = s.getSource().getOut().iterator();
        while(sourceOut.hasNext()){
            Transition outEdge = sourceOut.next();
            System.out.println("source:"+ s.getSource() +" has outedge:"+outEdge+" going to: "+outEdge.getDest());
        }

        System.out.println("~BEFORE:");
        sourceOut = s.getDest().getOut().iterator();
        while(sourceOut.hasNext()){
            Transition outEdge = sourceOut.next();
            System.out.println("source:"+ s.getDest() +" has outedge:"+outEdge+" going to: "+outEdge.getDest());
        }
        
        Transition nopout = this.tf.createNop(lastState, s.getDest());
        lastState.addOutEdge(nopout);

        System.out.println("~AFTER:");
        sourceOut = s.getDest().getOut().iterator();
        while(sourceOut.hasNext()){
            Transition outEdge = sourceOut.next();
            System.out.println("source:"+ s.getDest() +" has outedge:"+outEdge+" going to: "+outEdge.getDest());
        }

        lastState.setEnd(false);
        caller.refreshStates();
        // System.out.println("Caller States After Inlining: "+caller.getStates());
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

        State firstState = calleeStatesList .get(0);
        State lastState = calleeStatesList.get(calleeStates.size()-1);
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
    }

    public boolean visit(Assignment s){
        // System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
        // System.out.println("original Destination: "+s.getDest());
        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            Procedure caller = s.getDest().getMethod();
            Procedure callee = cu.getProcedure(mc.getName());
            if(callee.getName().equals("main"))return true;
            if(methodsToInline.contains(callee) && !alreadyInlinedAS.contains(s)){
                System.out.println("===== Inlining: caller: "+caller.getName()+" callee: "+callee.getName());
                initializeLocalVars(callee);
                // inline(caller, callee, s);
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
            // inline(caller, callee, m);
            alreadyInlinedMC.add(m);
        }
        return true;
    }

}
