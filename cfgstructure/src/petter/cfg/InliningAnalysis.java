package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.RenamingVisitor;


public class InliningAnalysis extends AbstractVisitor{

    // static HashSet<Integer> lub(HashSet<Integer> b1, HashSet<Integer> b2){
    //     if (b1==null) return b2;
    //     if (b2==null) return b1;
    //     HashSet<Integer> theunion = new HashSet<Integer>();
    //     theunion.addAll(b1);
    //     theunion.addAll(b2);
    //     return theunion;
    // }

    CompilationUnit cu;
    ArrayList<String> conditions;
    TransitionFactory tf;
    ArrayList<Procedure> methodsToInline;
    public InliningAnalysis(CompilationUnit cu, ArrayList<Procedure> methodsToInline){
        super(true); // forward reachability
        this.cu=cu;
        this.conditions = new ArrayList<String>();
        this.tf = new TransitionFactory();
        this.methodsToInline = methodsToInline;
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
                else{
                    Variable lhs = assignment.getLhs();
                    lhs.accept(new RenamingVisitor(p));
                    assignment.setLhs(lhs);

                    Expression rhs = assignment.getRhs();
                    rhs.accept(new RenamingVisitor(p));
                    assignment.setRhs(rhs);
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

    public void inline(Procedure caller, Procedure callee, Assignment s){
        System.out.println("Inlining "+callee.getName());
        Variable toReturn = s.getLhs();
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
                return o1.toString().compareTo(o2.toString());
            }
        });


        System.out.println("Callee States: "+calleeStatesList);
        State firstState = calleeStatesList.get(0);
        State lastState = calleeStatesList.get(calleeStatesList.size()-1);
        System.out.println("first: "+firstState);
        System.out.println("last: "+lastState);

        s.removeEdge();
        Transition nopin = this.tf.createNop(s.getSource(), firstState);
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
            calleeState = renameVars(calleeState, callee, null);
            System.out.println("changing state "+calleeState.toString());
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
        caller.refreshStates();
    }

    // public HashSet<Integer> visit(Procedure s){
    //     // System.out.println("Visiting Procedure: "+s.getName());
    //     if(b == null) b = new HashSet<Integer>();
    //     return b;
    // }


    // public HashSet<Integer> visit(GuardedTransition s, HashSet<Integer> b){
    //     // System.out.println("Visiting: if with guard: "+s.getAssertion());
    //     // System.out.println("b: "+s.getOperator());
    //     return b;
    // }


    public boolean visit(Assignment s){
        // System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
        // System.out.println("original Destination: "+s.getDest());
        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            Procedure caller = s.getDest().getMethod();
            Procedure callee = cu.getProcedure(mc.getName());
            System.out.println("caller: "+caller+" callee: "+callee);
            if(methodsToInline.contains(callee)){
                inline(caller, callee, s);
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
        if(methodsToInline.contains(callee)){
            inline(caller, callee, m);
        }
        return true;
    }

    // public HashSet<Integer> visit(State s, HashSet<Integer> newflow){
    //     // System.out.println("Visiting state:"+ s.toString());
    //     HashSet<Integer> oldflow = dataflowOf(s);
    //     newflow = new HashSet<Integer>();
    //     HashSet<Integer> newval = lub(oldflow, newflow);
    //     dataflowOf(s, newval);
    //     return newval;
    // }
}
