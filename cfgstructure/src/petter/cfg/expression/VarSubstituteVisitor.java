
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
public class VarSubstituteVisitor extends AbstractExpressionVisitor {

    private VarToVarMoveAnalysis varTovarMap;
    private HashMap<String, Variable> availableExpr;
    // private Variable lhs;
    private String rhs;
    private State dest;

    private HashMap<Variable, Variable> substituteV;
    private State source;
    private BinaryExpression be;

    public VarSubstituteVisitor(VarToVarMoveAnalysis varTovarMap, HashMap<String, Variable> availableExpr, State source, State dest) { //, Expression lhs, Expression rhs) {
        // if(exprMap == null) {
        //     this.d = new HashMap<String, HashSet<Variable>>();
        // }
        // else {
        //     this.d = exprMap;
        // }
        this.availableExpr = availableExpr;
        // this.lhs = lhs;
        // this.rhs = rhs;
        this.substituteV = new HashMap<Variable, Variable>();
        this.source = source;
        this.dest = dest;
        this.varTovarMap = varTovarMap;
        this.be = null;
        System.out.println("VarSubstituteVisitor");
    }

    public HashMap<Variable, Variable> getsubstituteV() {
        return this.substituteV;
    }

    // public void removeVarFromHashSet(Variable v) {
    //     for(String e : this.d.keySet()) {
    //         this.d.get(e).remove(v);
    //     }
    // }

    public boolean preVisit(IntegerConstant s) {
        System.out.println("IntegerConstant in Visitor: " + s.toString());
        return true;
    }

    public boolean preVisit(Variable s) {
        System.out.println("Variable in Visitor: " + s.toString());

        HashMap<String, HashSet<Variable>> flowOfSource = this.varTovarMap.dataflowOf(this.source);
        for(String key : flowOfSource.keySet()) {
            if(flowOfSource.get(key).contains(s) && flowOfSource.get(key).size() > 1) {
                this.be.substitute(s, this.availableExpr.get(key));
            }
        }
        return false;
    }

    public boolean preVisit(UnaryExpression s) {
        System.out.println("Unary in Visitor: " + s.toString());
        if(!s.hasArrayAccess()) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean preVisit(BinaryExpression s) {
        System.out.println("BinaryExpression in Visitor: " + s.toString());
        this.be = s;
        return true;
    }

}
