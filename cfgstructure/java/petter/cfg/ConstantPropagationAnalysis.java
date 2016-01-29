package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.Expression;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.ConstantFindingVisitor;


public class ConstantPropagationAnalysis extends AbstractPropagatingVisitor<HashMap<String, HashMap<Variable, IntegerConstant>>>{

    static HashMap<String, HashMap<Variable, IntegerConstant>> lub(HashMap<String, HashMap<Variable, IntegerConstant>> b1, HashMap<String, HashMap<Variable, IntegerConstant>> b2){
        // System.out.println("b1: "+b1);
        // System.out.println("b2: "+b2);
        if (b1==null){
            // System.out.println("b1 is null!");
            return b2;   
        }
        if (b2==null){
            // System.out.println("b2 is null!");
            return b1;
        }
        // ASK about the different cases
        HashMap<Variable, IntegerConstant> locals1 = b1.get("local");
        HashMap<Variable, IntegerConstant> locals2 = b2.get("local");

        HashMap<Variable, IntegerConstant> globals1 = b1.get("global");
        HashMap<Variable, IntegerConstant> globals2 = b2.get("global");

        HashMap<String, HashMap<Variable, IntegerConstant>> theunion = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        theunion.put("local", new HashMap<Variable, IntegerConstant>());
        theunion.put("global", new HashMap<Variable, IntegerConstant>());

        for(Variable v1 : locals1.keySet()){
            System.out.println("Checking if "+locals1.get(v1)+" == "+locals2.get(v1));
            if(locals1.get(v1) == locals2.get(v1))
                theunion.get("local").put(v1, locals1.get(v1));
        }
        for(Variable v2 : globals1.keySet()){
            System.out.println("Checking if "+globals1.get(v2)+" == "+globals2.get(v2));
            if(globals1.get(v2) == globals2.get(v2))
                theunion.get("global").put(v2, globals1.get(v2));
        }

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

    public boolean isFormal(Variable var){
        if(formalParams != null)
            return this.formalParams.keySet().contains(var.getId());
        return false;
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

    public HashMap<String, HashMap<Variable, IntegerConstant>> getMap(){
        return this.map;
    }

    public void setWorklist(ArrayList<Procedure> worklist){
    	this.worklist = worklist;
    }

    public void setFormalParameters(HashMap<Integer, IntegerConstant> formals){
        this.formalParams = formals;
    }

    CompilationUnit cu;
    TransitionFactory tf;
    Integer stateCounter;
    Procedure currProc;
    ArrayList<Procedure> worklist;
    HashMap<Integer, IntegerConstant> formalParams;
    HashMap<String, HashMap<Variable, IntegerConstant>> map;
    public ConstantPropagationAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.stateCounter = 1;
        this.map = null;
        this.formalParams = null;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Procedure s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        System.out.println("Visiting Procedure: "+s.getName());
        System.out.println("Current state: "+b+" formals:"+this.formalParams);
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
        // System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        cfv.setFormals(formalParams);
        System.out.println("Current state: "+b);
        Variable x = s.getLhs();
        Expression y = s.getRhs();

        if(y instanceof IntegerConstant){
            if(isLocal(x)){
                b.get("local").put(x, (IntegerConstant) y);
            }
            else{
                b.get("global").put(x, (IntegerConstant) y);
            }
        }
        else if(y instanceof Variable){
            Variable yy = (Variable) y;
            if(b.get("local").get(yy) != null){
                // #TODO: CHECK IF IT IS A FORMAL!
                if(isLocal(x)){
                    b.get("local").put(x, b.get("local").get(yy));
                }
                else{
                    b.get("global").put(x, b.get("local").get(yy));
                }
            }
            else if(b.get("global").get(yy) != null){
                if(isLocal(x)){
                    b.get("local").put(x, b.get("global").get(yy));
                }
                else{
                    b.get("global").put(x, b.get("global").get(yy));
                }
            }
            else if(isFormal(yy)){
                if(isLocal(x)){
                    b.get("local").put(x, formalParams.get(yy.getId()));
                }
                else{
                    b.get("global").put(x, formalParams.get(yy.getId()));
                }   
            }

        }
        else if(y instanceof UnaryExpression ||
               y instanceof BinaryExpression){
            y.accept(cfv);
            if(cfv.getConstant() != null){
                if(isLocal(x)){
                    b.get("local").put(x, cfv.getConstant());
                }
                else{
                    b.get("global").put(x, cfv.getConstant());
                }
            }
        }
        else if(y.hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) y;
            HashMap<Integer, IntegerConstant> parameterVals = new HashMap<Integer, IntegerConstant>();
            List<Expression> actualParams = mc.getParamsUnchanged();
            Procedure called = this.cu.getProcedure(mc.getName());
            System.out.println("EDW!");

            for(Integer i=0; i<actualParams.size(); i++){
                Expression actual = actualParams.get(i);
                // System.out.println("checking if "+actual+" is a constant");
                if(actual instanceof IntegerConstant){
                    parameterVals.put(i, (IntegerConstant) actual);
                }
                else if(actual instanceof Variable){
                    Variable actualV = (Variable) actual;
                    if(b.get("local").get(actualV) != null){
                        // System.out.println("actual local parameter:"+b.get("local").get(actualV));
                        parameterVals.put(i, b.get("local").get(actualV));
                    }
                    else if(b.get("global").get(actualV) != null){
                        // System.out.println("actual global parameter:"+b.get("global").get(actualV));
                        parameterVals.put(i, b.get("global").get(actualV));
                    }
                }
                else{
                    actual.accept(cfv);
                    if(cfv.getConstant() != null){
                        parameterVals.put(i, cfv.getConstant());   
                    }
                }
            }

            ConstantPropagationAnalysis interproc = new ConstantPropagationAnalysis(this.cu);
            System.out.println("Will enter func with parameterVals:"+parameterVals);
            if(!parameterVals.keySet().isEmpty()){
                interproc.setFormalParameters(parameterVals);
            }
            _enter(interproc, called, b);
            // ASK if this is the way to retrieve the result of the analysis
            _combine(b, interproc.dataflowOf(called.getEnd()), called.getName());
        }
        else{
            //#TODO: REMOVE IT ALSO FROM THE FORMALS!
            if(isLocal(x)){
                if(b.get("local").get(x) != null)
                    b.get("local").remove(x);
            }
            else{
                if(b.get("global").get(x) != null)
                    b.get("global").remove(x);   
            }
        }
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(GuardedTransition s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // System.out.println("Visiting if: "+s.toString());
        // System.out.println("Current state: "+b);
        
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        s.getAssertion().accept(cfv);
        
        System.out.println("constant in condition:"+cfv.getConstant());
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(MethodCall s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        petter.cfg.expression.MethodCall mc = s.getCallExpression();
        HashMap<Integer, IntegerConstant> parameterVals = new HashMap<Integer, IntegerConstant>();
        List<Expression> actualParams = mc.getParamsUnchanged();
        Procedure called = this.cu.getProcedure(mc.getName());
        System.out.println("EDW!");

        for(Integer i=0; i<actualParams.size(); i++){
            Expression actual = actualParams.get(i);
            // System.out.println("checking if "+actual+" is a constant");
            if(actual instanceof IntegerConstant){
                parameterVals.put(i, (IntegerConstant) actual);
            }
            else if(actual instanceof Variable){
                Variable actualV = (Variable) actual;
                if(b.get("local").get(actualV) != null){
                    // System.out.println("actual local parameter:"+b.get("local").get(actualV));
                    parameterVals.put(i, b.get("local").get(actualV));
                }
                else if(b.get("global").get(actualV) != null){
                    // System.out.println("actual global parameter:"+b.get("global").get(actualV));
                    parameterVals.put(i, b.get("global").get(actualV));
                }
            }
            else{
                actual.accept(cfv);
                if(cfv.getConstant() != null){
                    parameterVals.put(i, cfv.getConstant());   
                }
            }
        }
        ConstantPropagationAnalysis interproc = new ConstantPropagationAnalysis(this.cu);
        System.out.println("Will enter func with parameterVals:"+parameterVals);
        if(!parameterVals.keySet().isEmpty()){
            interproc.setFormalParameters(parameterVals);
        }
        _enter(interproc, called, b);
        // ASK if this is the way to retrieve the result of the analysis
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
        // this.map = newval;
        return newval;
    }
}
