package petter.cfg;

import petter.cfg.*;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.Nop;
import petter.cfg.edges.MethodCall;
import petter.cfg.edges.Transition;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Operator;
import petter.cfg.expression.Variable;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.UnknownExpression;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.ExprToVarVisitor;



public class VarToVarMoveAnalysis extends AbstractPropagatingVisitor<HashMap<String, HashSet<Variable>>>{

    private CompilationUnit cu;
    // private TransitionFactory tf;
    private HashMap<String, Variable> availableExpr;
    private ArrayList<State> stopIter;

    static HashMap<String, HashSet<Variable>> lub(HashMap<String, HashSet<Variable>> b1, HashMap<String, HashSet<Variable>> b2){
        if (b1 == null)
            return b2;
        if (b2 == null)
            return b1;

        for(String expr : b1.keySet()) {
            b1.get(expr).retainAll(b2.get(expr));
        }
        return b1;
    }

    static boolean lessoreq(HashMap<String, HashSet<Variable>> b1, HashMap<String, HashSet<Variable>> b2){
        System.out.println("lessoreq: "+ b1 + "   " + b2);
        if (b1 == null)
            return true;
        if (b2 == null)
            return false;
        if(b1.size() > b2.size())
            return false;
        else if(b1.size() == b2.size()) {
            for(String key : b1.keySet()) {
                if(b1.get(key).size() > b2.get(key).size())
                    return false;
            }
            return true;
        }
        else
            return true;
    }

    public VarToVarMoveAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        // this.tf = new TransitionFactory();
        this.availableExpr = new HashMap<String, Variable>();
    }

    public HashMap<String, Variable> getAvailableExpr() {
        return this.availableExpr;
    }

    public HashMap<String, HashSet<Variable>> deepCopy(HashMap<String, HashSet<Variable>> currentState){
        HashMap<String, HashSet<Variable>> newState = new HashMap<String, HashSet<Variable>>();

        for(String expr : currentState.keySet()) {
            HashSet<Variable> vars = new HashSet<Variable>();
            for(Variable v : currentState.get(expr)) {
                vars.add(v);
            }
            newState.put(expr, vars);
        }
        return newState;
    }

    public HashMap<String, HashSet<Variable>> visit(State s, HashMap<String, HashSet<Variable>> newflow) {
        System.out.println(s);
        System.out.println("Current state: "+newflow);
        HashMap<String, HashSet<Variable>> oldflow = dataflowOf(s);
        System.out.println("Old state: "+oldflow);
        if (!lessoreq(newflow, oldflow)) {
            HashMap<String, HashSet<Variable>> newval = lub(oldflow, newflow);
            System.out.println("intersect state: "+newval);

            dataflowOf(s, deepCopy(newval));
            return newval;
        }

        return null;
    }

    public HashMap<String, HashSet<Variable>> visit(Assignment s, HashMap<String, HashSet<Variable>> d) {
        System.out.println("Visiting assignment: "+s.getLhs().toString()+" = "+s.getRhs().toString());
        System.out.println("Current state in Ass: "+d);

        if(s.getLhs().toString().startsWith("$")) {
            return d;
        }
        // check only if the lhs is variable (not interested in stores)
        if(s.getLhs() instanceof Variable) {
            String rhs = s.getRhs().toString();
            Variable lhs = (Variable) s.getLhs();

            s.getRhs().accept(new ExprToVarVisitor(d, availableExpr, lhs, rhs));
        }
        return d;
    }

    public HashMap<String, HashSet<Variable>> visit(GuardedTransition s, HashMap<String, HashSet<Variable>> d) {
        // Expression e = s.getAssertion(); or do nothing
        System.out.println("Guard " + s.toString());
        System.out.println("Guard contain expr: "+d.containsKey(s.getAssertion().toString()));
        d.remove(s.getAssertion().toString()); // den 8a bei pote!
        return d;
    }

    public HashMap<String, HashSet<Variable>> visit(Procedure s, HashMap<String, HashSet<Variable>> d) {
        if(d == null) {
            d = new HashMap<String, HashSet<Variable>>();
        }
        return d;
    }

    public HashMap<String, HashSet<Variable>> visit(Nop s, HashMap<String, HashSet<Variable>> d) {
        return d;
    }

    public HashMap<String, HashSet<Variable>> visit(MethodCall s, HashMap<String, HashSet<Variable>> d) {
        return d;
    }

}
