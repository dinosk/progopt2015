package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.UnaryExpression;


public class ConstantTransformation extends AbstractVisitor{
    CompilationUnit cu;
    TransitionFactory tf;
    ArrayList<State> visited;
    HashMap<String, HashMap<Variable, IntegerConstant>> constantsMap;
    ConstantPropagationAnalysis copyprop;

    public ConstantTransformation(CompilationUnit cu, ConstantPropagationAnalysis copyprop){
        super(true); // forward reachability
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.visited = new ArrayList<State>();
        this.copyprop = copyprop;
    }

    public IntegerConstant findVar(Variable x){
        if(constantsMap.get("local").keySet().contains(x)){
            return constantsMap.get("local").get(x);
        }
        else if(constantsMap.get("global").keySet().contains(x)){
            return constantsMap.get("global").get(x);
        }
        return null;
    }

    public boolean visit(Assignment s){
        constantsMap = copyprop.dataflowOf(s.getSource());
        IntegerConstant knownConstant = null;
        if(s.getRhs() instanceof Variable){
            Variable y = (Variable) s.getRhs();

            knownConstant = findVar(y);
            if(knownConstant != null){
                s.removeEdge();
                Assignment newEdge = (Assignment) this.tf.createAssignment(s.getSource(), s.getDest(), s.getLhs(), knownConstant);
                s.getSource().addOutEdge(newEdge);
                s.getSource().getMethod().resetTransitions();
            }
        }
        else if(s.getRhs() instanceof UnaryExpression){
            UnaryExpression uexpr = (UnaryExpression) s.getRhs();
            if(uexpr.getExpression() instanceof Variable){
                Variable y = (Variable) uexpr.getExpression();
                knownConstant = findVar(y);
                if(knownConstant != null){
                    s.removeEdge();
                    Assignment newEdge = (Assignment) this.tf.createAssignment(s.getSource(), s.getDest(), s.getLhs(), knownConstant);
                    s.getSource().addOutEdge(newEdge);
                    s.getSource().getMethod().resetTransitions();
                }       
            }
        }
        return true;
    }

    // public boolean visit(GuardedTransition s){

    // }

}
