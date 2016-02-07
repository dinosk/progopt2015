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

public class VarToVarMoveAnalysis extends AbstractPropagatingVisitor<HashMap<String, HashSet<Variable>>> {

    private CompilationUnit cu;
    private HashMap<String, Variable> availableExpr;
    private boolean fixpointCheck;
    private List<State> visitedStates;

    public VarToVarMoveAnalysis(CompilationUnit cu) {
        super(true); // forward reachability
        this.cu = cu;
        this.availableExpr = new HashMap<String, Variable>();
        this.fixpointCheck = false;
        this.visitedStates = new ArrayList<State>();
    }

    static HashMap<String, HashSet<Variable>> lub(HashMap<String, HashSet<Variable>> b1, HashMap<String, HashSet<Variable>> b2){
        if (b1 == null)
            return b2;
        if (b2 == null)
            return b1;


        for(String expr : b1.keySet()) {
            if(!b2.containsKey(expr)) {
                b1.get(expr).clear();
            }
            else if(b1.get(expr).isEmpty() || b2.get(expr).isEmpty()) {
                b1.get(expr).clear();
            }
            else {
                // System.out.println("Edwwwww " + expr);
                b1.get(expr).retainAll(b2.get(expr));
                System.out.println("In lub 8a meinei : " + b1);
            }
        }
        return b1;
    }

    static boolean notequal(HashMap<String, HashSet<Variable>> b1, HashMap<String, HashSet<Variable>> b2){
        System.out.println("In notequal : " + b1 + " " + b2);
        if (b1 == null || b2 == null)
            return true;
        boolean res = !b1.equals(b2);
        System.out.println("RES : " + res);
        return res;
    }

    public HashMap<String, Variable> getAvailableExpr() {
        return this.availableExpr;
    }

    public void setFixpointCheck() {
        this.fixpointCheck = false;
    }

    public boolean getFixpointCheck() {
        return this.fixpointCheck;
    }

    public HashMap<String, HashSet<Variable>> deepCopy(HashMap<String, HashSet<Variable>> currentState){
        HashMap<String, HashSet<Variable>> newState = new HashMap<String, HashSet<Variable>>();

        if(currentState == null) {
            return newState;
        }
        for(String expr : currentState.keySet()) {
            HashSet<Variable> vars = new HashSet<Variable>();
            for(Variable v : currentState.get(expr)) {
                vars.add(v);
            }
            newState.put(expr, vars);
        }
        return newState;
    }

    public void setDataFlow(State s, HashMap<String, HashSet<Variable>> d) {
        HashMap<String, HashSet<Variable>> oldflow = dataflowOf(s);
        HashMap<String, HashSet<Variable>> newflow;

        if(oldflow != null) {
            newflow = lub(dataflowOf(s), d);
        }
        else {
            newflow = d;
        }
        dataflowOf(s, newflow);

        if(notequal(newflow, oldflow)) {
            this.fixpointCheck = true;
        }
        else {
        }
    }

    public HashMap<String, HashSet<Variable>> visit(State s, HashMap<String, HashSet<Variable>> d) {
        if(visitedStates.contains(s)) {
            return null;
        }
        visitedStates.add(s);
        return d;
    }

    public HashMap<String, HashSet<Variable>> visit(Assignment s, HashMap<String, HashSet<Variable>> d) {
        System.out.println("Visiting assignment: "+s.getLhs().toString()+" = "+s.getRhs().toString());
        System.out.println("Source of this assignment : " + s.getSource());
        System.out.println("Destionation of this assignment : " + s.getDest());

        if(s.getLhs() instanceof Variable && !(s.getLhs().toString().startsWith("$"))) {
            String rhs = s.getRhs().toString();
            Variable lhs = (Variable) s.getLhs();
            ExprToVarVisitor v = new ExprToVarVisitor(deepCopy(dataflowOf(s.getSource())), availableExpr, lhs, rhs);
            s.getRhs().accept(v);
            setDataFlow(s.getDest(), v.getExprMap());

        }
        //check with unaries and memory stores in the LHS
        else {
            d = deepCopy(dataflowOf(s.getSource()));
            if(s.getLhs() instanceof BinaryExpression) {
                BinaryExpression be = (BinaryExpression) s.getLhs();
                Expression e = be.getRight();
                if(e instanceof BinaryExpression) {
                    if(d.containsKey(e.toString())) {
                        d.get(e.toString()).clear(); // return the empty set for this expression
                    }
                }
            }
            setDataFlow(s.getDest(), d);
        }
        return d;
    }

    public HashMap<String, HashSet<Variable>> visit(GuardedTransition s, HashMap<String, HashSet<Variable>> d) {
        d = deepCopy(dataflowOf(s.getSource()));
        if(d.containsKey(s.getAssertion().toString())) {
            d.get(s.getAssertion().toString()).clear(); // return the empty set for this expression
        }

        setDataFlow(s.getDest(), d);
        return d;
    }

    public HashMap<String, HashSet<Variable>> visit(Procedure s, HashMap<String, HashSet<Variable>> d) {
        if(d == null) {
            d = new HashMap<String, HashSet<Variable>>();
        }
        this.visitedStates.clear();
        return d;
    }

    public HashMap<String, HashSet<Variable>> visit(Nop s, HashMap<String, HashSet<Variable>> d) {
        setDataFlow(s.getDest(), deepCopy(dataflowOf(s.getSource())));

        return d;
    }

    public HashMap<String, HashSet<Variable>> visit(MethodCall s, HashMap<String, HashSet<Variable>> d) {
        return d;
    }

}
