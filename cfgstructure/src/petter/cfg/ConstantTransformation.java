package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.BinaryExpression;
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

    public void makeUnreachable(State s){
        Iterator<Transition> outEdges = s.getOutIterator();
        while(outEdges.hasNext()){
            Transition next = outEdges.next();
            outEdges.remove();
            // s.deleteOutEdge(next);
            // System.out.println("In degree for "+next.getDest()+" after deleting: "+next.getDest().getInDegree());
            if(next.getDest().getInDegree() == 1){
                makeUnreachable(next.getDest());
            }
        }
    }

    public boolean visit(Procedure s){
        this.visited.clear();
        return true;
    }

    public boolean visit(GuardedTransition s){
        if(s.getAssertion() instanceof BinaryExpression){
            BinaryExpression ifGuard = (BinaryExpression) s.getAssertion();
            IntegerConstant left = null;
            IntegerConstant right = null;
            Integer result = null;
            boolean always1 = false;
            boolean always0 = false;

            constantsMap = copyprop.dataflowOf(s.getSource());
            if(ifGuard.getLeft() instanceof Variable){
                Variable leftVar = (Variable) ifGuard.getLeft();
                if(findVar(leftVar) != null){
                    left = findVar(leftVar);
                }
            }
            else if(ifGuard.getLeft() instanceof IntegerConstant){
                left = (IntegerConstant) ifGuard.getLeft();
            }
            if(ifGuard.getRight() instanceof Variable){
                Variable rightVar = (Variable) ifGuard.getRight();
                if(findVar(rightVar) != null){
                    right = findVar(rightVar);
                }
            }
            else if(ifGuard.getRight() instanceof IntegerConstant){
                right = (IntegerConstant) ifGuard.getRight();
            }
            if(left != null && right != null){
                if(ifGuard.getOperator().toString().equals("+")){
                    result = left.getIntegerConst() + right.getIntegerConst();
                }
                else if(ifGuard.getOperator().toString().equals("*")){
                    result = left.getIntegerConst() * right.getIntegerConst();
                }
                else if(ifGuard.getOperator().toString().equals("-")){
                    result = left.getIntegerConst() - right.getIntegerConst();
                }
                else if(ifGuard.getOperator().toString().equals("/")){
                    result = left.getIntegerConst() / right.getIntegerConst();
                }
            }

            if(result != null){
                if(s.getOperator().toString().equals("==")){
                    if(result == 0){
                        always1 = true;
                    }
                    else{
                        always0 = true;
                    }
                }
                else if(s.getOperator().toString().equals("!=")){
                    if(result != 0){
                        always1 = true;
                    }
                    else{
                        always0 = true;
                    }
                }
                else if(s.getOperator().toString().equals(">")){
                    if(result > 0){
                        always1 = true;
                    }
                    else{
                        always0 = true;
                    }
                }
                else if(s.getOperator().toString().equals("<")){
                    if(result < 0){
                        always1 = true;
                    }
                    else{
                        always0 = true;
                    }
                }
            }
            if(always1){
                // System.out.println("Guard"+s+" is always1");
                s.removeEdge();
                Nop nop = (Nop) this.tf.createNop(s.getSource(), s.getDest());
                s.getSource().addOutEdge(nop);
            }
            else if(always0){
                // System.out.println("Guard"+s+" is always0");
                s.removeEdge();
                makeUnreachable(s.getDest());
            }
        }
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
