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
        Iterator<Procedure> allmethods = cu.iterator();
        while(allmethods.hasNext()){
            Procedure nextProc = allmethods.next();
            callGraphBuilder.enter(nextProc);
            tr.enter(nextProc, null);
        }

        HashMap<Procedure, ArrayList<Procedure>> callGraph = callGraphBuilder.getCallGraph();
        ArrayList<Procedure> leafProcs = callGraphBuilder.getLeafProcs();
        if(leafProcs.isEmpty()){
            System.out.println("No leaves found");
        }
        else{
            for(Procedure method : leafProcs){
                System.out.println(method.getName()+" is a leaf");
            }
        }

        Procedure __main = cu.getProcedure("main");
        System.out.println("------------ Starting InliningAnalysis 1/2 ------------");
        InliningAnalysis ra = new InliningAnalysis(cu, leafProcs);
        ra.enter(__main, null);
        ra.fullAnalysis();
        System.out.println("------------ Starting TailRecursionAnalysis 2/2 ------------");
        tr.fullAnalysis();        

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