package petter.cfg;

import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.*;

public class OptimizerAnalysis{

    public static void main(String[] args) throws Exception {
        ArrayList<String> argumentsArray = new ArrayList<String>(Arrays.asList(args));
        int numOfCallsCount = 10; //take it as an argument from command line?
        int numOfStatesCount = 100;

        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        CallGraphBuilder callGraphBuilder = new CallGraphBuilder(cu);
        
        // Map Locals with Variable names
        System.out.println("Calculating procVarMap");
        HashMap<Procedure, HashMap<Integer, Variable>> procVarMap = new HashMap<Procedure, HashMap<Integer, Variable>>();
        for(String s : cu.getProcedures().keySet()) {
            Procedure proc = cu.getProcedures().get(s);
            procVarMap.put(proc, new HashMap<Integer, Variable>());
            VarMapVisitor varMap = new VarMapVisitor(procVarMap, proc);
            varMap.enter(proc);
            varMap.fullAnalysis();
        }
        
        // get all function names of a compilation unit and then inline according to the #ofCalls
        HashMap<String, Integer> procCalls = new HashMap<String, Integer>();
        for(String methodName : cu.getProcedures().keySet()) {
            procCalls.put(methodName, 0);
        }
        NumOfCallsVisitor callsVisitor = new NumOfCallsVisitor(procCalls);
        Iterator<Procedure> allmethods = cu.iterator();
        while(allmethods.hasNext()){
            callsVisitor.enter(allmethods.next());
        }
        System.out.println("Counting MethodCalls");
        callsVisitor.fullAnalysis();
        ArrayList<Procedure> numOfCalls = new ArrayList<Procedure>();
        for(String methodName : callsVisitor.getProcCalls().keySet()) {
            if(callsVisitor.getProcCalls().get(methodName) <= numOfCallsCount) {
                numOfCalls.add(cu.getProcedures().get(methodName));
            }
        }

        allmethods = cu.iterator();
        while(allmethods.hasNext()) {
            Procedure proc = allmethods.next();
            DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterInit.jpg");
            layout.callDot(proc);
        }

        if(!argumentsArray.contains(("--no-tail"))){
            System.out.println("------------ Starting TailRecursionAnalysis 2/4 ------------");
            TailRecursionAnalysis tr = new TailRecursionAnalysis(cu, procVarMap);
            allmethods = cu.iterator();
            while(allmethods.hasNext()){
                Procedure nextProc = allmethods.next();
                if(nextProc.getName().equals("$init"))continue;
                do{
                    tr.enter(nextProc);
                    tr.fullAnalysis();
                }while(!tr.fixedPoint);
            }

            allmethods = cu.iterator();
            while(allmethods.hasNext()) {
                Procedure proc = allmethods.next();
                DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterTailRec.jpg");
                layout.callDot(proc);
            }

        }
        
        if(!argumentsArray.contains(("--no-inlining"))){
            System.out.println("------------ Starting InliningAnalysis 1/4 ------------");
            Procedure __main = cu.getProcedure("main");
            
            ArrayList<Procedure> leafProcs = callGraphBuilder.getLeafProcs();
            HashMap<Procedure, ArrayList<Procedure>> callGraph = callGraphBuilder.getCallGraph();

            InliningAnalysis ia = new InliningAnalysis(cu, leafProcs, procVarMap);
            allmethods = cu.iterator();
            while(allmethods.hasNext()){
                Procedure nextProc = allmethods.next();
                if(nextProc.getName().equals("$init") || nextProc.getName().equals("main"))continue;
                int iterCount = 0;
                do{
                    // System.out.println("Analyzing "+nextProc.getName());
                    ia.enter(nextProc);
                    ia.fullAnalysis();
                    iterCount++;
                }while(!ia.fixedPoint);
                System.out.println("Finished in "+iterCount+" iterations");
            }
            __main = cu.getProcedure("main");
            System.out.println("ti epistrefei i main:"+__main.getName());
            do{
                ia.enter(__main);
                ia.fullAnalysis();
            }while(!ia.fixedPoint);

            allmethods = cu.iterator();
            while(allmethods.hasNext()) {
                Procedure proc = allmethods.next();
                DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterInline.jpg");
                layout.callDot(proc);
            }
        }

        if(!argumentsArray.contains(("--no-constant"))){
            System.out.println("------------ Starting ConstantPropagationAnalysis 3/4 ------------");

            ConstantPropagationAnalysis copyprop = new ConstantPropagationAnalysis(cu);
            Procedure __main = cu.getProcedure("main");
            // do{
            //     copyprop.enter(__main, null);
            //     copyprop.fullAnalysis();
            // }while(!copyprop.fixedPoint);

            allmethods = cu.iterator();
            while(allmethods.hasNext()){
                Procedure nextProc = allmethods.next();
                if(nextProc.getName().equals("$init"))continue;
                int iterCount = 1;
                do{
                    iterCount++;
                    System.out.println("Analyzing "+nextProc.getName());
                    copyprop.enter(nextProc, null);
                    copyprop.fullAnalysis();
                }while(!copyprop.fixedPoint);
                System.out.println("Finished after "+iterCount+" iterations");
            }

            allmethods = cu.iterator();
            while(allmethods.hasNext()) {
                Procedure proc = allmethods.next();
                DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterConstant.jpg");
                for (State s : proc.getStates()){
                    layout.highlight(s,(copyprop.dataflowOf(s))+"");
                }
                layout.callDot(proc);
            }

            System.out.println("============== Applying Transformation 4 ==============");
            ConstantTransformation constantTrans = new ConstantTransformation(cu, copyprop);
            constantTrans.enter(__main);
            constantTrans.fullAnalysis();
            
            allmethods = cu.iterator();
            while(allmethods.hasNext()){
                Procedure proc = allmethods.next();
                DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterT4.jpg");
                for (State s : proc.getStates()){
                    layout.highlight(s,(copyprop.dataflowOf(s))+"");
                }
                layout.callDot(proc);
            }
        }

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
