package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.Expression;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.ConstantFindingVisitor ;


public class ConstantPropagationAnalysis extends AbstractPropagatingVisitor<HashMap<String, HashMap<Variable, IntegerConstant>>>{

    static HashMap<String, HashMap<Variable, IntegerConstant>> lub(HashMap<String, HashMap<Variable, IntegerConstant>> b1, HashMap<String, HashMap<Variable, IntegerConstant>> b2){
        if (b1==null) return b2;
        if (b2==null) return b1;
        HashMap<Variable, IntegerConstant> locals1 = b1.get("local");
        HashMap<Variable, IntegerConstant> locals2 = b2.get("local");

        HashMap<Variable, IntegerConstant> globals1 = b1.get("global");
        HashMap<Variable, IntegerConstant> globals2 = b2.get("global");

        HashMap<String, HashMap<Variable, IntegerConstant>> theunion = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        theunion.put("local", new HashMap<Variable, IntegerConstant>());
        theunion.put("global", new HashMap<Variable, IntegerConstant>());

        for(Variable v1 : locals1.keySet())
            if(locals1.get(v1) == locals2.get(v1))
                theunion.get("local").put(v1, locals1.get(v1));
        for(Variable v2 : globals1.keySet())
            if(globals1.get(v2) == globals2.get(v2))
                theunion.get("global").put(v2, globals1.get(v2));

        return theunion;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> deepCopy(HashMap<String, HashMap<Variable, IntegerConstant>> a){
        HashMap<String, HashMap<Variable, IntegerConstant>> b = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        for(String firstLevel : a.keySet()){
            b.put(firstLevel, new HashMap<Variable, IntegerConstant>());
            for(Variable secondLevel : a.get(firstLevel).keySet()){
                b.get(firstLevel).put(secondLevel, a.get(firstLevel).get(secondLevel));
            }
        }
        return b;
    }

    public void _enter(ConstantPropagationAnalysis interproc, Procedure called, HashMap<String, HashMap<Variable, IntegerConstant>> initial){
        HashMap<String, HashMap<Variable, IntegerConstant>> propagated = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        // Moving the locals to globals. Must make sure this is correct
        propagated.put("local", new HashMap<Variable, IntegerConstant>());
        propagated.put("global", new HashMap<Variable, IntegerConstant>());
        propagated.get("global").putAll(initial.get("global"));
        interproc.enter(called, propagated);
        interproc.fullAnalysis();
    }

    public boolean isLocal(Variable var){
        return this.currProc.getLocalVariables().contains(var.getId());
    }

    public void _combine(HashMap<String, HashMap<Variable, IntegerConstant>> initial, HashMap<String, HashMap<Variable, IntegerConstant>> result, String name){
        System.out.println("Initial map: "+initial);
        System.out.println("Result map: "+result);
        for(Variable var : result.get("global").keySet()){
            if(var.toString() == "return"){
                var.putAnnotation("external name", name+"()");
                initial.get("local").put(var, result.get("global").get(var));
                result.get("global").remove(var);
                break;
            }
        }
        result.get("global").keySet().removeAll(initial.get("local").keySet());
        initial.get("global").putAll(result.get("global"));
    }

    CompilationUnit cu;
    TransitionFactory tf;
    Integer stateCounter;
    Procedure currProc;
    public ConstantPropagationAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.stateCounter = 1;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Procedure s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        System.out.println("Visiting Procedure: "+s.getName());
        System.out.println("Current state: "+b);
        System.out.println("local vars:" + s.getLocalVariables());
        this.currProc = s;
        if(b == null){
            b = new HashMap<String, HashMap<Variable, IntegerConstant>>();
            b.put("local", new HashMap<Variable, IntegerConstant>());
            b.put("global", new HashMap<Variable, IntegerConstant>());
        }
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Assignment s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
        System.out.println("Current state: "+b);
        Expression y = s.getRhs();
        Variable x = s.getLhs();

        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        y.accept(cfv);
        System.out.println(cfv.getConstants());
        Iterator<IntegerConstant> iter = cfv.getConstants().iterator();
        //redo prwta na elenxw ti einai kai meta to constant count
        if(cfv.getConstants().size() > 1 || y instanceof IntegerConstant){
            if(cfv.getConstants().size() == 1)
                if(isLocal(x))
                    b.get("local").put(x, iter.next());
                else
                    b.get("global").put(x, iter.next());
            
            else{
                System.out.println("constants: "+cfv.getConstants());
                System.out.println("b: "+b);
                int result = 1;
                while(iter.hasNext()){
                    result *= iter.next().getIntegerConst();
                }
                if(isLocal(x))
                    b.get("local").put(x, new IntegerConstant(result));
                else
                    b.get("global").put(x, new IntegerConstant(result));
                System.out.println("new b: "+b);
            }   
        }
        else{
            if(y.hasMethodCall()){
                petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) y;
                Procedure called = this.cu.getProcedure(mc.getName());
                ConstantPropagationAnalysis interproc = new ConstantPropagationAnalysis(this.cu);
                _enter(interproc, called, b);
                _combine(b, interproc.dataflowOf(called.getEnd()), called.getName());
            }
            else if(y instanceof Variable){
                Variable yy = (Variable) y;
                if(isLocal(yy)){
                    if(b.get("local").get(yy) != null){
                        if(isLocal(x))
                            b.get("local").put(x, b.get("local").get(yy));
                        else
                            b.get("global").put(x, b.get("local").get(yy));
                    }
                    else{
                        b.get("local").remove(yy);
                    }
                }
                else{
                    if(b.get("global").get(yy) != null){
                        if(isLocal(x))
                            b.get("local").put(x, b.get("global").get(yy));
                        else
                            b.get("global").put(x, b.get("global").get(yy));
                    }
                    else{
                        b.get("global").remove(yy);
                    }
                }
            }
            else if(isLocal(x)){
                if(b.get("local").get(x) != null)
                    b.get("local").remove(x);
            }

        }
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(GuardedTransition s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        System.out.println("Visiting if: "+s.toString());
        System.out.println("Current state: "+b);
        
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        s.getAssertion().accept(cfv);
        
        System.out.println(cfv.getConstants());
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(MethodCall s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        System.out.println("Visiting MethodCall:"+ s.toString());
        System.out.println("Current state: "+b);

        Procedure called = cu.getProcedure(s.getCallExpression().getName());
        ConstantPropagationAnalysis interproc = new ConstantPropagationAnalysis(this.cu);
        _enter(interproc, called, b);
        _combine(b, interproc.dataflowOf(called.getEnd()), called.getName());
        return b;
    }
    
    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(State s, HashMap<String, HashMap<Variable, IntegerConstant>> newflow){
        System.out.println("Visiting "+ s.toString());
        System.out.println("Current state: "+newflow);
        HashMap<String, HashMap<Variable, IntegerConstant>> oldflow = dataflowOf(s);
        if(newflow == null)
            newflow = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        HashMap<String, HashMap<Variable, IntegerConstant>> newval = lub(oldflow, newflow);

        dataflowOf(s, deepCopy(newval));
        return newval;
    }
}
