package petter.cfg;

import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.*;

public class OptimizerAnalysis{

    public static void main(String[] args) throws Exception {

        int numOfCallsCount = 10; //take it as an argumet from command line?
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        ArrayList<Procedure> worklist = new ArrayList<Procedure>();
        CallGraphBuilder callGraphBuilder = new CallGraphBuilder(cu);
        TailRecursionAnalysis tr = new TailRecursionAnalysis(cu);
        ConstantPropagationAnalysis cpa = new ConstantPropagationAnalysis(cu);
        // CopyPropagationAnalysis copyprop = new CopyPropagationAnalysis(cu);

        

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
        // get all function names of a compilation unit and then inline according to the #ofCalls
        HashMap<String, Integer> procCalls = new HashMap<String, Integer>();
        for(String methodName : cu.getProcedures().keySet()) {
            procCalls.put(methodName, 0);
        }
        NumOfCallsVisitor callsVisitor = new NumOfCallsVisitor(cu, procCalls);
        Iterator<Procedure> allmethods = cu.iterator();
        while(allmethods.hasNext()){
            callsVisitor.enter(allmethods.next());
        }
        //callsVisitor.fullAnalysis();
        // callsVisitor.enter(__main);
        // callsVisitor.fullAnalysis();
        ArrayList<Procedure> numOfCalls = new ArrayList<Procedure>();
        for(String methodName : callsVisitor.getProcCalls().keySet()) {
            if(callsVisitor.getProcCalls().get(methodName) <= numOfCallsCount) {
                numOfCalls.add(cu.getProcedures().get(methodName));
                System.out.println("Add " + methodName + " " + callsVisitor.getProcCalls().get(methodName));
            }
        }
        InliningAnalysis ia1 = new InliningAnalysis(cu, numOfCalls);
        System.out.println("------------ Starting InliningAnalysis 1/4 ------------");
        allmethods = cu.iterator();
        while(allmethods.hasNext()){
            ia1.enter(allmethods.next());
        }
		//ia1.fullAnalysis();
		InliningAnalysis ia = new InliningAnalysis(cu, leafProcs);
        // ia.fullAnalysis();
        System.out.println("------------ Starting TailRecursionAnalysis 2/3 ------------");
        allmethods = cu.iterator();
        while(allmethods.hasNext()){
            Procedure nextProc = allmethods.next();
            callGraphBuilder.enter(nextProc);
            tr.enter(nextProc, null);
            // cpa.enter(nextProc, null);
        }
        // tr.fullAnalysis();

        System.out.println("------------ Starting ConstantPropagationAnalysis 3/4 ------------");
        Procedure __main = cu.getProcedure("main");
        worklist.add(__main);
        cpa.setWorklist(worklist);
        //#TODO check if should iterate
        while(!worklist.isEmpty()){
        	Procedure nextProc = worklist.get(0);
        	worklist.remove(0);
        	cpa.enter(nextProc, cpa.dataflowOf(nextProc.getEnd()));
        	cpa.fullAnalysis();
        }
        

        // cpa.enter(bar, null);
        // cpa.fullAnalysis();

        //#TODO check if should iterate
        // copyprop.enter(___main, null);
        // copyprop.fullAnalysis();

        // DotLayout layout = new DotLayout("jpg", "barConstant.jpg");

        // for (State s : bar.getStates()){
        //     System.out.println("For "+s+" we have "+cpa.dataflowOf(s));
        //     layout.highlight(s,(cpa.dataflowOf(s))+"");
        // }
        // layout.callDot(bar);

//        allmethods = cu.iterator();
//       while(allmethods.hasNext()){
//            Procedure proc = allmethods.next();
//            DotLayout layout = new DotLayout("jpg", proc.getName()+"After22.jpg");
//            System.out.println("----------------"+proc.getName()+"----------------");
//            for (State s: proc.getStates()){
//                System.out.println("For "+s+" we have "+tr.dataflowOf(s));
//                layout.highlight(s,(tr.dataflowOf(s))+"");
//            }
//            layout.callDot(proc);
//        }

    }
}
