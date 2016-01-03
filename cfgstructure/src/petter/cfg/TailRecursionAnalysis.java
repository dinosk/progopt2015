package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;


public class TailRecursionAnalysis extends AbstractPropagatingVisitor<HashSet<Integer>>{

    static HashSet<Integer> lub(HashSet<Integer> b1, HashSet<Integer> b2){
        if (b1==null) return b2;
        if (b2==null) return b1;
        HashSet<Integer> theunion = new HashSet<Integer>();
        theunion.addAll(b1);
        theunion.addAll(b2);
        return theunion;
    }

    CompilationUnit cu;
    ArrayList<String> conditions;
    TransitionFactory tf;
    public TailRecursionAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
        this.conditions = new ArrayList<String>();
        this.tf = new TransitionFactory();
    }

    public HashSet<Integer> visit(Procedure s, HashSet<Integer> b){
        if(b == null) b = new HashSet<Integer>();
        return b;
    }


    public HashSet<Integer> visit(GuardedTransition s, HashSet<Integer> b){
        return b;
    }


    public HashSet<Integer> visit(Assignment s, HashSet<Integer> b){
        if(s.getRhs().hasMethodCall()){
            petter.cfg.expression.MethodCall mc = (petter.cfg.expression.MethodCall) s.getRhs();
            Procedure caller = s.getDest().getMethod();
            Procedure callee = cu.getProcedure(mc.getName());
        }
        return b;
    }

    public HashSet<Integer> visit(MethodCall m, HashSet<Integer> b){
        Procedure caller = m.getDest().getMethod();
        Procedure callee = cu.getProcedure(m.getCallExpression().getName());
        
        ArrayList<State> calleeStates = new ArrayList<State>();
        return b;
    }

    public HashSet<Integer> visit(State s, HashSet<Integer> newflow){
        // System.out.println("Visiting state:"+ s.toString());
        // System.out.println("Is it the last state? "+s.isEnd());
        if(s.isEnd()){
            Iterator<Transition> allIn = s.getInIterator();
            while(allIn.hasNext()){
                if(allIn.next() instanceof MethodCall){
                    System.out.println("========== There is Tail Recursion!");                    
                }
            }
        }
        HashSet<Integer> oldflow = dataflowOf(s);
        newflow = new HashSet<Integer>();
        HashSet<Integer> newval = lub(oldflow, newflow);
        dataflowOf(s, newval);
        return newval;
    }

    // public static void main(String[] args) throws Exception {
    //     CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
    //     TailRecursionAnalysis tr = new TailRecursionAnalysis(cu);
    //     Procedure __main = cu.getProcedure("main");
    //     tr.enter(__main, null);
    //     tr.fullAnalysis();
    //     Iterator<Procedure> allmethods = tr.cu.iterator();
    //     while(allmethods.hasNext()){
    //         Procedure proc = allmethods.next();
    //         DotLayout layout = new DotLayout("jpg", proc.getName()+"After.jpg");
    //         for (State s: proc.getStates()){
    //             layout.highlight(s,(ra.dataflowOf(s))+"");
    //         }
    //         layout.callDot(proc);
    //     }
    // }
}
