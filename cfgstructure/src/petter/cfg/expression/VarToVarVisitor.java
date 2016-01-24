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
public class VarToVarVisitor extends AbstractExpressionVisitor{
    /**
     * provides a standardized return value for all not overwritten visit methods.
     * the default value here is true, meaning, that a not overwritten visit method just passes on to the next item in the queue
     * @param  e item, which is visited; can be ignored
     * @return a default value for visit methods
     */

    private HashMap<Variable, Variable> varTovarMap;

    public VarToVarVisitor(HashMap<Variable, Variable> d){
        this.varTovarMap = d;
    }

    protected boolean defaultBehaviour(Expression e){
        return true;
    }

    public boolean preVisit(IntegerConstant s){
        return defaultBehaviour(s);
    }

    public boolean preVisit(Variable s){
        if(varTovarMap.containsKey(s)) {
            s = varTovarMap.get(s);
        }
        // substitute ...

        return defaultBehaviour(s);
    }

    public boolean preVisit(MethodCall s) {
        return defaultBehaviour(s);
    }

    public boolean preVisit(UnknownExpression s) {
        return defaultBehaviour(s);
    }
    public boolean preVisit(UnaryExpression s){
        return defaultBehaviour(s);
    }

    public boolean preVisit(BinaryExpression s){
        s.getLeft().accept(this);
        s.getRight().accept(this);

        return defaultBehaviour(s);
    }
}
