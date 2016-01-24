package petter.cfg;

import java.io.*;
import java.util.*;

import petter.cfg.*;
import petter.cfg.edges.*;
// import petter.cfg.expression.Expression;
// import petter.cfg.expression.Variable;
// import petter.cfg.expression.RenamingVisitor;
// import petter.cfg.expression.MethodCall;


public class NumOfCallsVisitor extends AbstractVisitor{

    private CompilationUnit cu;
    // private TransitionFactory tf;
    private HashMap<String, Integer> procCalls;

    public NumOfCallsVisitor(CompilationUnit cu, HashMap<String, Integer> procCalls){
        super(true); // forward reachability
        this.cu = cu;
        // this.tf = new TransitionFactory();
        this.procCalls = procCalls;
        System.out.println("NumOfCallsVisitor");
    }

    public HashMap<String, Integer> getProcCalls() {
        return this.procCalls;
    }

    public boolean visit(Assignment s) {
        if(s.getRhs().hasMethodCall()) {
            petter.cfg.expression.MethodCall meth_call = (petter.cfg.expression.MethodCall) s.getRhs();
            int count = this.procCalls.get(meth_call.getName()) + 1;
            System.out.println("Assign : " + count);
            this.procCalls.put(meth_call.getName(), count);
        }
        return true;
    }

    public boolean visit(MethodCall s) {
        int count = this.procCalls.get(s.getCallExpression().getName()) + 1;
        System.out.println("MethodCall : " + count);
        this.procCalls.put(s.getCallExpression().getName(), count);
        // this.procCalls.put(s.getCallExpression().getName(),  this.procCalls.get(s.getCallExpression().getName()+1);
        return true;
    }

}
