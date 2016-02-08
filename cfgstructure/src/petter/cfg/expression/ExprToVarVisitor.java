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
public class ExprToVarVisitor extends AbstractExpressionVisitor {

    private HashMap<Expression, ArrayList<Variable>> d;
    // private HashMap<Expression, Variable> availableExpr;
    private Variable lhs;
    private Expression rhs;
    private State dest;

    public ExprToVarVisitor(HashMap<Expression, ArrayList<Variable>> exprMap, Variable lhs, Expression rhs) {
        // System.out.println("Map " + exprMap);
        if(exprMap == null) {
            this.d = new HashMap<Expression, ArrayList<Variable>>();
        }
        else {
            this.d = exprMap;
        }
        // this.availableExpr = availableExpr;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public HashMap<Expression, ArrayList<Variable>> getExprMap() {
        return this.d;
    }

    public void removeVarFromHashSet(Variable v) {
        for(Expression e : this.d.keySet()) {
            this.d.get(e).remove(v);
        }
    }

    public void removeExprX(Variable v) {
        ArrayList<Expression> toRemove = new ArrayList<Expression>();
        for(Expression e : this.d.keySet()) {
            RemoveExprXVisitor vv = new RemoveExprXVisitor(v);
            e.accept(vv);
            if(vv.getRemoveFlag())
                toRemove.add(e);
        }
        for(Expression e : toRemove) {
            this.d.remove(e);
        }
    }

    public boolean preVisit(IntegerConstant s) {
        // if(!this.availableExpr.containsKey(this.rhs)) {
        //     this.availableExpr.put(this.rhs, this.lhs);
        // }
        for(Expression e : this.d.keySet()) {
           this.d.get(e).remove(this.lhs);
        }
        // if(this.d.containsKey(this.rhs)) {
        //     this.d.get(this.rhs).add(this.lhs);
        // }
        // else {
        //     HashSet<Variable> vars = new HashSet<Variable>();
        //     vars.add(this.lhs);
        //     this.d.put(this.rhs, vars);
        // }
        boolean found = false;
        for(Expression e : this.d.keySet()) {
                if(e instanceof IntegerConstant) {
                    IntegerConstant c = (IntegerConstant) e;
                    if(c.equals(this.rhs)) {
                        this.d.get(e).add(this.lhs);
                        found = true;
                        break;
                    }
                }
            }
            if(!found) {
                ArrayList<Variable> vars = new ArrayList<Variable>();
                vars.add(this.lhs);
                this.d.put(this.rhs, vars);
            }
        removeExprX(this.lhs);
        return false;
    }

    public boolean preVisit(Variable s) {
        // System.out.println("Variable in Visitor: " + s.toString());
        for (Expression e : this.d.keySet()) {
            if(this.d.get(e).contains(s)) {
                this.d.get(e).add(this.lhs);
            }
            else {
                this.d.get(e).remove(this.lhs);
            }
        }
        removeExprX(this.lhs);
        return false;
    }

    public boolean preVisit(UnaryExpression s) {
        // System.out.println("Unary in Visitor: " + s.toString());
        if(!s.hasArrayAccess()) {
            removeVarFromHashSet(this.lhs);
            return false;
        }
        else {
            return true;
        }
    }

    public boolean preVisit(BinaryExpression s) {
        // System.out.println("BinaryExpression in Visitor: " + s.toString());
        // if(s.hasArrayAccess()) {
        //     // System.out.println("Expr hasArrayAccess!");
        //     removeVarFromHashSet(this.lhs);
        //     return false;
        // }
        // Memory Expr
        if(s.getOperator().is(Operator.ARRAY)) {
            Expression e = s.getRight();
            // System.out.println("BinExpr is Array");
            if(e instanceof IntegerConstant || e instanceof Variable) {
                removeVarFromHashSet(this.lhs);
            }
            if(e instanceof BinaryExpression) {
                // this.d.remove(e.toString()); !!!!
                // if(this.d.containsKey(e))
                //     this.d.get(e).clear();
                BinaryExpression bexpr = (BinaryExpression) e;
                for(Expression expr : this.d.keySet()) {
                    if(expr instanceof BinaryExpression) {
                        BinaryExpression be = (BinaryExpression) expr;
                        if(be.equals(bexpr)) {
                            this.d.get(expr).clear();
                        }
                    }
                }
                removeVarFromHashSet(this.lhs);
            }
            removeExprX(this.lhs);
        }
        // Arithmetic Operation
        else {
            // System.out.println("BinExpr is Arithmetic");
            // if(!this.availableExpr.containsKey(this.rhs)) {
            //     this.availableExpr.put(this.rhs, this.lhs);
            // }
            removeVarFromHashSet(this.lhs);
            // if(this.d.containsKey(this.rhs)) {
            //     this.d.get(this.rhs).add(this.lhs);
            // }
            boolean found = false;
            for(Expression e : this.d.keySet()) {
                if(e instanceof BinaryExpression) {
                    BinaryExpression be = (BinaryExpression) e;
                    if(be.equals(this.rhs)) {
                        this.d.get(e).add(this.lhs);
                        found = true;
                        break;
                    }
                }
            }
            if(!found) {
                ArrayList<Variable> vars = new ArrayList<Variable>();
                vars.add(this.lhs);
                this.d.put(this.rhs, vars);
            }
            removeExprX(this.lhs);
        }
        return false;
    }

}
