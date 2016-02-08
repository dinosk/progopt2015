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

    // Calculate the LUB of two dataflows [intersection]
    static HashMap<String, HashMap<Variable, IntegerConstant>> lub(HashMap<String, HashMap<Variable, IntegerConstant>> b1, HashMap<String, HashMap<Variable, IntegerConstant>> b2){

        if (b1==null)return b2;   
        if (b2==null)return b1;
        HashMap<Variable, IntegerConstant> locals1 = b1.get("local");
        HashMap<Variable, IntegerConstant> locals2 = b2.get("local");
        HashMap<Variable, IntegerConstant> globals1 = b1.get("global");
        HashMap<Variable, IntegerConstant> globals2 = b2.get("global");

        HashMap<String, HashMap<Variable, IntegerConstant>> theintersection = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        theintersection.put("local", new HashMap<Variable, IntegerConstant>());
        theintersection.put("global", new HashMap<Variable, IntegerConstant>());

        for(Variable localVar : locals1.keySet()){
            if(locals1.get(localVar).equals(locals2.get(localVar)))
                theintersection.get("local").put(localVar, locals1.get(localVar));
        }
        for(Variable globalVar : globals1.keySet()){
            if(globals1.get(globalVar).equals(globals2.get(globalVar)))
                theintersection.get("global").put(globalVar, globals1.get(globalVar));
        }
        return theintersection;
    }

    // we need to create a deepcopy of each dataflow map so that they don't interfere between states
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

    // assign a dataflow to a state. if state already is assigned a dataflow then calculate the LUB of the 2
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
            fixedPoint = false;
        }
    }

    // overloaded method to add an IntegerConstant to the map
    // this version gets the value of another variable
    public void addConstant(Variable x, Variable y, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        if(b.get("local").get(y) != null){
            addConstant(x, b.get("local").get(y), b);
        }
        else if(b.get("global").get(y) != null){
            addConstant(x, b.get("global").get(y), b);
        }
        else if(isFormal(y) && formalParams.get(this.currProc.getFormalParameters().indexOf(y.getId())) != null){
            // This thing here tries to find if we have a value for the formal parameter y
            addConstant(x, formalParams.get(this.currProc.getFormalParameters().indexOf(y.getId())), b);
        }
    }

    // this one adds an integer directly
    public void addConstant(Variable x, IntegerConstant constant, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        if(!x.toString().startsWith("$")){
            if(isLocal(x) || isFormal(x)){
                b.get("local").put(x, constant);
            }
            else{
                b.get("global").put(x, constant);
            }
        }
    }

    public void removeConstant(Variable x, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        if(b != null){
            if(isLocal(x)){
                if(b.get("local").get(x) != null)
                    b.get("local").remove(x);
            }
            else{
                if(b.get("global").get(x) != null)
                    b.get("global").remove(x);
            }
        }
        if(formalParams != null){
            if(formalParams.keySet().contains(x.getId())){
                formalParams.remove(x.getId());
            }
        }
    }

    // setting up a visitor to visit a callee procedure
    public ConstantPropagationAnalysis setupVisitor(HashMap<String, HashMap<Variable, IntegerConstant>> b,
                                                    petter.cfg.expression.MethodCall mc){
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        cfv.setProc(currProc);
        HashMap<Integer, IntegerConstant> parameterVals = new HashMap<Integer, IntegerConstant>();
        List<Expression> actualParams = mc.getParamsUnchanged();

        // pass the actual arguments in case they are assigned an IntegerConstant
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
        ConstantPropagationAnalysis interproc = new ConstantPropagationAnalysis(this.cu);
        if(!parameterVals.keySet().isEmpty()){
            interproc.setFormalParameters(parameterVals);
        }
        return interproc;
    }

    // create a new map for the initial value of the visitor
    // gets all globals of the current map
    public void _enter(ConstantPropagationAnalysis interproc, Procedure called, HashMap<String, HashMap<Variable, IntegerConstant>> initial){
        HashMap<String, HashMap<Variable, IntegerConstant>> propagated = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        // Moving the locals to globals
        propagated.put("local", new HashMap<Variable, IntegerConstant>());
        propagated.put("global", new HashMap<Variable, IntegerConstant>());
        propagated.get("global").putAll(initial.get("global"));
        interproc.enter(called, propagated);
        interproc.fullAnalysis();
    }

    // after finishing calculate the LUB of the dataflow of the final state of the callee 
    // with the one we had before the MethodCall
    public void _combine(HashMap<String, HashMap<Variable, IntegerConstant>> initial, 
                         HashMap<String, HashMap<Variable, IntegerConstant>> result,
                         String name, Variable assignee){
        IntegerConstant returnValue;
        for(Variable var : result.get("global").keySet()){
            if(var.toString() == "return"){
                returnValue = result.get("global").get(var);
                if(assignee != null){
                    initial.get("local").put(assignee, result.get("global").get(var));
                }
                result.get("global").remove(var);
                break;
            }
        }
        initial.get("global").clear();
        initial.get("global").putAll(result.get("global"));
    }

    public void setFormalParameters(HashMap<Integer, IntegerConstant> formals){
        this.formalParams = formals;
    }

    CompilationUnit cu;
    TransitionFactory tf;
    Procedure currProc;
    ArrayList<Procedure> worklist;
    HashMap<Integer, IntegerConstant> formalParams;
    ArrayList<State> visited;
    boolean fixedPoint;
    public ConstantPropagationAnalysis(CompilationUnit cu){
        super(true); // forward analysis
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.visited = new ArrayList<State>();
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Procedure s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        this.currProc = s;
        this.visited.clear();
        fixedPoint = true;
        if(b == null){
            b = new HashMap<String, HashMap<Variable, IntegerConstant>>();
            b.put("local", new HashMap<Variable, IntegerConstant>());
            b.put("global", new HashMap<Variable, IntegerConstant>());
        }
        dataflowOf(s.getBegin(), b);
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Nop s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // Just propagate the dataflow
        b = deepCopy(dataflowOf(s.getSource()));
        setDataflow(s, b);
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Assignment s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // At each edge propagate the dataflow to the next by making a new deep copy
        b = deepCopy(dataflowOf(s.getSource()));
        if(s.getLhs() instanceof Variable){
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
                // in case of MethodCall instantiate new visitor to visit the callee
                petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) y;
                Procedure called = this.cu.getProcedure(mc.getName());
                ConstantPropagationAnalysis interproc = setupVisitor(b, mc);
                _enter(interproc, called, b);
                _combine(b, interproc.dataflowOf(called.getEnd()), called.getName(), x);
            }
            else{
                removeConstant(x, b);
            }
        }
        setDataflow(s, b);
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(GuardedTransition s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        b = dataflowOf(s.getSource());
        setDataflow(s, b);
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(MethodCall s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
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
        if(s.isEnd()){
        // Creating the dotlayout right here in case of interprocedural analysis
        // -> the top visitor is not aware of the interproc.dataflowOf(any state);
            DotLayout layout = new DotLayout("jpg", currProc.getName()+"AfterConstant.jpg");
            for (State state : currProc.getStates()){
                layout.highlight(state,(dataflowOf(state))+"");
            }
            try{
                layout.callDot(currProc);
            }
            catch(Exception e){
                System.out.println(e);
            }
        }
        if(visited.contains(s))return null;
        visited.add(s);
        return b;
    }
}
