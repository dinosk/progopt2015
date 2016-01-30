package petter.cfg.expression;
import java.util.Map;
import java.util.HashMap;
import petter.cfg.expression.types.Int;
import petter.cfg.expression.types.Type;

/**
 * represents a constant integer value
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class IntegerConstant implements Expression, java.io.Serializable{
    private int value;
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
     * create a new constant integer value 
     * @param value integer value
     */
    public IntegerConstant(int value){
	this.value=value;
    }
    /**
     * @return string representation of integer value
     */
    public String toString(){
	return ""+value;
    }
    /**
     * get the value of the IntegerConstant
     * @return guess what?
     */
    public int getIntegerConst(){
	return value;
    }
    /**
     * an IntegerConstant cannot contain a multiplication
     * @return false
     */
    public boolean hasMultiplication(){
	return false;
    }
   /**
     * an IntegerConstant cannot contain a division
     * @return false
     */
    public boolean hasDivision(){
	return false;
    }
   /**
     * an IntegerConstant is not invertible
     * @return false
     */
    public boolean isInvertible(Variable var){
	return false;
    }
    /**
     * an IntegerConstant is not linear
     * @return false
     */
    public boolean isLinear(){
	return true;
    }
    /**
     * an IntegerConstant cannot contain a method call
     * @return false
     */
    public boolean hasMethodCall(){
	return false;
    }
    /**
     * an IntegerConstant cannot contain an UnknownExpression
     * @return false
     */
    public boolean hasUnknown(){
	return false;
    }  
    /**
     * analysis of an expression
     * @param v the analysing ExpressionVisitor
     */
    public void accept(ExpressionVisitor v){
	v.preVisit(this);
	v.postVisit(this);
    }
    /**
     * degree of an integer value is always 0
     * @return guess what?
     */
    public int getDegree(){
	return 0;
    }
    public void substitute(Variable v, Expression ex){}
    public boolean equals(Object o){
        if (! (o instanceof IntegerConstant)) return false;
        return (((IntegerConstant)o).value == value);
    }

    @Override
    public Type getType() {
        return Int.create();
    }
}

