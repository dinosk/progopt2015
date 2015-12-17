package petter.cfg.expression;

import petter.cfg.Annotatable;
/**
 * provides an interface to constructing an expression
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public interface Expression extends Annotatable{
    /**
     * check if an expression contains a multiplication
     */
    boolean hasMultiplication();
   /**
     * check if an expression contains a division
     */
    boolean hasDivision();
    /**
     * check if a variable is invertible
     */
    boolean isInvertible(Variable var);
   /**
     * check if an expression is linear
     */
    boolean isLinear();
   /**
     * check if an expression contains a method call
     */
    boolean hasMethodCall();
   /**
     * check if an expression has an unknown expression
     */
    boolean hasUnknown();
    /**
     * analysis of an expression
     * @param v the analysing ExpressionVisitor
     */
    void accept(ExpressionVisitor v);
    /**
     * get the degree of an expression
     */
    int getDegree();
    /**
     * substitute this variable with the following expression
     */
    void substitute(Variable v, Expression ex);
  
}
