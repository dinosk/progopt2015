package petter.cfg.expression;
import java.util.Map;
import java.util.HashMap;
import petter.cfg.Annotatable;
/**
 * represents a Variable in an expression
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class Variable implements Expression, Annotatable, java.io.Serializable{
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


    private int id;
    /**
     * create a new Variable     
     * @param id integer value of variable
     */
    private Variable(int id){
	this.id=id;
    }
    public Variable(int id, String externalname
                    //, String scope
        ){
        this(id);
        putAnnotation("external name",externalname);
        //putAnnotation("scope",scope);
    }
    /**
     * the Id of the variable
     * @return guess what?
     */
    public int getId(){
	return id;
    }
    /**
     *
     */
    public int hashCode(){
      return (new Integer(id)).hashCode();
    }
    /**
     * check if two variables are equal
     * @return guess what?
     */
    public boolean equals(Object o){
	if (!(o instanceof Variable)) return false;
   	Variable other= (Variable)o;
	return id==other.id;
    }
    /**
     * the variables name
     * @return guess what?
     */
    public String toString(){
        String name = (String)getAnnotation("external name");
        if (name==null) return "(Variable: #"+id+")";
        else return name;
    }
    /**
     * a variable cannot contain a multiplication
     * @return false
     */
    public boolean hasMultiplication(){
	return false;
    }
    /**
     * a variable cannot contain a division
     * @return false
     */
    public boolean hasDivision(){
	return false;
    }
    /**
     * check if the variable is invertible 
     * @return guess what?
     */
    public boolean isInvertible(Variable var){
	if(this.equals(var)) return true;
	    return false;
    }
   /**
     * a variable is always linear
     * @return true
     */
    public boolean isLinear(){
	return true;
    }
  /**
     * a variable cannot contain a methodCall
     * @return false
     */
    public boolean hasMethodCall(){
	return false;
    }
/**
     * a variable cannot contain an unknown expression
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
     * get the degree of a variable
     * degree of a single variable's always 1
     * @return 1
     */
    public int getDegree(){
	   return 1;
    }
    
    public void substitute(Variable v, Expression ex){}
  

}

