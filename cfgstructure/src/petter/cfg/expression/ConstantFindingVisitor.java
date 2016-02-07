package petter.cfg.expression;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.util.*;
/**
 * provides an abstract class to visit an expression;
 * the visitor performs a run through the whole expression, as long as it's visit methods return true;
 * to terminate you have to ensure that the return value of a visit method becomes false at some point;
 */
public class ConstantFindingVisitor extends AbstractExpressionVisitor{
    /**
     * provides a standardized return value for all not overwritten visit methods.
     * the default value here is true, meaning, that a not overwritten visit method just passes on to the next item in the queue
     * @param  e item, which is visited; can be ignored
     * @return a default value for visit methods
     */    	    

    public IntegerConstant getValue(Expression x){
    	if(x instanceof Variable){
    		if(map.get("local").get((Variable) x) != null)
    			return map.get("local").get((Variable) x);
    		else if(map.get("global").get((Variable) x) != null){
                return map.get("global").get((Variable) x);  
            }
            else if(isFormal((Variable) x)){
                return formalParams.get(this.currProc.getFormalParameters().indexOf(((Variable) x).getId()));
            }
        }
    	else if(x instanceof IntegerConstant)
    		return (IntegerConstant) x;
    	return null;
    }

    public void setFormals(HashMap<Integer, IntegerConstant> formals){
        this.formalParams = formals;
    }

    public void setProc(Procedure currProc){
        this.currProc = currProc;
    }

    public boolean isFormal(Variable var){
        if(formalParams != null){
            return this.currProc.getFormalParameters().contains(var.getId());
        }
        return false;
    }

    public boolean isFormal(int id){
        if(formalParams != null)
            return this.currProc.getFormalParameters().contains(id);
        return false;
    }

    Procedure currProc;
    HashMap<Integer, IntegerConstant> formalParams;
    private IntegerConstant constant;
    private HashMap<String, HashMap<Variable, IntegerConstant>> map;
    public ConstantFindingVisitor(HashMap<String, HashMap<Variable, IntegerConstant>> b){
        this.map = b;
        this.constant = null;
    }

    public IntegerConstant getConstant(){
        return this.constant;
    }

    protected boolean defaultBehaviour(Expression e){
        return true;
    }

    public boolean preVisit(IntegerConstant s){
        return true;
    }

    public boolean preVisit(Variable s){
        return true;
    }
    
    public boolean preVisit(MethodCall s){
        return true;
    }

    public boolean preVisit(UnknownExpression s){return defaultBehaviour(s);}
    public boolean preVisit(UnaryExpression s){
        if(s.getExpression() instanceof Variable){
            if(map.get("local").get((Variable) s.getExpression()) != null){
                constant = map.get("local").get(s);
            }
            else if(map.get("global").get((Variable) s.getExpression()) != null){
                constant = map.get("global").get(s);
            }
            // this joke here checks if the var is a formal
            else if(isFormal(((Variable) s.getExpression()).getId())){
                constant = map.get("global").get(s);
            }
        }
        return defaultBehaviour(s);
    }
    
    public boolean preVisit(BinaryExpression s){
        if((s.getLeft() instanceof Variable  || s.getLeft() instanceof IntegerConstant) &&
           (s.getRight() instanceof Variable || s.getRight() instanceof IntegerConstant))
            return defaultBehaviour(s);
        else
            return false;
    }

    public void postVisit(IntegerConstant s){}
    public void postVisit(Variable s){}
    public void postVisit(MethodCall s){}
    public void postVisit(UnknownExpression s){}
    public void postVisit(UnaryExpression s){}
    public void postVisit(BinaryExpression s){
    	IntegerConstant a = getValue(s.getLeft());
    	IntegerConstant b = getValue(s.getRight());
        Operator op = s.getOperator();
    	if(a != null && b != null){
            if(op.toString().equals("+"))
                constant = new IntegerConstant(a.getIntegerConst() + b.getIntegerConst());
            else if(op.toString().equals("-"))
                constant = new IntegerConstant(a.getIntegerConst() - b.getIntegerConst());
            else if(op.toString().equals("*"))
                constant = new IntegerConstant(a.getIntegerConst() * b.getIntegerConst());
    	}
    }
}
