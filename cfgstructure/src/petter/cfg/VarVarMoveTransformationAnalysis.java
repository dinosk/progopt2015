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

public class VarVarMoveTransformationAnalysis extends AbstractVisitor {

    private CompilationUnit cu;
    private VarToVarMoveAnalysis varTovarMap;
    // private boolean fixpointCheck;
    private HashMap<String, Variable> availableExpr;
    private TransitionFactory tf;
    private List<State> visitedStates;

    public VarVarMoveTransformationAnalysis(CompilationUnit cu, VarToVarMoveAnalysis varTovar) {
        super(true); // forward reachability
        this.cu = cu;
        this.varTovarMap = varTovar;
        this.availableExpr = this.varTovarMap.getAvailableExpr();
        // this.fixpointCheck = false;
        this.tf = new TransitionFactory();
        this.visitedStates = new ArrayList<State>();
    }

    public boolean visit(State s) {
        // System.out.println("In state : " +s);
        // HashMap<String, HashSet<Variable>> newflow = dataflowOf(s);
        // System.out.println("Current state: "+newflow);
        if(visitedStates.contains(s)) {
            return false;
        }
        visitedStates.add(s);
        return true;
    }

    public boolean visit(Assignment s) {
        System.out.println("Visiting assignment: "+s.getLhs().toString()+" = "+s.getRhs().toString());
        System.out.println("Source of this assignment : " + s.getSource());
        System.out.println("Destination of this assignment : " + s.getDest());

        if(s.getLhs().toString().startsWith("$")) {
            // System.out.println("DEST : "+ s.getDest() + " source : " + s.getSource());
            // setDataFlow(s.getDest(), deepCopy(dataflowOf(s.getSource())));
            return true;
        }
        // if(s.getLhs() instanceof Variable) {
        // String rhs = s.getRhs().toString();
        // Variable lhs = (Variable) s.getLhs();
        // ExprToVarVisitor v = new ExprToVarVisitor(deepCopy(dataflowOf(s.getSource())), availableExpr, lhs, rhs);
        // s.getRhs().accept(v);
        // setDataFlow(s.getDest(), v.getExprMap());


        // VarSubstituteVisitor v = new VarSubstituteVisitor(dataflowOf(s.getSource()), this.availableExpr);

        if(s.getLhs() instanceof Variable) {
            // s.getRhs().accept(v);
            if(s.getRhs() instanceof Variable) {
                Variable v = (Variable) s.getRhs();

                HashMap<String, HashSet<Variable>> flowOfSource = this.varTovarMap.dataflowOf(s.getSource());
                for(String key : flowOfSource.keySet()) {
                    if(flowOfSource.get(key).contains(v) && flowOfSource.get(key).size() > 1) {
                        // substitute
                        s.removeEdge();
                        Assignment newEdge = (Assignment) this.tf.createAssignment(s.getSource(), s.getDest(), s.getLhs(), this.availableExpr.get(key));
                        s.getSource().addOutEdge(newEdge);
                        s.getSource().getMethod().resetTransitions();
                    }
                }
            }
        }

        return true;
    }

    public boolean visit(GuardedTransition s) {
        // Expression e = s.getAssertion(); or do nothing
        System.out.println("Guard " + s.toString());

        // System.out.println("Guard contain expr: "+d.containsKey(s.getAssertion().toString()));
        // d.remove(s.getAssertion().toString()); // den 8a bei pote!

        return true;
    }

    public boolean visit(Procedure s) {
        // this.visitedStates.clear();
        return true;
    }

    public boolean visit(Nop s) {
        // setDataFlow(s.getDest(), deepCopy(dataflowOf(s.getSource())));

        return true;
    }

    public boolean visit(MethodCall s) {
        return true;
    }

}

