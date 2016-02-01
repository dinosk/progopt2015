package petter.cfg.expression;
import java.util.Map;
import java.util.HashMap;
/**
 * represents an UnaryExpression 
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class UnaryExpression implements Expression, java.io.Serializable{
    private Expression e;
    private Operator sign;
    /**
     * Arbitrary annotations identified by key.
     */
    private Map<Object, Object> annotations;
    public Object getAnnotation(Object key) {
        if (annotations == null) return null;
        return annotations.get(key);
    }
    public Object putAnnotation(Object key, Object value) {
        if (annotations == null)
            annotations = new HashMap<Object, Object>();
        return annotations.put(key, value);
    }
    public <T> T getAnnotation(Class<T> key) throws ClassCastException {
        if (annotations == null) return null;
        return key.cast(annotations.get(key));
    }
    public <T> T putAnnotation(Class<T> key, T value) throws ClassCastException {
        if (annotations == null)
            annotations = new HashMap<Object, Object>();
        return key.cast(annotations.put(key, value));
    }
    public Map<Object, Object> getAnnotations() {
        return annotations;
    }
    public void putAnnotations(Map<?, ?> a) {
        annotations.putAll(a);
    }

    /**
     * create a new UnaryExpression  
     * @param e expression
     * @param sign sign of the expression
     */
    public UnaryExpression(Expression e,Operator sign){
	this.e=e;
	this.sign=sign;
    }
    /**
     * @return string representation of signed unaryExpression
     */
    public String toString(){
	return sign.toString()+e.toString();
    }
    /**
     * get the sign of the UnaryExpression
     * @return guess what?
     */
    public Operator getOperator(){
	return sign;
    }
    /**
     * set the sign of the UnaryExpression
     */
    public void setOperator(Operator op){
	this.sign = op;
    }
    /**
     * get the expression of the UnaryExpression
     * @return guess what?
     */
    public Expression getExpression(){
	return e;
    }
    /**
     * check if UnaryExpression contains a Multiplication
     * @return guess what?
     */
    public boolean hasMultiplication(){
	return e.hasMultiplication();
    }
    /**
     * check if UnaryExpression contains a Division
     * @return guess what?
     */
    public boolean hasDivision(){
	return e.hasDivision();
    }
    /**
     * check if UnaryExpression is invertible
     * @return guess what?
     */
    public boolean isInvertible(Variable var){
	return e.isInvertible(var);
    }
    /**
     * check if UnaryExpression is linear
     * @return guess what?
     */
    public boolean isLinear(){
	return e.isLinear();
    }
    /**
     * check if UnaryExpression has a method call
     * @return guess what?
     */
    public boolean hasMethodCall(){
	return e.hasMethodCall();
    }
    /**
     * check if UnaryExpression conatins an UnknownExpression
     * @return guess what?
     */
    public boolean hasUnknown(){
	if (!sign.is(Operator.MINUS)) return true;
	return e.hasUnknown();
    }
    /**
     * analysis of an expression
     * @param v the analysing ExpressionVisitor
     */
    public void accept(ExpressionVisitor v){
	if (v.preVisit(this)) e.accept(v);
	v.postVisit(this);
    }
    /**
     * @return degree of UnaryExpression
     */
    public int getDegree(){
	return e.getDegree();
    }
    public void substitute(Variable v, Expression ex){
        if (e.equals(v)) e = ex;
        else e.substitute(v,ex);
    }
    public boolean equals(Object o){
        if (! (o instanceof UnaryExpression)) return false;
        if (!sign.equals(((UnaryExpression)o).sign)) return false;
        return e.equals(((UnaryExpression)o).e);
    }

    @Override
    public boolean hasArrayAccess() {
        return e.hasArrayAccess();
    }
  
}

