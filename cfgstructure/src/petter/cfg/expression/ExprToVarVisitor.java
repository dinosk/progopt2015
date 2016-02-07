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

    private HashMap<String, HashSet<Variable>> d;
    private HashMap<String, Variable> availableExpr;
    private Variable lhs;
    private String rhs;
    private State dest;

    public ExprToVarVisitor(HashMap<String, HashSet<Variable>> exprMap, HashMap<String, Variable> availableExpr, Variable lhs, String rhs) {
        // System.out.println("Map " + exprMap);
        if(exprMap == null) {
            this.d = new HashMap<String, HashSet<Variable>>();
        }
        else {
            this.d = exprMap;
        }
        this.availableExpr = availableExpr;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public HashMap<String, HashSet<Variable>> getExprMap() {
        return this.d;
    }

    public void removeVarFromHashSet(Variable v) {
        for(String e : this.d.keySet()) {
            this.d.get(e).remove(v);
        }
    }

    public boolean preVisit(IntegerConstant s) {
        // System.out.println("IntegerConstant in Visitor: " + s.toString());
        if(!this.availableExpr.containsKey(this.rhs)) {
            this.availableExpr.put(this.rhs, this.lhs);
        }
        for(String e : this.d.keySet()) {
           this.d.get(e).remove(this.lhs);
        }
        if(this.d.containsKey(this.rhs)) {
            this.d.get(this.rhs).add(this.lhs);
        }
        else {
            HashSet<Variable> vars = new HashSet<Variable>();
            vars.add(this.lhs);
            this.d.put(this.rhs, vars);
        }
        return false;
    }

    public boolean preVisit(Variable s) {
        // System.out.println("Variable in Visitor: " + s.toString());
        for (String e : this.d.keySet()) {
            if(this.d.get(e).contains(s)) {
                this.d.get(e).add(this.lhs);
            }
            else {
                this.d.get(e).remove(this.lhs);
            }
        }
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
                if(this.d.containsKey(e.toString()))
                    this.d.get(e.toString()).clear();
                removeVarFromHashSet(this.lhs);
            }
        }
        // Arithmetic Operation
        else {
            // System.out.println("BinExpr is Arithmetic");
            if(!this.availableExpr.containsKey(this.rhs)) {
                this.availableExpr.put(this.rhs, this.lhs);
            }
            removeVarFromHashSet(this.lhs);
            if(this.d.containsKey(this.rhs)) {
                this.d.get(this.rhs).add(this.lhs);
            }
            else {
                HashSet<Variable> vars = new HashSet<Variable>();
                vars.add(this.lhs);
                this.d.put(this.rhs, vars);
            }
            // return false;
        }
        return false;
    }

}
