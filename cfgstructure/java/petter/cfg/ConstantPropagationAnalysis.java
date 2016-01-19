package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.Expression;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.ConstantFindingVisitor ;


public class ConstantPropagationAnalysis extends AbstractPropagatingVisitor<HashMap<Variable, IntegerConstant>>{

    static HashMap<Variable, IntegerConstant> lub(HashMap<Variable, IntegerConstant> b1, HashMap<Variable, IntegerConstant> b2){
        if (b1==null) return b2;
        if (b2==null) return b1;
        HashMap<Variable, IntegerConstant> theunion = new HashMap<Variable, IntegerConstant>(b1);
        theunion.putAll(b2);
        return theunion;
    }

    // static HashMap<Variable, IntegerConstant> lessoreq(HashMap<Variable, IntegerConstant> b1, HashMap<Variable, IntegerConstant> b2){
    //     if (b1==null) return new HashMap<Variable, IntegerConstant>();
    //     if (b2==null) return null;
    //     // return ((!b1) || b2);
    //     return null;
    // }

    // public void enter(Procedure called, HashMap<Variable, IntegerConstant> initial){

    // }

    // public void combine(HashMap<Variable, IntegerConstant> initial, HashMap<Variable, IntegerConstant> result){

    // }    

    CompilationUnit cu;
    TransitionFactory tf;
    Integer stateCounter;
    Procedure currProc;
    HashMap<Procedure, HashMap<Variable, IntegerConstant>> propagateProcs;
    public ConstantPropagationAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.stateCounter = 1;
        this.propagateProcs = new HashMap<Procedure, HashMap<Variable, IntegerConstant>>();
    }

    public HashMap<Variable, IntegerConstant> visit(Procedure s, HashMap<Variable, IntegerConstant> b){
        System.out.println("Visiting Procedure: "+s.getName());
        System.out.println("Current state: "+b);
        System.out.println("local vars:" + s.getLocalVariables());
        
        if(b == null) b = new HashMap<Variable, IntegerConstant>();
        return b;
    }

    public HashMap<Variable, IntegerConstant> visit(Assignment s, HashMap<Variable, IntegerConstant> b){
        System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
        System.out.println("Current state: "+b);
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        Expression rhs = s.getRhs();
        rhs.accept(cfv);
        System.out.println(cfv.getConstants());
        Iterator<IntegerConstant> iter = cfv.getConstants().iterator();
        if(!cfv.getConstants().isEmpty()){
            if(cfv.getConstants().size() == 1){
                b.put(s.getLhs(), iter.next());
            }
            else{
                System.out.println("constants: " + cfv.getConstants());
                System.out.println("b: " + b);
                int result = 1;
                while(iter.hasNext()){
                    result *= iter.next().getIntegerConst();
                }
                b.put(s.getLhs(), new IntegerConstant(result));
                System.out.println("new b: " + b);
            }   
        }
        else{
            if(s.getRhs().hasMethodCall()){
                petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
                Procedure called = this.cu.getProcedure(mc.getName());
                HashMap<Variable, IntegerConstant> currentConstants = new HashMap<Variable, IntegerConstant>();
                currentConstants.putAll(b);
                System.out.println("Saving state of constants: "+currentConstants);
                this.propagateProcs.put(called, currentConstants);
            }
            else if(s.getRhs() instanceof Variable){
                Variable y = (Variable) s.getRhs();
                if(b.get(y) != null){
                    b.put(s.getLhs(), b.get(y));
                }
            }
            else if(b.get(s.getLhs()) != null)
                b.remove(s.getLhs());
        }
        return b;
    }

    public HashMap<Variable, IntegerConstant> visit(GuardedTransition s, HashMap<Variable, IntegerConstant> b){
        System.out.println("Visiting if: "+s.toString());
        System.out.println("Current state: "+b);
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        Expression assertion = s.getAssertion();
        assertion.accept(cfv);
        System.out.println(cfv.getConstants());
        // Iterator<IntegerConstant> iter = cfv.getConstants().iterator();
        // while()
        // if(!cfv.getConstants().isEmpty()){
        //     if(cfv.getConstants().size() == 2){
        //         b.put(s.getLhs(), )
        //     }
        //     else{
        //     }
        // }
        return b;
    }

    public HashMap<Variable, IntegerConstant> visit(MethodCall s, HashMap<Variable, IntegerConstant> b){
        System.out.println("Visiting MethodCall:"+ s.toString());
        System.out.println("Current state: "+b);

        Procedure called = cu.getProcedure(s.getCallExpression().getName());
        HashMap<Variable, IntegerConstant> currentConstants = new HashMap<Variable, IntegerConstant>();
        currentConstants.putAll(b);
        System.out.println("Saving state of constants: "+currentConstants);
        this.propagateProcs.put(called, currentConstants);
        return b;
    }
    
    public HashMap<Variable, IntegerConstant> visit(State s, HashMap<Variable, IntegerConstant> newflow){
        System.out.println("Visiting "+ s.toString());
        System.out.println("Current state: "+newflow);
        HashMap<Variable, IntegerConstant> oldflow = dataflowOf(s);
        if(newflow == null)
            newflow = new HashMap<Variable, IntegerConstant>();
        HashMap<Variable, IntegerConstant> newval = lub(oldflow, newflow);
        dataflowOf(s, newval);
        // for(State calleeState : this.currProc.getStates()){
        //     System.out.println("----- Assigned "+calleeState+" has flow: "+dataflowOf(calleeState));
        // }
        return newval;
    }
}
