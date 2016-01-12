package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;

public class OptimizerAnalysis{
    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        CallGraphBuilder callGraphBuilder = new CallGraphBuilder(cu);
        TailRecursionAnalysis tr = new TailRecursionAnalysis(cu);
        ConstantPropagationAnalysis cpa = new ConstantPropagationAnalysis(cu);

        Iterator<Procedure> allmethods = cu.iterator();
        while(allmethods.hasNext()){
            Procedure nextProc = allmethods.next();
            callGraphBuilder.enter(nextProc);
            tr.enter(nextProc, null);
            cpa.enter(nextProc, null);
        }

        ArrayList<Procedure> leafProcs = callGraphBuilder.getLeafProcs();
        HashMap<Procedure, ArrayList<Procedure>> callGraph = callGraphBuilder.getCallGraph();
        if(leafProcs.isEmpty()){
            System.out.println("No leaves found");
        }
        else{
            for(Procedure method : leafProcs){
                System.out.println(method.getName()+" is a leaf");
            }
        }

        Procedure __main = cu.getProcedure("main");
        System.out.println("------------ Starting InliningAnalysis 1/3 ------------");
        InliningAnalysis ia = new InliningAnalysis(cu, leafProcs);
        allmethods = cu.iterator();
        while(allmethods.hasNext()){
            ia.enter(allmethods.next());
        }
        ia.fullAnalysis();
        System.out.println("------------ Starting TailRecursionAnalysis 2/3 ------------");
        tr.fullAnalysis();     
        System.out.println("------------ Starting ConstantPropagationAnalysis 3/3 ------------");
        cpa.fullAnalysis();


        allmethods = cu.iterator();
        while(allmethods.hasNext()){
            Procedure proc = allmethods.next();
            DotLayout layout = new DotLayout("jpg", proc.getName()+"After.jpg");
            // for (State s: proc.getStates()){
            //     layout.highlight(s,(ra.dataflowOf(s))+"");
            // }
            layout.callDot(proc);
        }
    }
}