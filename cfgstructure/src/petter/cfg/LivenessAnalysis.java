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


public class LivenessAnalysis extends AbstractPropagatingVisitor<HashSet<Variable>>{

    static HashMap<String, HashMap<Variable, IntegerConstant>> lub(HashMap<String, HashMap<Variable, IntegerConstant>> b1, HashMap<String, HashMap<Variable, IntegerConstant>> b2){
        if (b1==null)return b2;   
        if (b2==null)return b1;
        HashMap<Variable, IntegerConstant> locals1 = b1.get("local");
        HashMap<Variable, IntegerConstant> locals2 = b2.get("local");
        HashMap<Variable, IntegerConstant> globals1 = b1.get("global");
        HashMap<Variable, IntegerConstant> globals2 = b2.get("global");

        HashMap<String, HashMap<Variable, IntegerConstant>> theunion = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        theunion.put("local", new HashMap<Variable, IntegerConstant>());
        theunion.put("global", new HashMap<Variable, IntegerConstant>());

        for(Variable localVar : locals1.keySet()){
            if(locals1.get(localVar).equals(locals2.get(localVar)))
                theunion.get("local").put(localVar, locals1.get(localVar));
        }
        for(Variable globalVar : globals1.keySet()){
            if(globals1.get(globalVar).equals(globals2.get(globalVar)))
                theunion.get("global").put(globalVar, globals1.get(globalVar));
        }
        return theunion;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> deepCopy(HashMap<String, HashMap<Variable, IntegerConstant>> b){
        HashMap<String, HashMap<Variable, IntegerConstant>> newb = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        newb.put("local", new HashMap<Variable, IntegerConstant>());
        newb.put("global", new HashMap<Variable, IntegerConstant>());

        for(Variable localVar : b.get("local").keySet()){
            newb.get("local").put(localVar, b.get("local").get(localVar));
        }
        for(Variable globalVar : b.get("global").keySet()){
            newb.get("global").put(globalVar, b.get("global").get(globalVar));
        }
        return newb;
    }


    public boolean isLocal(Variable var){
        return this.currProc.getLocalVariables().contains(var.getId());
    }

    public boolean isFormal(Variable var){
        if(formalParams != null){
            return this.currProc.getFormalParameters().contains(var.getId());
        }
        return false;
    }

    public void setDataflow(Transition s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        if(dataflowOf(s.getDest()) != null){
            if(b.equals(dataflowOf(s.getDest()))){
                b = null;
                return;
            }
            HashMap<String, HashMap<Variable, IntegerConstant>> newflow = lub(b, dataflowOf(s.getDest()));
            if(!newflow.equals(dataflowOf(s.getDest()))){
                fixedPoint = false;
            }
            dataflowOf(s.getDest(), newflow);
        }
        else{
            dataflowOf(s.getDest(), b);
        }
    }


    public LivenessAnalysis setupVisitor(HashMap<String, HashMap<Variable, IntegerConstant>> b,
                                                    petter.cfg.expression.MethodCall mc){
        LivenessAnalysis cfv = new LivenessAnalysis(b);
        cfv.setProc(currProc);
        HashMap<Integer, IntegerConstant> parameterVals = new HashMap<Integer, IntegerConstant>();
        List<Expression> actualParams = mc.getParamsUnchanged();

        for(Integer i=0; i<actualParams.size(); i++){
            Expression actual = actualParams.get(i);
            if(actual instanceof IntegerConstant){
                parameterVals.put(i, (IntegerConstant) actual);
            }
            else if(actual instanceof Variable){
                Variable actualV = (Variable) actual;
                if(b.get("local").get(actualV) != null){
                    parameterVals.put(i, b.get("local").get(actualV));
                }
                else if(b.get("global").get(actualV) != null){
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
        LivenessAnalysis interproc = new LivenessAnalysis(this.cu);
        // System.out.println("Will enter func with parameterVals:"+parameterVals);
        if(!parameterVals.keySet().isEmpty()){
            interproc.setFormalParameters(parameterVals);
        }
        return interproc;
    }

    public void _enter(ConstantPropagationAnalysis interproc, Procedure called, HashMap<String, HashMap<Variable, IntegerConstant>> initial){
        HashMap<String, HashMap<Variable, IntegerConstant>> propagated = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        // Moving the locals to globals
        propagated.put("local", new HashMap<Variable, IntegerConstant>());
        propagated.put("global", new HashMap<Variable, IntegerConstant>());
        propagated.get("global").putAll(initial.get("global"));
        interproc.enter(called, propagated);
        interproc.fullAnalysis();
    }

    public void _combine(HashMap<String, HashMap<Variable, IntegerConstant>> initial, 
                         HashMap<String, HashMap<Variable, IntegerConstant>> result,
                         String name, Variable assignee){
        // System.out.println("Initial map: "+initial);
        // System.out.println("Result map: "+result);
        IntegerConstant returnValue;
        for(Variable var : result.get("global").keySet()){
            if(var.toString() == "return"){
                returnValue = result.get("global").get(var);
                if(assignee != null){ //&& !assignee.toString().startsWith("$")){
                    initial.get("local").put(assignee, result.get("global").get(var));
                }
                result.get("global").remove(var);
                break;
            }
        }
        // result.get("global").keySet().removeAll(initial.get("local").keySet());
        initial.get("global").clear();
        initial.get("global").putAll(result.get("global"));
    }

    public void setWorklist(ArrayList<Procedure> worklist){
    	this.worklist = worklist;
    }

    public void setFormalParameters(HashMap<Integer, IntegerConstant> formals){
        this.formalParams = formals;
    }

    CompilationUnit cu;
    TransitionFactory tf;
    Procedure currProc;
    ArrayList<Procedure> worklist;
    HashMap<Integer, IntegerConstant> formalParams;
    HashSet<Variable> liveVariables;
    ArrayList<State> visited;
    boolean fixedPoint;
    public LivenessAnalysis(CompilationUnit cu){
        super(false); // forward analysis
        this.cu=cu;
        this.visited = new ArrayList<State>();
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Procedure s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        this.currProc = s;
        this.visited.clear();
        fixedPoint = true;
        System.out.println("--------------------- Visiting Procedure: "+s.getName()+" ---------------------");
        System.out.println("Current result: "+dataflowOf(s.getEnd())+" formals:"+this.formalParams);
        if(b == null){
            b = new HashMap<String, HashMap<Variable, IntegerConstant>>();
            b.put("local", new HashMap<Variable, IntegerConstant>());
            b.put("global", new HashMap<Variable, IntegerConstant>());
        }
        dataflowOf(s.getBegin(), b);
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Nop s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // if(s.getSource() != null){
        b = deepCopy(dataflowOf(s.getSource()));
        // }
        // else{
        //     if(b == null){
        //         b = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        //         b.put("local", new HashMap<Variable, IntegerConstant>());
        //         b.put("global", new HashMap<Variable, IntegerConstant>());
        //     }
        // }
        setDataflow(s, b);
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Assignment s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // System.out.println("Visiting assignment: "+s);
        // System.out.println("Current state: "+b);
        b = deepCopy(dataflowOf(s.getSource()));
     
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        cfv.setFormals(formalParams);
        cfv.setProc(currProc);
        Variable x = (Variable) s.getLhs();
        Expression y = s.getRhs();

        if(y instanceof IntegerConstant){
            addConstant(x, (IntegerConstant) y, b);
        }
        else if(y instanceof Variable){
            addConstant(x, (Variable) y, b);
        }
        else if(y instanceof UnaryExpression ||
                y instanceof BinaryExpression){
            y.accept(cfv);
            if(cfv.getConstant() != null){
                addConstant(x, cfv.getConstant(), b);
            }
        }
        else if(y.hasMethodCall()){
            // System.out.println("Assignment has methodcall coming from: "+s.getSource());
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) y;
            Procedure called = this.cu.getProcedure(mc.getName());
            ConstantPropagationAnalysis interproc = setupVisitor(b, mc);
            _enter(interproc, called, b);
            _combine(b, interproc.dataflowOf(called.getEnd()), called.getName(), x);
        }
        else{
            removeConstant(x, b);
        }
        setDataflow(s, b);
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(GuardedTransition s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // System.out.println("Visiting if condition: "+s);
        b = dataflowOf(s.getSource());
        setDataflow(s, b);
        // System.out.println("constant in condition:"+cfv.getConstant());
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(MethodCall s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // System.out.println("In methodcall starting from: "+s.getSource());
        petter.cfg.expression.MethodCall mc = s.getCallExpression();
        Procedure called = this.cu.getProcedure(mc.getName());
        ConstantPropagationAnalysis interproc = setupVisitor(b, mc);
        b = deepCopy(dataflowOf(s.getSource()));
        _enter(interproc, called, b);
        _combine(b, interproc.dataflowOf(called.getEnd()), called.getName(), null);
        setDataflow(s, b);
        return b;
    }
    
    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(State s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // System.out.println("Visiting "+ s.toString());
        // System.out.println("Current state: "+dataflowOf(s));
        if(visited.contains(s))return null;
        visited.add(s);
        return b;
    }
}
