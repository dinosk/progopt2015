package petter.cfg;

import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.*;

public class OptimizerAnalysis{

    public static void main(String[] args) throws Exception {

        int numOfCallsCount = 10; //take it as an argumet from command line?
        int numOfStatesCount = 100;

        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        ArrayList<Procedure> worklist = new ArrayList<Procedure>();
        CallGraphBuilder callGraphBuilder = new CallGraphBuilder(cu);
        TailRecursionAnalysis tr = new TailRecursionAnalysis(cu);
        // ConstantPropagationAnalysis cpa = new ConstantPropagationAnalysis(cu);

        Iterator<Procedure> allmethods;// = cu.iterator();

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


// Procedure __main = cu.getProcedure("main");
        // Map Locals with Variable names
        HashMap<Procedure, HashMap<Integer, Variable>> procVarMap = new HashMap<Procedure, HashMap<Integer, Variable>>();
        for(String s : cu.getProcedures().keySet()) {
            Procedure proc = cu.getProcedures().get(s);
            procVarMap.put(proc, new HashMap<Integer, Variable>());
            VarMapVisitor varMap = new VarMapVisitor(cu, procVarMap, proc);
            varMap.enter(proc);
            varMap.fullAnalysis();
        }
        for(Procedure p : procVarMap.keySet()) {
            System.out.println("Proc " + p.getName() + " locals " + procVarMap.get(p));
        }


        allmethods = cu.iterator();
        // VarMapVisitor varMap = new VarMapVisitor(cu);
        // allmethods = cu.iterator();
        // while(allmethods.hasNext()){
        //     varMap.enter(allmethods.next());
        // }
        // // varMap.enter(__main);
        // varMap.fullAnalysis();
        // HashMap<String, HashMap<Integer, Variable>> vv = varMap.getprocVarMap();
        // for(String s : vv.keySet()) {
        //     System.out.println("Proc " + s + " Map: " + vv.get(s));
        // }

        while(allmethods.hasNext()) {
            Procedure proc = allmethods.next();
            DotLayout layout = new DotLayout("jpg", proc.getName()+"Before120.jpg");
            System.out.println("----------------"+proc.getName()+"----------------");
            // for (State s: proc.getStates()){
            //     System.out.println("For "+s+" we have "+ia2.dataflowOf(s));
            //     layout.highlight(s,(ia2.dataflowOf(s))+"");
            // }
            layout.callDot(proc);
        }

        // allmethods = cu.iterator();
        // ArrayList<Procedure> numOfStates = new ArrayList<Procedure>();
        // int statesSum = 0;
        // while(allmethods.hasNext()) {
        //     Procedure proc = allmethods.next();
        //     // Iterable<State> states = proc.getStates();
        //     // it = states.iterator();
        //     statesSum = 0;
        //     for(State s : proc.getStates())
        //          statesSum++;
        //     System.out.println("Procedure " + proc.getName() + " has " + statesSum + " number of states.");
        //     if(statesSum <= numOfStatesCount) {
        //         numOfStates.add(proc);
        //     }
        //  }

        // InliningAnalysis ia2 = new InliningAnalysis(cu, numOfStates);
        // System.out.println("------------ Starting InliningAnalysis 0/4 ------------");
        // allmethods = cu.iterator();
        // while(allmethods.hasNext()){
        //     ia2.enter(allmethods.next());
        // }
        // ia2.fullAnalysis();


        // allmethods = cu.iterator();
        // while(allmethods.hasNext()) {
        //     Procedure proc = allmethods.next();
        //     DotLayout layout = new DotLayout("jpg", proc.getName()+"After120.jpg");
        //     System.out.println("----------------"+proc.getName()+"----------------");
        //     // for (State s: proc.getStates()){
        //     //     System.out.println("For "+s+" we have "+ia2.dataflowOf(s));
        //     //     layout.highlight(s,(ia2.dataflowOf(s))+"");
        //     // }
        //     layout.callDot(proc);
        // }



        // while(allmethods.hasNext()){
        //     Procedure nextProc = allmethods.next();
        //     callGraphBuilder.enter(nextProc);
        //     tr.enter(nextProc, null);
        //     // cpa.enter(nextProc, null);
        // }

        // ArrayList<Procedure> leafProcs = callGraphBuilder.getLeafProcs();
        // HashMap<Procedure, ArrayList<Procedure>> callGraph = callGraphBuilder.getCallGraph();
        // if(leafProcs.isEmpty()){
        //     System.out.println("No leaves found");
        // }
        // else{
        //     for(Procedure method : leafProcs){
        //         System.out.println(method.getName()+" is a leaf");
        //     }
        // }
        // // get all function names of a compilation unit and then inline according to the #ofCalls
        // HashMap<String, Integer> procCalls = new HashMap<String, Integer>();
        // for(String methodName : cu.getProcedures().keySet()) {
        //     procCalls.put(methodName, 0);
        // }
        // NumOfCallsVisitor callsVisitor = new NumOfCallsVisitor(cu, procCalls);
        // allmethods = cu.iterator();
        // while(allmethods.hasNext()){
        //     callsVisitor.enter(allmethods.next());
        // }
        // // callsVisitor.fullAnalysis();
        // // callsVisitor.enter(__main);
        // // callsVisitor.fullAnalysis();
        // ArrayList<Procedure> numOfCalls = new ArrayList<Procedure>();
        // for(String methodName : callsVisitor.getProcCalls().keySet()) {
        //     if(callsVisitor.getProcCalls().get(methodName) <= numOfCallsCount) {
        //         numOfCalls.add(cu.getProcedures().get(methodName));
        //         System.out.println("Add " + methodName + " " + callsVisitor.getProcCalls().get(methodName));
        //     }
        // }
        // InliningAnalysis ia1 = new InliningAnalysis(cu, numOfCalls);
        // System.out.println("------------ Starting InliningAnalysis 1/4 ------------");
        // allmethods = cu.iterator();
        // while(allmethods.hasNext()){
        //     ia1.enter(allmethods.next());
        // }
        // // ia1.fullAnalysis();

        // InliningAnalysis ia = new InliningAnalysis(cu, leafProcs);

        // // System.out.println("------------ Starting InliningAnalysis 1/4 ------------");
        // // allmethods = cu.iterator();
        // // while(allmethods.hasNext()){
        // //     ia.enter(allmethods.next());
        // // }
        // // ia.fullAnalysis();
        // System.out.println("------------ Starting TailRecursionAnalysis 2/4 ------------");
        // // tr.fullAnalysis();
        // System.out.println("------------ Starting ConstantPropagationAnalysis 3/4 ------------");
        // Procedure bar = cu.getProcedure("bar");
        // worklist.add(bar);
        // //#TODO check if should iterate
        // // cpa.enter(bar, null);
        // // cpa.fullAnalysis();

        // System.out.println("------------ Starting ConstantPropagationAnalysis 4/4 ------------");
        // Procedure ___main = cu.getProcedure("main");
        // worklist.add(___main);
        //#TODO check if should iterate
        // copyprop.enter(___main, null);
        // copyprop.fullAnalysis();

        // DotLayout layout = new DotLayout("jpg", "barConstant.jpg");

        // for (State s : bar.getStates()){
        //     System.out.println("For "+s+" we have "+cpa.dataflowOf(s));
        //     layout.highlight(s,(cpa.dataflowOf(s))+"");
        // }
        // layout.callDot(bar);




        // intraprocedural Var Var Moves

        // DotLayout layout = new DotLayout("jpg", __main.getName()+"After111.jpg");
        // System.out.println("----------------"+__main.getName()+"----------------");
        // for (State s: __main.getStates()){
        //     System.out.println("For "+s+" we have "+varTovar.dataflowOf(s));
        //     layout.highlight(s,(varTovar.dataflowOf(s))+"");
        // }
        // layout.callDot(__main);

    }
}
