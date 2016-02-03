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
        System.out.println("Finding lub of b1: "+b1+" and b2: "+b2);
        if (b1==null)return b2;   
        if (b2==null)return b1;
        HashMap<Variable, IntegerConstant> locals1 = b1.get("local");
        HashMap<Variable, IntegerConstant> locals2 = b2.get("local");
        HashMap<Variable, IntegerConstant> globals1 = b1.get("global");
        HashMap<Variable, IntegerConstant> globals2 = b2.get("global");

        HashMap<String, HashMap<Variable, IntegerConstant>> theintersection = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        theintersection.put("local", new HashMap<Variable, IntegerConstant>());
        theintersection.put("global", new HashMap<Variable, IntegerConstant>());

        for(Variable v1 : locals1.keySet()){
            if(locals1.get(v1) == locals2.get(v1))
                theintersection.get("local").put(v1, locals1.get(v1));
        }
        for(Variable v2 : globals1.keySet()){
            if(globals1.get(v2) == globals2.get(v2))
                theintersection.get("global").put(v2, globals1.get(v2));
        }

        return theintersection;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> deepCopy(HashMap<String, HashMap<Variable, IntegerConstant>> b){
        HashMap<String, HashMap<Variable, IntegerConstant>> newb = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        newb.put("local", new HashMap<Variable, IntegerConstant>());
        newb.put("global", new HashMap<Variable, IntegerConstant>());

        for(Variable v1 : b.get("local").keySet()){
            newb.get("local").put(v1, b.get("local").get(v1));
        }
        for(Variable v2 : b.get("global").keySet()){
            newb.get("global").put(v2, b.get("global").get(v2));
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
            dataflowOf(s.getDest(), lub(b, dataflowOf(s.getDest())));
        }
        else{
            dataflowOf(s.getDest(), b);
        }
    }

    public void addConstant(Variable x, Variable y, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // System.out.println("expression var is formal:"+isFormal(y));
        if(b.get("local").get(y) != null){
            addConstant(x, b.get("local").get(y), b);
        }
        else if(b.get("global").get(y) != null){
            addConstant(x, b.get("global").get(y), b);
        }
        else if(isFormal(y) && formalParams.get(this.currProc.getFormalParameters().indexOf(y.getId())) != null){
            // This piece here tries to find if we have a value for the formal parameter y
            addConstant(x, formalParams.get(this.currProc.getFormalParameters().indexOf(y.getId())), b);
        }
    }

    public void addConstant(Variable x, IntegerConstant constant, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // if(b.get("local").get(x) != null && b.get("local").get(x) != constant){
        //     b.get("local").remove(x);
        // }
        // else if(b.get("global").get(x) != null && b.get("global").get(x) != constant){
        //     b.get("global").remove(x);
        // }
        // else if(isFormal(x) && formalParams.get(this.currProc.getFormalParameters().indexOf(x.getId())) != constant){
        //     formalParams.remove(this.currProc.getFormalParameters().indexOf(x.getId()));
        // }
        // else{
        if(!x.toString().startsWith("$")){
            if(isLocal(x) || isFormal(x)){
                b.get("local").put(x, constant);
            }
            else{
                b.get("global").put(x, constant);
            }
        }
        // }
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

    public ConstantPropagationAnalysis setupVisitor(HashMap<String, HashMap<Variable, IntegerConstant>> b,
                                                    petter.cfg.expression.MethodCall mc){
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        cfv.setProc(currProc);
        HashMap<Integer, IntegerConstant> parameterVals = new HashMap<Integer, IntegerConstant>();
        List<Expression> actualParams = mc.getParamsUnchanged();
        System.out.println("EDW!");

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
        System.out.println("Will enter func with parameterVals:"+parameterVals);
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
    Procedure currProc;
    ArrayList<Procedure> worklist;
    HashMap<Integer, IntegerConstant> formalParams;
    HashMap<String, HashMap<Variable, IntegerConstant>> map;
    ArrayList<State> visited;
    public ConstantPropagationAnalysis(CompilationUnit cu){
        super(true); // forward analysis
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.visited = new ArrayList<State>();
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Procedure s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        System.out.println("--------------------- Visiting Procedure: "+s.getName()+" ---------------------");
        System.out.println("Current state: "+b+" formals:"+this.formalParams);
        System.out.println("local vars:" + s.getLocalVariables());
        this.currProc = s;
        if(b == null){
            b = new HashMap<String, HashMap<Variable, IntegerConstant>>();
            b.put("local", new HashMap<Variable, IntegerConstant>());
            b.put("global", new HashMap<Variable, IntegerConstant>());
        }
        dataflowOf(s.getBegin(), b);
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Nop s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        if(s.getSource() != null){
            b = dataflowOf(s.getSource());
        }
        else{
            if(b == null){
                b = new HashMap<String, HashMap<Variable, IntegerConstant>>();
                b.put("local", new HashMap<Variable, IntegerConstant>());
                b.put("global", new HashMap<Variable, IntegerConstant>());
            }
        }
        setDataflow(s, b);
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(Assignment s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        System.out.println("Visiting assignment: "+s);
        System.out.println("Current state: "+b);
        b = deepCopy(dataflowOf(s.getSource()));
        // if(b == null){
        //     b = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        //     b.put("local", new HashMap<Variable, IntegerConstant>());
        //     b.put("global", new HashMap<Variable, IntegerConstant>());
        // }
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
            // ASK if this is the way to retrieve the result of the analysis
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
        ConstantFindingVisitor cfv = new ConstantFindingVisitor(b);
        b = dataflowOf(s.getSource());
        s.getAssertion().accept(cfv);
        setDataflow(s, b);
        // System.out.println("constant in condition:"+cfv.getConstant());
        return b;
    }

    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(MethodCall s, HashMap<String, HashMap<Variable, IntegerConstant>> b){
        // System.out.println("In methodcall starting from: "+s.getSource());
        petter.cfg.expression.MethodCall mc = s.getCallExpression();
        Procedure called = this.cu.getProcedure(mc.getName());
        ConstantPropagationAnalysis interproc = setupVisitor(b, mc);
        b = dataflowOf(s.getSource());
        _enter(interproc, called, b);
        _combine(b, interproc.dataflowOf(called.getEnd()), called.getName(), null);
        setDataflow(s, b);
        return b;
    }
    
    public HashMap<String, HashMap<Variable, IntegerConstant>> visit(State s, HashMap<String, HashMap<Variable, IntegerConstant>> newflow){
        System.out.println("Visiting "+ s.toString());
        System.out.println("Current state: "+dataflowOf(s));
        if(visited.contains(s))return null;
        visited.add(s);

        // HashMap<String, HashMap<Variable, IntegerConstant>> oldflow = dataflowOf(s);
        // if(newflow == null)
        //     newflow = new HashMap<String, HashMap<Variable, IntegerConstant>>();
        // HashMap<String, HashMap<Variable, IntegerConstant>> newval = lub(oldflow, newflow);
        // HashMap<String, HashMap<Variable, IntegerConstant>> newvalCopy = deepCopy(newval);
        // dataflowOf(s, newvalCopy);
        // // dataflowOf(s, newval);
        // for (State ss: currProc.getStates()){
        //     System.out.println("For "+ss+" we have "+dataflowOf(ss));
        // }
        
        if(s.isEnd()){
            System.out.println("--------------------- Exiting Procedure: "+currProc.getName()+" ---------------------");
            DotLayout layout = new DotLayout("jpg", currProc.getName()+"After1.jpg");
            for (State ss: currProc.getStates()){
                // System.out.println("For "+ss+" we have "+dataflowOf(ss));
                layout.highlight(ss,(dataflowOf(ss))+"");
            }
            try{
                layout.callDot(currProc);
            }
            catch(Exception e){
                System.out.println(e);
            }
        }
        
        return newflow;
    }
}
