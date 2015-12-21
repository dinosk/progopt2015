package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;


public class ReachabilityAnalysis extends AbstractPropagatingVisitor<HashSet<Integer>>{

    static HashSet<Integer> lub(HashSet<Integer> b1, HashSet<Integer> b2){
        if (b1==null) return b2;
        if (b2==null) return b1;
        HashSet<Integer> theunion = new HashSet<Integer>();
        theunion.addAll(b1);
        theunion.addAll(b2);
        return theunion;
    }

    // static HashSet<Integer> lessoreq(HashSet<Integer> b1, HashSet<Integer> b2){
    //     if (b1==null) return new HashSet<Integer>();
    //     if (b2==null) return null;
    //     // return ((!b1) || b2);
    //     return
    // }

    CompilationUnit cu;
    ArrayList<String> conditions; 
    public ReachabilityAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.conditions = new ArrayList<String>();
    }

    public HashSet<Integer> visit(Procedure s, HashSet<Integer> b){
        // System.out.println(Arrays.toString(this.getQueue()));
        System.out.println("Visiting Procedure: "+s.getName());
        if(b == null) b = new HashSet<Integer>();
        // Iterator<Integer> itr = b.iterator();
        // while(itr.hasNext()){
        //     System.out.println(itr.next());
        // }
        return b;
    }


    public HashSet<Integer> visit(GuardedTransition s, HashSet<Integer> b){
        System.out.println("Visiting: if with guard: "+s.getAssertion());
        // System.out.println(Arrays.toString(this.getQueue()));

        System.out.println("b: "+s.getOperator());
        return b;
    }


    public HashSet<Integer> visit(Assignment s, HashSet<Integer> b){
        System.out.println("Visiting assignment: "+s.getLhs()+" = "+s.getRhs());
        // System.out.println(Arrays.toString(this.getQueue()));

        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            enter(cu.getProcedure(mc.getName()), b);
        }
        return b;
    }

    public HashSet<Integer> visit(MethodCall m, HashSet<Integer> b){
        // method calls need special attention; in this case, we just 
        // continue with analysing the next state and triggering the analysis
        // of the callee
        System.out.println("Visiting: MethodCall of: "+m.getCallExpression().getName());
        // System.out.println(Arrays.toString(this.getQueue()));
        
        enter(cu.getProcedure(m.getCallExpression().getName()), null);
        return b;
    }

    public HashSet<Integer> visit(State s, HashSet<Integer> newflow){
        // Iterator<Transition> inEdges = s.getInIterator();
        // while(inEdges.hasNext()){
        //     Transition inEdge = inEdges.next();
        //     System.out.println(inEdge);
        // }
        System.out.println("Visiting state:"+ s.toString());
        // System.out.println(Arrays.toString(this.getQueue()));
        // System.out.println("Is loop seperator: "+s.isLoopSeparator());

        HashSet<Integer> oldflow = dataflowOf(s);
        // System.out.println("oldflow: "+oldflow);
        // System.out.println("newflow: "+newflow);        
        // if (!lessoreq(newflow, oldflow)){
        newflow = new HashSet<Integer>();
        HashSet<Integer> newval = lub(oldflow, newflow);
        dataflowOf(s, newval);
        return newval;
        // }
        // return null;
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        ReachabilityAnalysis ra = new ReachabilityAnalysis(cu);
        Procedure __main = cu.getProcedure("main");
        ra.enter(__main, null);
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
