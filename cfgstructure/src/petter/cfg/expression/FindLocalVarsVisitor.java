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
public class FindLocalVarsVisitor extends AbstractExpressionVisitor {

    private HashMap<Procedure, HashMap<Integer, Variable>> idToVarMap;
    private Procedure proc;

    public FindLocalVarsVisitor(HashMap<Procedure, HashMap<Integer, Variable>> idToVarMap, Procedure proc) {
        this.idToVarMap = idToVarMap;
        this.proc = proc;
    }

    public boolean preVisit(Variable s) {
        // System.out.println("Variable in FindVisitor in proc: " + s.toString() + " " + proc.getName());
        if(!s.toString().startsWith("$")) {
            if(this.proc.getLocalVariables().contains(s.getId())){
                // System.out.println("Einai local var h: " + s);
                int id = s.getId();
                if(!this.idToVarMap.get(this.proc).containsKey(id)) {
                    // System.out.println("Vazw sto map : " + id + " " + s);
                    this.idToVarMap.get(this.proc).put(id, s);
                }
            }
        }
        return true;
    }

}
