package petter.cfg;

import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.*;

public class OptimizerAnalysis{

    public static void main(String[] args) throws Exception {

        int numOfCallsCount = 10; //take it as an argument from command line?
        int numOfStatesCount = 100;

        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        ArrayList<Procedure> worklist = new ArrayList<Procedure>();
        CallGraphBuilder callGraphBuilder = new CallGraphBuilder(cu);
        // ConstantPropagationAnalysis cpa = new ConstantPropagationAnalysis(cu);

        // Map Locals with Variable names
        HashMap<Procedure, HashMap<Integer, Variable>> procVarMap = new HashMap<Procedure, HashMap<Integer, Variable>>();
        for(String s : cu.getProcedures().keySet()) {
            Procedure proc = cu.getProcedures().get(s);
            procVarMap.put(proc, new HashMap<Integer, Variable>());
            VarMapVisitor varMap = new VarMapVisitor(cu, procVarMap, proc);
            varMap.enter(proc);
            varMap.fullAnalysis();
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
        callsVisitor.fullAnalysis();
        ArrayList<Procedure> numOfCalls = new ArrayList<Procedure>();
        for(String methodName : callsVisitor.getProcCalls().keySet()) {
            if(callsVisitor.getProcCalls().get(methodName) <= numOfCallsCount) {
                numOfCalls.add(cu.getProcedures().get(methodName));
                // System.out.println("Add " + methodName + " " + callsVisitor.getProcCalls().get(methodName));
            }
        }

        allmethods = cu.iterator();
        while(allmethods.hasNext()) {
            Procedure proc = allmethods.next();
            DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterInit.jpg");
            layout.callDot(proc);
        }

        System.out.println("------------ Starting TailRecursionAnalysis 2/4 ------------");
        TailRecursionAnalysis tr = new TailRecursionAnalysis(cu, procVarMap);
        allmethods = cu.iterator();
        while(allmethods.hasNext()){
            Procedure nextProc = allmethods.next();
            // if(nextProc.getName().equals("$init"))continue;
            tr.enter(nextProc);
        }
        tr.fullAnalysis();
        
        allmethods = cu.iterator();
        while(allmethods.hasNext()) {
            Procedure proc = allmethods.next();
            DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterTailRec.jpg");
            layout.callDot(proc);
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
        
        System.out.println("------------ Starting InliningAnalysis 1/4 ------------");
        // Procedure __main = cu.getProcedure("main");
        // System.out.println("main has end:"+__main.getEnd());
        // System.out.println("End has outedges:"+__main.getEnd().getOutDegree());

        // InliningAnalysis ia = new InliningAnalysis(cu, leafProcs, procVarMap);
        // allmethods = cu.iterator();
        // while(allmethods.hasNext()){
        //     Procedure nextProc = allmethods.next();
        //     if(nextProc.getName().equals("$init"))continue;
        //     ia.hasWork = true;
        //     while(ia.hasWork){
        //         System.out.println("Analyzing "+nextProc.getName());
        //         ia.hasWork = false;
        //         ia.enter(nextProc);
        //         ia.fullAnalysis();
        //     }
        // }
        // Procedure __main = cu.getProcedure("main");
        // System.out.println("ti epistrefei i main:"+__main.getName());
        // ia.hasWork = true;
        // while(ia.hasWork){
        //     System.out.println("Analyzing main");
        //     ia.hasWork = false;
        //     ia.enter(__main);
        //     ia.fullAnalysis();
        // }

        // allmethods = cu.iterator();
        // while(allmethods.hasNext()) {
        //     Procedure proc = allmethods.next();
        //     DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterInline.jpg");
        //     layout.callDot(proc);
        // }

        System.out.println("------------ Starting ConstantPropagationAnalysis 3/4 ------------");
        ConstantPropagationAnalysis copyprop = new ConstantPropagationAnalysis(cu);

        // allmethods = cu.iterator();
        // // while(allmethods.hasNext()){
        // //     copyprop.enter(allmethods.next(), null);
        // // }
        // __main = cu.getProcedure("main");
        // copyprop.enter(__main, null);
        // copyprop.fullAnalysis();

        // allmethods = cu.iterator();
        // while(allmethods.hasNext()) {
        //     Procedure proc = allmethods.next();
        //     DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterConstant.jpg");
        //     for (State s : proc.getStates()){
        //         layout.highlight(s,(copyprop.dataflowOf(s))+"");
        //     }
        //     layout.callDot(proc);
        // }

        // System.out.println("------------ Starting VarToVarMoveAnalysis 4/4 ------------");

        // VarVar Moves Analysis

        // VarToVarMoveAnalysis varTovar = new VarToVarMoveAnalysis(cu);
        // System.out.println("------------ Starting VarVarMoveAnalysis 0/4 ------------");
        // Procedure __main = cu.getProcedure("main");
        // HashMap<String, HashSet<Variable>> d = new HashMap<String, HashSet<Variable>>();
        // varTovar.enter(__main, d);
        // varTovar.fullAnalysis();

        // System.out.println("Available Expr: " + varTovar.getAvailableExpr());
        // System.out.println("Final Map: " + varTovar.dataflowOf(__main.getEnd()));
        // System.out.println("-----------****************************----\n");
        // varTovar.enter(__main, null);
        // varTovar.fullAnalysis();
        //  System.out.println("Available Expr: " + varTovar.getAvailableExpr());
        // System.out.println("Final Map: " + varTovar.dataflowOf(__main.getEnd()));
        // System.out.println("---------------\n");
        // varTovar.enter(__main, null);
        // varTovar.fullAnalysis();

        // intraprocedural Var Var Moves

        // DotLayout layout = new DotLayout("jpg", __main.getName()+"After111.jpg");
        // System.out.println("----------------"+__main.getName()+"----------------");
        // for (State s: __main.getStates()){
        //     System.out.println("For "+s+" we have "+varTovar.dataflowOf(s));
        //     layout.highlight(s,(varTovar.dataflowOf(s))+"");
        // }
        // layout.callDot(__main);
        System.out.println("------------ All Done! ------------");
    }
}
