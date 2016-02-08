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
public class RemoveExprXVisitor extends AbstractExpressionVisitor {

    private boolean flag;
    private Variable v;

    public RemoveExprXVisitor(Variable v) {
        this.flag = false;
        this.v = v;
    }

    public boolean getRemoveFlag() {
        return this.flag;
    }

    public boolean preVisit(Variable s) {
        if(this.v.equals(s))
            this.flag = true;
        return true;
    }
}
