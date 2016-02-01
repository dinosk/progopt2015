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

    // boolean constant_folding(Expression a, Expression b, Operator op){
    //     IntegerConstant ac = null;
    //     IntegerConstant bc = null;
        
    //     if(a instanceof IntegerConstant)
    //         ac = (IntegerConstant) a;
    //     else if(a instanceof Variable && this.map.get("local").get(a) != null)
    //         ac = this.map.get("local").get(a);
    //     else if(a instanceof Variable && this.map.get("global").get(a) != null)
    //         ac = this.map.get("global").get(a);
    //     else return false;
        
    //     if(b instanceof IntegerConstant)
    //         bc = (IntegerConstant) b;
    //     else if(b instanceof Variable && this.map.get("local").get(b) != null)
    //         bc = this.map.get("local").get(b);
    //     else if(b instanceof Variable && this.map.get("global").get(b) != null)
    //         bc = this.map.get("global").get(b);
    //     else return false;

    //     if(ac != null && bc != null){
    //         if(op.toString().equals("+"))
    //             constants.add(new IntegerConstant(ac.getIntegerConst() + bc.getIntegerConst()));
    //         else if(op.toString().equals("-"))
    //             constants.add(new IntegerConstant(ac.getIntegerConst() - bc.getIntegerConst()));
    //         else if(op.toString().equals("*"))
    //             constants.add(new IntegerConstant(ac.getIntegerConst() * bc.getIntegerConst()));
    //         else if(op.toString().equals("/"))
    //             constants.add(new IntegerConstant(ac.getIntegerConst() / bc.getIntegerConst()));
    //         // else if(op.toString().equals("/")
    //         //     constants.add(ac.getIntegerConst() / bc.getIntegerConst());
    //         System.out.println("\nOP: "+op+"\n");
    //         return true;
    //     }        
    //     return false;
    // }

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
        // System.out.println("Formal parameters for cfv set to: "+formals);
        this.formalParams = formals;
    }

    public void setProc(Procedure currProc){
        this.currProc = currProc;
    }

    public boolean isFormal(Variable var){
        if(formalParams != null){
            // System.out.println("checking if "+var.getId()+" is in "+currProc.getFormalParameters());
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
        // System.out.println("Visiting an IntegerConstant");
        return true;
    }

    public boolean preVisit(Variable s){
        // System.out.println("Visiting "+s.toString()+" current map: "+this.map);
        // if(map.get("local").get(s) != null){
        //     // constants.add(map.get("local").get(s));
        //     s.substitute(s, map.get("local").get(s));
        //     System.out.println("substituted!");
        // }
        // else if(map.get("global").get(s) != null){
        //     // constants.add(map.get("global").get(s));
        //     s.substitute(s, map.get("global").get(s));
        //     System.out.println("substituted!");
        // }
        return defaultBehaviour(s);
    }
    
    public boolean preVisit(MethodCall s){
        // if(map.get("local").get(s.getName()+"()") != null){
        //     s.substitute(s, map.get("local").get(s.getName()+"()"));
        // }
        // else if(map.get("global").get(s.getName()+"()") != null){
        //     s.substitute(s, map.get("global").get(s.getName()+"()"));
        // }
        return defaultBehaviour(s);
    }

    public boolean preVisit(UnknownExpression s){return defaultBehaviour(s);}
    public boolean preVisit(UnaryExpression s){
<<<<<<< HEAD
        if(s.getExpression() instanceof Variable){
            if(map.get("local").get((Variable) s.getExpression()) != null){
                constant = map.get("local").get(s);
                // s.substitute(s, map.get("local").get(s));
            }
            else if(map.get("global").get((Variable) s.getExpression()) != null){
                constant = map.get("global").get(s);
                // s.substitute(s, map.get("global").get(s));
            }
            // this joke here checks if the var is a formal
            else if(isFormal(((Variable) s.getExpression()).getId())){
                constant = map.get("global").get(s);
                // s.substitute(s, map.get("global").get(s));
            }
=======
        if(map.get("local").get(s) != null){
            // s.substitute(s, map.get("local").get(s));
        }
        else if(map.get("global").get(s) != null){
            // s.substitute(s, map.get("global").get(s));
>>>>>>> da057483376d0f98d6225d69f2580ab995d92169
        }
        return defaultBehaviour(s);
    }
    
    public boolean preVisit(BinaryExpression s){
<<<<<<< HEAD
        // System.out.println("Visiting a BinaryExpression");
        if((s.getLeft() instanceof Variable  || s.getLeft() instanceof IntegerConstant) &&
           (s.getRight() instanceof Variable || s.getRight() instanceof IntegerConstant))
            return defaultBehaviour(s);
        else
            return false;
=======
        Expression left = s.getLeft();
        Expression right = s.getRight();

        if(left instanceof Variable){
            Variable leftVar = (Variable) left;
            if(this.map.get(leftVar) != null){
                // s.substitute(leftVar, this.map.get(leftVar));
            }
        }

        if(right instanceof Variable){
            Variable rightVar = (Variable) right;
            if(this.map.get(rightVar) != null){
                // s.substitute(rightVar, this.map.get(rightVar));
            }
        }

        return defaultBehaviour(s);
>>>>>>> da057483376d0f98d6225d69f2580ab995d92169
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

        System.out.println("a : "+a);
        System.out.println("b : "+b);
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
