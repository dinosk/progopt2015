package petter.cfg.expression;

import petter.cfg.*;
import petter.cfg.edges.*;
import java.util.*;
/**
 * provides an abstract class to visit an expression;
 * the visitor performs a run through the whole expression, as long as it's visit methods return true;
 * to terminate you have to ensure that the return value of a visit method becomes false at some point;
 * @see ExpressionVisitor
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class FindVarsVisitor extends AbstractExpressionVisitor {

    private HashMap<String, HashSet<Variable>> d;
    private HashMap<String, Variable> availableExpr;
    private Variable lhs;
    private String rhs;
    private State dest;

    private HashSet<Variable> usedVars;

    public FindVarsVisitor() {

        this.usedVars = new HashSet<Variable>();
    }

    public HashSet<Variable> getUsedVars() {
        return this.usedVars;
    }

    public boolean preVisit(Variable s) {
        this.usedVars.add(s);
        return true;
    }

    public boolean preVisit(BinaryExpression s) {
        if(s.hasArrayAccess()){
            s.getRight().accept(this);
            return false;
        }
        return true;
    }

}
