package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;

public class OptimizerAnalysis{
    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        System.out.println("----------- Building CallGraph 1/2 -----------");
        CallGraphBuilder callGraphBuilder = new CallGraphBuilder(cu);
        Iterator<Procedure> allmethods = cu.iterator();        
        while(allmethods.hasNext()){
            callGraphBuilder.enter(allmethods.next());
        }

        HashMap<Procedure, ArrayList<Procedure>> callGraph = callGraphBuilder.getCallGraph();
        for(Procedure method : callGraph.keySet()){
            System.out.println(method.getName()+" calls: "+callGraph.get(method));
        }

        // InliningAnalysis ra = new InliningAnalysis(cu);
        System.out.println("----------- Starting TailRecursionAnalysis 2/2 -----------");
        TailRecursionAnalysis tr = new TailRecursionAnalysis(cu);
        allmethods = cu.iterator();        
        while(allmethods.hasNext()){
            tr.enter(allmethods.next(), null);
        }
        tr.fullAnalysis();
    }
}