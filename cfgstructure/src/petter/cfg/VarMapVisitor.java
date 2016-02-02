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
    private HashMap<Procedure, HashMap<Integer, Variable>> procVarMap;
    private Procedure current_proc;

    public VarMapVisitor(CompilationUnit cu, HashMap<Procedure, HashMap<Integer, Variable>> procVarMap, Procedure proc){
        super(true); // forward reachability
        this.cu = cu;
        this.procVarMap = procVarMap;
        this.current_proc = proc;
        // System.out.println("VarMapVisitor");
    }

    public boolean visit(Assignment s) {
        // System.out.println("Assignment : " + s.toString());

        s.getLhs().accept(new FindLocalVarsVisitor(this.procVarMap, this.current_proc));

        return true;
    }

}
