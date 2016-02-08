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
        HashMap<Procedure, HashMap<Integer, Variable>> procVarMap = new HashMap<Procedure, HashMap<Integer, Variable>>();
        for(String s : cu.getProcedures().keySet()) {
            Procedure proc = cu.getProcedures().get(s);
            procVarMap.put(proc, new HashMap<Integer, Variable>());
            VarMapVisitor varMap = new VarMapVisitor(procVarMap, proc);
            varMap.enter(proc);
            varMap.fullAnalysis();
        }

        // Get all function names of a compilation unit and then inline according to the #ofCalls
        HashMap<String, Integer> procCalls = new HashMap<String, Integer>();
        for(String methodName : cu.getProcedures().keySet()) {
            procCalls.put(methodName, 0);
        }
        NumOfCallsVisitor callsVisitor = new NumOfCallsVisitor(procCalls);
        Iterator<Procedure> allmethods = cu.iterator();
        while(allmethods.hasNext()){
            callsVisitor.enter(allmethods.next());
        }
        // Counts the number of calls for each procedure
        callsVisitor.fullAnalysis();
        ArrayList<Procedure> numOfCalls = new ArrayList<Procedure>();
        for(String methodName : callsVisitor.getProcCalls().keySet()) {
            if(callsVisitor.getProcCalls().get(methodName) <= numOfCallsCount) {
                numOfCalls.add(cu.getProcedures().get(methodName));
            }
        }

        if(!argumentsArray.contains(("--no-tailrec"))){
            System.out.println("------------ Starting TailRecursionAnalysis 1/5 ------------");
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
                DotLayout layout = new DotLayout("jpg", proc.getName()+"AfterTail.jpg");
                layout.callDot(proc);
            }
        }

        if(!argumentsArray.contains(("--no-inlining"))){
            System.out.println("------------ Starting InliningAnalysis 2/5 ------------");
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
                    ia.enter(nextProc);
                    ia.fullAnalysis();
                    iterCount++;
                }while(!ia.fixedPoint);
            }
            __main = cu.getProcedure("main");
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
            System.out.println("------------ Starting ConstantPropagationAnalysis 3/5 ------------");
            ConstantPropagationAnalysis copyprop = new ConstantPropagationAnalysis(cu);
            Procedure __main = cu.getProcedure("main");
            
            allmethods = cu.iterator();
            while(allmethods.hasNext()){
                Procedure nextProc = allmethods.next();
                if(nextProc.getName().equals("$init"))continue;
                int iterCount = 1;
                do{
                    iterCount++;
                    copyprop.enter(nextProc, null);
                    copyprop.fullAnalysis();
                }while(!copyprop.fixedPoint);
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

            // Applying Transformation 4
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
        
        if(!argumentsArray.contains(("--no-vartovar"))) {
            System.out.println("------------ Starting VarVarMoveAnalysis 4/5 ------------");
            VarToVarMoveAnalysis varTovar = new VarToVarMoveAnalysis();
            Procedure __main = cu.getProcedure("main");
            int iterCounter = 0;
            do{
                iterCounter++;
                varTovar.enter(__main, null);
                varTovar.fullAnalysis();
            }
            while(!varTovar.getFixpointCheck());

            // intraprocedural Var Var Moves Before T3
            DotLayout layout = new DotLayout("jpg", __main.getName()+"AfterVarMoves.jpg");
            for(State s: __main.getStates()){
                layout.highlight(s,(varTovar.dataflowOf(s))+"");
            }
            layout.callDot(__main);

            // After transformations T3
            VarVarMoveTransformationAnalysis t3 = new VarVarMoveTransformationAnalysis(varTovar);
            t3.enter(__main);
            t3.fullAnalysis();

            layout = new DotLayout("jpg", __main.getName()+"AfterT3.jpg");
            for(State s: __main.getStates()){
                layout.highlight(s,(varTovar.dataflowOf(s))+"");
            }
            layout.callDot(__main);
        }

        if(!argumentsArray.contains(("--no-liveness"))){
            System.out.println("------------ Starting LivenessAnalysis 5/5 ------------");
            IntraTrulyLivenessAnalysis itLive = new IntraTrulyLivenessAnalysis();
            Procedure __main = cu.getProcedure("main");
            do {
                itLive.enter(__main, null);
                itLive.fullAnalysis();
            }
            while(!itLive.getFixpointCheck());

            DotLayout layout = new DotLayout("jpg", __main.getName()+"AfterLive.jpg");
            for(State s: __main.getStates()){
                layout.highlight(s,(itLive.dataflowOf(s))+"");
            }
            layout.callDot(__main);

            // Dead variable removal
            RemoveDeadVarsAnalysis deadVars= new RemoveDeadVarsAnalysis(itLive);
            deadVars.enter(__main, null);
            deadVars.fullAnalysis();

            layout = new DotLayout("jpg", __main.getName()+"AfterDead.jpg");
            for(State s: __main.getStates()){
            }
            layout.callDot(__main);
        }
        System.out.println("------------ All Done! ------------");
    }
}
