package petter.cfg;

import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.VarCopyVisitor;



public class CopyPropagationAnalysis extends AbstractPropagatingVisitor<HashMap<String, HashMap<Variable, Variable>>>{

    private CompilationUnit cu;
    private TransitionFactory tf;
    private Procedure current_method;

    public CopyPropagationAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu = cu;
        this.tf = new TransitionFactory();
    }
    public void _enter(CopyPropagationAnalysis copyprop, Procedure callee, HashMap<String, HashMap<Variable, Variable>> initialState) {
        HashMap<String, HashMap<Variable, Variable>> propagated = new HashMap<String, HashMap<Variable, Variable>>();
        // keep globals as they are and set locals to 0
        propagated.put("local", new HashMap<Variable, Variable>());
        propagated.put("global", new HashMap<Variable, Variable>());
        propagated.get("global").putAll(initialState.get("global"));

        copyprop.enter(callee, propagated);
        copyprop.fullAnalysis();
    }

    public void _combine(HashMap<String, HashMap<Variable, Variable>> initialState, HashMap<String, HashMap<Variable, Variable>> callEffect) {
        // Only the globals are updated
        // callEffect.get("global").keySet().removeAll(initialState.get("local").keySet());
        initialState.get("global").putAll(callEffect.get("global"));
    }

    public HashMap<String, HashMap<Variable, Variable>> visit(Procedure s, HashMap<String, HashMap<Variable, Variable>> b){
        this.current_method = s;
        System.out.println("Procedure: " + s.getName());
        if(b == null) {
            b = new HashMap<String, HashMap<Variable, Variable>>();
            b.put("local", new HashMap<Variable, Variable>());
            b.put("global", new HashMap<Variable, Variable>());
        }
        return b;
    }

    public HashMap<String, HashMap<Variable, Variable>> visit(Assignment s, HashMap<String, HashMap<Variable, Variable>> b){
        System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());

        if(s.getRhs() instanceof Variable) {
            Variable v = (Variable) s.getRhs();
            // rhs of Assignment is Variable
            boolean flag = true;
            String key;
            while(flag) {
                flag = false;
                // apply transitively to all assignments
                key = "global";
                if(this.current_method.getLocalVariables().contains(v.getId())){
                    key = "local";
                }
                while(b.get(key).containsKey(v)) {
                    flag = true;
                    v = b.get(key).get(v);
                }
            }
            if(this.current_method.getLocalVariables().contains(s.getLhs().getId())) {
                b.get("local").put(s.getLhs(), v);
            }
            else {
                b.get("global").put(s.getLhs(), v);
            }
        }
        return b;
    }

    public HashMap<String, HashMap<Variable, Variable>> visit(GuardedTransition s, HashMap<String, HashMap<Variable, Variable>> b){
        //check expressions of the form: if(flag)
        System.out.println("Visiting if: " + s.toString());

        VarCopyVisitor varcopy = new VarCopyVisitor(this.current_method, b);
        s.getAssertion().accept(varcopy);

        return b;
    }

    public HashMap<String, HashMap<Variable, Variable>> visit(MethodCall s, HashMap<String, HashMap<Variable, Variable>> b){
        System.out.println("Visiting MethodCall:"+ s.toString());
        System.out.println("Current state: "+b);

        Procedure callee = cu.getProcedure(s.getCallExpression().getName());
        CopyPropagationAnalysis copyprop = new CopyPropagationAnalysis(this.cu);
        _enter(copyprop, callee, b);
        _combine(b, copyprop.dataflowOf(callee.getEnd())); /////

        return b;
    }

    // public HashMap<String, HashMap<Variable, IntegerConstant>> visit(State s, HashMap<String, HashMap<Variable, Variable>> newflow){

    // }

}
