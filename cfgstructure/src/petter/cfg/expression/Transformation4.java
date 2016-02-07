package petter.cfg.expression;
import petter.cfg.*;
import petter.cfg.edges.*;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.Variable;
import java.util.HashMap;
/**
 * provides an abstract class to visit an expression;
 * the visitor performs a run through the whole expression, as long as it's visit methods return true;
 * to terminate you have to ensure that the return value of a visit method becomes false at some point;
 * @see ExpressionVisitor
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class Transformation4 extends AbstractExpressionVisitor{
    /**
     * provides a standardized return value for all not overwritten visit methods.
     * the default value here is true, meaning, that a not overwritten visit method just passes on to the next item in the queue
     * @param  e item, which is visited; can be ignored
     * @return a default value for visit methods
     */

    public IntegerConstant findVar(Variable x){
        if(constantsMap.get("local").keySet().contains(x)){
            return constantsMap.get("local").get(x);
        }   
        else if(constantsMap.get("global").keySet().contains(x)){
            return constantsMap.get("global").get(x);
        }
        return null;
    }

    public void setConstantsMap(HashMap<String, HashMap<Variable, IntegerConstant>> constantsMap){
        this.constantsMap = constantsMap;
    }   

    private HashMap<String, HashMap<Variable, IntegerConstant>> constantsMap;
    
    public boolean preVisit(MethodCall s){return defaultBehaviour(s);}
    public boolean preVisit(UnknownExpression s){return defaultBehaviour(s);}
    public boolean preVisit(UnaryExpression s){return defaultBehaviour(s);}

    public boolean preVisit(BinaryExpression s){
        if(s.getLeft() instanceof Variable){
            Variable y = (Variable) s.getLeft();

            IntegerConstant knownConstant = findVar(y);
            if(knownConstant != null){
                s.setLeft(knownConstant);
            }
        }

        if(s.getRight() instanceof Variable){
            Variable y = (Variable) s.getRight();

            IntegerConstant knownConstant = findVar(y);
            if(knownConstant != null){
                s.setRight(knownConstant);
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
