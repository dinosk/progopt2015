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
    boolean constant_folding(Expression a, Expression b, Operator op){
        IntegerConstant ac = null;
        IntegerConstant bc = null;
        
        if(a instanceof IntegerConstant)
            ac = (IntegerConstant) a;
        else if(a instanceof Variable && this.map.get("local").get(a) != null)
            ac = this.map.get("local").get(a);
        else if(a instanceof Variable && this.map.get("global").get(a) != null)
            ac = this.map.get("global").get(a);
        else return false;
        
        if(b instanceof IntegerConstant)
            bc = (IntegerConstant) b;
        else if(b instanceof Variable && this.map.get("local").get(b) != null)
            bc = this.map.get("local").get(b);
        else if(b instanceof Variable && this.map.get("global").get(b) != null)
            bc = this.map.get("global").get(b);
        else return false;

        if(ac != null && bc != null){
            if(op.toString().equals("+"))
                constants.add(new IntegerConstant(ac.getIntegerConst() + bc.getIntegerConst()));
            else if(op.toString().equals("-"))
                constants.add(new IntegerConstant(ac.getIntegerConst() - bc.getIntegerConst()));
            else if(op.toString().equals("*"))
                constants.add(new IntegerConstant(ac.getIntegerConst() * bc.getIntegerConst()));
            else if(op.toString().equals("/"))
                constants.add(new IntegerConstant(ac.getIntegerConst() / bc.getIntegerConst()));
            // else if(op.toString().equals("/")
            //     constants.add(ac.getIntegerConst() / bc.getIntegerConst());
            System.out.println("\nOP: "+op+"\n");
            return true;
        }        
        return false;
    }

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
        System.out.println("Visiting "+s.toString()+" current map: "+this.constants);
        if(map.get("local").get(s) != null){
            constants.add(map.get("local").get(s));
            // s.substitute(s, map.get("local").get(s));
        }
        else if(map.get("global").get(s) != null){
            constants.add(map.get("global").get(s));
            // s.substitute(s, map.get("global").get(s));
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
        if(s.getExpression() instanceof Variable){
            if(map.get("local").get(s.getExpression()) != null){
                constants.add(map.get("local").get(s));
                // s.substitute(s, map.get("local").get(s));
            }
            else if(map.get("global").get(s) != null){
                constants.add(map.get("global").get(s));
                // s.substitute(s, map.get("global").get(s));
            }
        }
        return defaultBehaviour(s);
    }
    
    public boolean preVisit(BinaryExpression s){
        // System.out.println("Visiting BinaryExpression: "+s.toString()+" current map: "+this.constants);
        // Expression left = s.getLeft();
        // Expression right = s.getRight();
        // left.accept(this);
        // right.accept(this);
        // Operator op = s.getOperator();
        // constant_folding(left, right, op);
        return defaultBehaviour(s);
    }

    public void postVisit(IntegerConstant s){}
    public void postVisit(Variable s){}
    public void postVisit(MethodCall s){}
    public void postVisit(UnknownExpression s){}
    public void postVisit(UnaryExpression s){}
    public void postVisit(BinaryExpression s){}
}
