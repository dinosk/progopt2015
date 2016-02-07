package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.Transformation4;


public class ConstantTransformation extends AbstractVisitor{
    CompilationUnit cu;
    TransitionFactory tf;
    ArrayList<State> visited;
    HashMap<String, HashMap<Variable, IntegerConstant>> constantsMap;
    ConstantPropagationAnalysis copyprop;
    Transformation4 t4;

    public ConstantTransformation(CompilationUnit cu, ConstantPropagationAnalysis copyprop){
        super(true); // forward reachability
        this.cu=cu;
        this.tf = new TransitionFactory();
        this.visited = new ArrayList<State>();
        this.copyprop = copyprop;
        this.t4 = new Transformation4();
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

    public boolean visit(Procedure s){
        this.visited.clear();
        return true;
    }

    public boolean visit(Assignment s){
        // System.out.println("Visiting assignment "+s);
        constantsMap = copyprop.dataflowOf(s.getSource());
        IntegerConstant knownConstant = null;
        if(s.getRhs() instanceof Variable){
            Variable y = (Variable) s.getRhs();

            knownConstant = findVar(y);
            if(knownConstant != null){
                s.removeEdge();
                Assignment newEdge = (Assignment) this.tf.createAssignment(s.getSource(), s.getDest(), s.getLhs(), knownConstant);
                // System.out.println("Edge "+s+" removed! Adding "+newEdge+" between "+s.getSource()+" and "+s.getDest());
                s.getSource().addOutEdge(newEdge);
                // System.out.println("Transitions reset.");
            }
        }
        else{
            t4.setConstantsMap(constantsMap);
            s.getRhs().accept(t4);
        }
        return true;
    }

    public boolean visit(State s){
        if(visited.contains(s))return false;
        // System.out.println("Visiting "+s);
        if(s.isEnd()){
            s.getMethod().resetTransitions();
            return false;
        }
        visited.add(s);
        return true;
    }

}
