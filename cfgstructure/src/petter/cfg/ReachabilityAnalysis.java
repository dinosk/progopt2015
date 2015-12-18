package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;


public class ReachabilityAnalysis extends AbstractPropagatingVisitor<Boolean>{

    static Boolean lub(Boolean b1,Boolean b2){
        if (b1==null) return b2;
        if (b2==null) return b1;
        return b1||b2;
    }
    static boolean lessoreq(Boolean b1,Boolean b2){
        if (b1==null) return true;
        if (b2==null) return false;
        return ((!b1) || b2);
    }

    CompilationUnit cu;
    public ReachabilityAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
    }
    public Boolean visit(MethodCall m,Boolean b){
        // method calls need special attention; in this case, we just 
        // continue with analysing the next state and triggering the analysis
        // of the callee
        enter(cu.getProcedure(m.getCallExpression().getName()),true);
        return b;
    }
    public Boolean visit(State s,Boolean newflow){
        Boolean oldflow = dataflowOf(s);
        if (!lessoreq(newflow,oldflow)){
            Boolean newval = lub(oldflow,newflow);
            dataflowOf(s,newval);
            return newval;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(args[0]);
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        ReachabilityAnalysis ra = new ReachabilityAnalysis(cu);
        Iterator<Procedure> allmethods = cu.iterator();
        while(allmethods.hasNext()){
            Procedure proc = allmethods.next();
            ra.enter(proc,true);
            ra.fullAnalysis();
            DotLayout layout = new DotLayout("jpg", proc.getName()+".jpg");
            for (State s: proc.getStates()){
                layout.highlight(s,(ra.dataflowOf(s))+"");
            }
            layout.callDot(proc);
        }
    }
}
