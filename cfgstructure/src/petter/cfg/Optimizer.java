package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;

public class Optimizer{
	public static void main(String[] args) throws Exception {
	    CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
	    CallGraphBuilder callGraphBuilder = new CallGraphBuilder(cu);
	    
	    Iterator<Procedure> allmethods = cu.iterator();        
	    while(allmethods.hasNext()){
	    	callGraphBuilder.enter(allmethods.next());
	    }

	    HashMap<Procedure, ArrayList<Procedure>> callGraph = callGraphBuilder.getCallGraph();
	    for(Procedure method : callGraph.keySet()){
	    	System.out.println(method.getName()+" calls: "+callGraph.get(method));
	    }
	    InliningAnalysis ra = new InliningAnalysis(cu);
	    Procedure __main = cu.getProcedure("main");
	    // ra.enter(__main, null);
	    // ra.fullAnalysis();
	    // allmethods = ra.cu.iterator();        
	    // while(allmethods.hasNext()){
	    //     Procedure proc = allmethods.next();
	    //     DotLayout layout = new DotLayout("jpg", proc.getName()+"After.jpg");
	    //     for (State s: proc.getStates()){
	    //         layout.highlight(s,(ra.dataflowOf(s))+"");
	    //     }
	    //     layout.callDot(proc);
	    // }
	}
}