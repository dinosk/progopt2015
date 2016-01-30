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
public class ConstantFindingVisitor extends AbstractExpressionVisitor{
    /**
     * provides a standardized return value for all not overwritten visit methods.
     * the default value here is true, meaning, that a not overwritten visit method just passes on to the next item in the queue
     * @param  e item, which is visited; can be ignored
     * @return a default value for visit methods
     */

    private HashSet<IntegerConstant> constants;
    private HashMap<String, HashMap<Variable, IntegerConstant>> map;
    public ConstantFindingVisitor(HashMap<String, HashMap<Variable, IntegerConstant>> b){
        this.map = b;
        this.constants = new HashSet<IntegerConstant>();
    }

    public HashSet<IntegerConstant> getConstants(){
        return this.constants;
    }

    protected boolean defaultBehaviour(Expression e){
        return true;
    }

    public boolean preVisit(IntegerConstant s){
        constants.add(s);
        return true;
    }

    public boolean preVisit(Variable s){
        if(map.get("local").get(s) != null){
            s.substitute(s, map.get("local").get(s));
        }
        else if(map.get("global").get(s) != null){
            s.substitute(s, map.get("global").get(s));
        }
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
        if(map.get("local").get(s) != null){
            // s.substitute(s, map.get("local").get(s));
        }
        else if(map.get("global").get(s) != null){
            // s.substitute(s, map.get("global").get(s));
        }
        return defaultBehaviour(s);
    }
    
    public boolean preVisit(BinaryExpression s){
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
    }

    public void postVisit(IntegerConstant s){}
    public void postVisit(Variable s){}
    public void postVisit(MethodCall s){}
    public void postVisit(UnknownExpression s){}
    public void postVisit(UnaryExpression s){}
    public void postVisit(BinaryExpression s){}
}
