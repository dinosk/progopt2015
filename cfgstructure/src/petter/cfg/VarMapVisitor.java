package petter.cfg;

import java.io.*;
import java.util.*;

import petter.cfg.*;
import petter.cfg.edges.*;
// import petter.cfg.edges;
// import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.FindLocalVarsVisitor;
// import petter.cfg.expression.MethodCall;


public class VarMapVisitor extends AbstractVisitor{

    private CompilationUnit cu;
    // private HashMap<Integer, Variable> idToVarMap;
    private HashMap<Procedure, HashMap<Integer, Variable>> procVarMap;
    private Procedure current_proc;

    public VarMapVisitor(CompilationUnit cu, HashMap<Procedure, HashMap<Integer, Variable>> procVarMap, Procedure proc){
        super(true); // forward reachability
        this.cu = cu;
        // this.idToVarMap = new HashMap<Integer, Variable>();
        // this.idToVarMap = null;
        this.procVarMap = procVarMap;
        this.current_proc = proc;
        System.out.println("VarMapVisitor");
    }

    // public HashMap<String, HashMap<Integer, Variable>> getprocVarMap() {
    //     return this.procVarMap;
    // }

    public boolean visit(Assignment s) {
        System.out.println("Assignment : " + s.toString());

        s.getLhs().accept(new FindLocalVarsVisitor(this.procVarMap, this.current_proc));

        return true;
    }

    // public boolean visit(MethodCall s) {
    //     int count = this.procCalls.get(s.getCallExpression().getName()) + 1;
    //     System.out.println("MethodCall : " + count);
    //     this.procCalls.put(s.getCallExpression().getName(), count);
    //     // this.procCalls.put(s.getCallExpression().getName(),  this.procCalls.get(s.getCallExpression().getName()+1);
    //     return true;
    // }
    // public boolean visit(GuardedTransition s) {
    //     s.accept(new FindLocalVarsVisitor(this.idToVarMap));
    //     return true;
    // }

    // public boolean visit(Procedure s) {
    //     // if(this.idToVarMap == null) {
    //     this.current_proc = s;
    //     System.out.println("Procedure: " + s.getName());
    //     // HashMap<Integer, Variable> v = new HashMap<Integer, Variable>();
    //     this.procVarMap.put(s.getName(), new HashMap<Integer, Variable>());
    //     // }
    //     return false;
    // }

}
