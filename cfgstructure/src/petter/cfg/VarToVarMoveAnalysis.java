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

public class VarToVarMoveAnalysis extends AbstractPropagatingVisitor<HashMap<Expression, ArrayList<Variable>>> {

    private boolean fixpointCheck;
    private List<State> visitedStates;

    public VarToVarMoveAnalysis() {
        super(true); // forward reachability
        this.visitedStates = new ArrayList<State>();
    }

    static HashMap<Expression, ArrayList<Variable>> lub(HashMap<Expression, ArrayList<Variable>> b1, HashMap<Expression, ArrayList<Variable>> b2){
        if (b1 == null)
            return b2;
        if (b2 == null)
            return b1;

        for(Expression expr : b1.keySet()) {
            if(b2.get(expr) == null) {
                b1.get(expr).clear();
            }

            else if(b1.get(expr).isEmpty() || b2.get(expr).isEmpty()) {
                b1.get(expr).clear();
            }


            else {
                b1.get(expr).retainAll(b2.get(expr));
            }
        }

        return b1;
    }

    public boolean getFixpointCheck() {
        return this.fixpointCheck;
    }

    public HashMap<Expression, ArrayList<Variable>> deepCopy(HashMap<Expression, ArrayList<Variable>> currentState){
        HashMap<Expression, ArrayList<Variable>> newState = new HashMap<Expression, ArrayList<Variable>>();

        if(currentState == null) {
            return newState;
        }
        for(Expression expr : currentState.keySet()) {
            ArrayList<Variable> vars = new ArrayList<Variable>();
            for(Variable v : currentState.get(expr)) {
                vars.add(v);
            }
            newState.put(expr, vars);
        }
        return newState;
    }

    public void setDataFlow(State s, HashMap<Expression, ArrayList<Variable>> d) {
        HashMap<Expression, ArrayList<Variable>> oldflow = dataflowOf(s);
        HashMap<Expression, ArrayList<Variable>> newflow;

        if(oldflow != null) {
            newflow = lub(dataflowOf(s), d);
        }
        else {
            newflow = d;
        }
        dataflowOf(s, newflow);

        if(!newflow.equals(oldflow)){
            this.fixpointCheck = false;
        }
    }

    public HashMap<Expression, ArrayList<Variable>> visit(State s, HashMap<Expression, ArrayList<Variable>> d) {
        if(visitedStates.contains(s)) {
            return null;
        }
        visitedStates.add(s);
        return d;
    }

    public HashMap<Expression, ArrayList<Variable>> visit(Assignment s, HashMap<Expression, ArrayList<Variable>> d) {

        // we don't handle $Variables
        if(s.getLhs() instanceof Variable && !(s.getLhs().toString().startsWith("$"))) {
            Variable lhs = (Variable) s.getLhs();
            ExprToVarVisitor v = new ExprToVarVisitor(deepCopy(dataflowOf(s.getSource())), lhs, s.getRhs());
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
                    // return the empty set for this expression
                    BinaryExpression bexpr = (BinaryExpression) e;
                    for(Expression expr : d.keySet()) {
                        if(expr instanceof BinaryExpression) {
                            BinaryExpression bex = (BinaryExpression) expr;
                            if(bex.equals(bexpr)) {
                                d.get(expr).clear();
                            }
                        }
                    }
                }
            }
            setDataFlow(s.getDest(), d);
        }
        return d;
    }

    public HashMap<Expression, ArrayList<Variable>> visit(GuardedTransition s, HashMap<Expression, ArrayList<Variable>> d) {
        d = deepCopy(dataflowOf(s.getSource()));

        if(s.getAssertion() instanceof BinaryExpression) {
            BinaryExpression bexpr = (BinaryExpression) s.getAssertion();
            for(Expression expr : d.keySet()) {
                if(expr instanceof BinaryExpression) {
                    BinaryExpression be = (BinaryExpression) expr;
                    if(be.equals(bexpr)) {
                        d.get(expr).clear();     // return the empty set for this expression
                    }
                }
            }
        }

        setDataFlow(s.getDest(), d);
        return d;
    }

    public HashMap<Expression, ArrayList<Variable>> visit(Procedure s, HashMap<Expression, ArrayList<Variable>> d) {
        if(d == null) {
            d = new HashMap<Expression, ArrayList<Variable>>();
        }
        this.fixpointCheck = true;
        this.visitedStates.clear();
        return d;
    }

    public HashMap<Expression, ArrayList<Variable>> visit(Nop s, HashMap<Expression, ArrayList<Variable>> d) {
        setDataFlow(s.getDest(), deepCopy(dataflowOf(s.getSource())));

        return d;
    }

    public HashMap<Expression, ArrayList<Variable>> visit(MethodCall s, HashMap<Expression, ArrayList<Variable>> d) {
        return d;
    }

}
