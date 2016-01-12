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

    // static HashMap<Variable, IntegerConstant> lub(HashMap<Variable, IntegerConstant> b1, HashMap<Variable, IntegerConstant> b2){
    //     if (b1==null) return b2;
    //     if (b2==null) return b1;
    //     HashMap<Variable, IntegerConstant> theunion = new HashMap<Variable, IntegerConstant>();
    //     theunion.addAll(b1);
    //     theunion.addAll(b2);
    //     return theunion;
    // }

    // static HashMap<Variable, IntegerConstant> lessoreq(HashMap<Variable, IntegerConstant> b1, HashMap<Variable, IntegerConstant> b2){
    //     if (b1==null) return new HashMap<Variable, IntegerConstant>();
    //     if (b2==null) return null;
    //     // return ((!b1) || b2);
    //     return null;
    // }

    CompilationUnit cu;
    TransitionFactory tf;
    public ConstantPropagationAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.tf = new TransitionFactory();
    }

    public HashMap<Variable, IntegerConstant> visit(Procedure s, HashMap<Variable, IntegerConstant> b){
        System.out.println("Visiting Procedure: "+s.getName());
        if(b == null) b = new HashMap<Variable, IntegerConstant>();
        return b;
    }

    public HashMap<Variable, IntegerConstant> visit(Assignment s, HashMap<Variable, IntegerConstant> b){
        System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
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
                int result = 1;
                while(iter.hasNext()){
                    result *= iter.next().getIntegerConst();
                }
                b.put(s.getLhs(), new IntegerConstant(result));
            }   
        }
        return b;
    }

    public HashMap<Variable, IntegerConstant> visit(GuardedTransition s, HashMap<Variable, IntegerConstant> b){
        System.out.println("Visiting if: "+s.toString());
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
        return b;
    }
    // public HashMap<Variable, IntegerConstant> visit(State s, HashMap<Variable, IntegerConstant> newflow){
    //     System.out.println("Visiting state:"+ s.toString());
    //     HashMap<Variable, IntegerConstant> oldflow = dataflowOf(s);
    //     newflow = new HashMap<Variable, IntegerConstant>();
    //     HashMap<Variable, IntegerConstant> newval = lub(oldflow, newflow);
    //     dataflowOf(s, newval);
    //     return newval;
    // }
}
