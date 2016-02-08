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
import petter.cfg.expression.FindVarsVisitor;

public class RemoveDeadVarsAnalysis extends AbstractPropagatingVisitor<HashSet<Variable>> {

    private boolean fixpointCheck;
    private List<State> visitedStates;
    private IntraTrulyLivenessAnalysis itLive;

    public RemoveDeadVarsAnalysis(IntraTrulyLivenessAnalysis itLive) {
        super(false); // backward analysis
        this.visitedStates = new ArrayList<State>();
        this.itLive = itLive;
    }

    public boolean getFixpointCheck() {
        return this.fixpointCheck;
    }

    public HashSet<Variable> visit(State s, HashSet<Variable> d) {
        if(visitedStates.contains(s)) {
            return null;
        }
        visitedStates.add(s);
        return d;
    }

    public HashSet<Variable> visit(Assignment s, HashSet<Variable> d) {
        if(s.getLhs() instanceof Variable && !s.getLhs().toString().startsWith("$")) {
            Variable v = (Variable) s.getLhs();
            if(!(s.getRhs() instanceof UnknownExpression)) {
                if(!(this.itLive.dataflowOf(s.getDest()).contains(v)) && !v.toString().equals("return")) {
                    System.out.println("Assignment to be removed "+s.toString());
                    s.removeEdge();
                    Nop edge = new Nop(s.getSource(), s.getDest());
                    s.getSource().addOutEdge(edge);
                    s.getSource().getMethod().resetTransitions();
                }
            }
        }
        return d;
    }

    public HashSet<Variable> visit(GuardedTransition s, HashSet<Variable> d) {

        return d;
    }

    public HashSet<Variable> visit(Procedure s, HashSet<Variable> d) {
        if(d == null) {
            d = new HashSet<Variable>();
        }
        this.fixpointCheck = true;
        this.visitedStates.clear();
        return d;
    }

    public HashSet<Variable> visit(Nop s, HashSet<Variable> d) {
        return d;
    }

    public HashSet<Variable> visit(MethodCall s, HashSet<Variable> d) {
        return d;
    }

}
