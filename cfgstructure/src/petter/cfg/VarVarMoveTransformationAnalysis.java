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
import petter.cfg.expression.VarSubstituteVisitor;

public class VarVarMoveTransformationAnalysis extends AbstractVisitor {

    private VarToVarMoveAnalysis varTovarMap;
    // private HashMap<String, Variable> availableExpr;
    private List<State> visitedStates;

    public VarVarMoveTransformationAnalysis(VarToVarMoveAnalysis varTovar) {
        super(true); // forward reachability
        this.varTovarMap = varTovar;
        // this.availableExpr = this.varTovarMap.getAvailableExpr();
        this.visitedStates = new ArrayList<State>();
    }

    public boolean visit(State s) {
        if(visitedStates.contains(s)) {
            return false;
        }
        visitedStates.add(s);
        return true;
    }

    public boolean visit(Assignment s) {
        // System.out.println("Visiting assignment: "+s.getLhs().toString()+" = "+s.getRhs().toString());
        // System.out.println("Source of this assignment : " + s.getSource());
        // System.out.println("Destination of this QQQxassignment : " + s.getDest());

        if(s.getLhs().toString().startsWith("$")) {
            return true;
        }
        if(s.getLhs() instanceof Variable) {
            if(s.getRhs() instanceof IntegerConstant) {
                return true;
            }
            else {
                //Binary or Unary
                s.getRhs().accept(new VarSubstituteVisitor(this.varTovarMap, s.getSource(), s.getDest(), s));
            }
        }
        else {
            s.getLhs().accept(new VarSubstituteVisitor(this.varTovarMap, s.getSource(), s.getDest(), s));
            s.getRhs().accept(new VarSubstituteVisitor(this.varTovarMap, s.getSource(), s.getDest(), s));

        }
        return true;
    }

    public boolean visit(GuardedTransition s) {
        s.getAssertion().accept(new VarSubstituteVisitor(this.varTovarMap, s.getSource(), s.getDest(), null));

        return true;
    }

    public boolean visit(Procedure s) {
        return true;
    }

    public boolean visit(Nop s) {

        return true;
    }

    public boolean visit(MethodCall s) {
        return true;
    }

}

