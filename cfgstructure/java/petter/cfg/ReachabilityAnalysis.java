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
        System.out.println("visiting: "+m.getCallExpression().getName());
        enter(cu.getProcedure(m.getCallExpression().getName()),true);
        return b;
    }
    public Boolean visit(State s,Boolean newflow){
        
        Iterator<Transition> inEdges = s.getInIterator();
        System.out.println("at State: "+s.toString()+" with edges:");
        while(inEdges.hasNext()){
            Transition inEdge = inEdges.next();
            System.out.println(inEdge);
        }

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
        //Iterator<Procedure> allmethods = cu.iterator();
        Procedure __main = cu.getProcedure("main");
//        while(allmethods.hasNext()){
  //          Procedure proc = allmethods.next();
    //        DotLayout layout = new DotLayout("jpg", proc.getName()+"Before.jpg");
      //      for (State s: proc.getStates()){
        //        layout.highlight(s,(ra.dataflowOf(s))+"");
          //  }
            //layout.callDot(proc);
        //}
        ra.enter(__main,true);
        ra.fullAnalysis();
        Iterator<Procedure> allmethods = ra.cu.iterator();        
        while(allmethods.hasNext()){
            Procedure proc = allmethods.next();
            DotLayout layout = new DotLayout("jpg", proc.getName()+"After.jpg");
            for (State s: proc.getStates()){
                layout.highlight(s,(ra.dataflowOf(s))+"");
            }
            layout.callDot(proc);
        }
    }
}
