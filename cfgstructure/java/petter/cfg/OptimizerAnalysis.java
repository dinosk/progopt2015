package petter.cfg;
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;
import java.util.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.IntegerConstant;

public class OptimizerAnalysis{
    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        ArrayList<Procedure> worklist = new ArrayList<Procedure>();
        CallGraphBuilder callGraphBuilder = new CallGraphBuilder(cu);
        TailRecursionAnalysis tr = new TailRecursionAnalysis(cu);
        ConstantPropagationAnalysis cpa = new ConstantPropagationAnalysis(cu);

        Iterator<Procedure> allmethods = cu.iterator();
        while(allmethods.hasNext()){
            Procedure nextProc = allmethods.next();
            callGraphBuilder.enter(nextProc);
            tr.enter(nextProc, null);
            // cpa.enter(nextProc, null);
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

        InliningAnalysis ia = new InliningAnalysis(cu, leafProcs);
        Procedure __main = cu.getProcedure("main");
        System.out.println("------------ Starting InliningAnalysis 1/3 ------------");
        allmethods = cu.iterator();
        while(allmethods.hasNext()){
            ia.enter(allmethods.next());
        }
        ia.fullAnalysis();
        System.out.println("------------ Starting TailRecursionAnalysis 2/3 ------------");
        tr.fullAnalysis();     
        System.out.println("------------ Starting ConstantPropagationAnalysis 3/4 ------------");
        Procedure bar = cu.getProcedure("bar");

        // worklist.add(bar);
        //#TODO check if should iterate
        cpa.enter(bar, null);
        cpa.fullAnalysis();
        // cpa.enter(bar, null);
        // cpa.fullAnalysis();

        // DotLayout layout = new DotLayout("jpg", "barConstant.jpg");
        
        // for (State s : bar.getStates()){
        //     System.out.println("For "+s+" we have "+cpa.dataflowOf(s));
        //     layout.highlight(s,(cpa.dataflowOf(s))+"");
        // }
        // layout.callDot(bar);

        allmethods = cu.iterator();
        while(allmethods.hasNext()){
            Procedure proc = allmethods.next();
            DotLayout layout = new DotLayout("jpg", proc.getName()+"After22.jpg");
            System.out.println("----------------"+proc.getName()+"----------------");
            for (State s: proc.getStates()){
                System.out.println("For "+s+" we have "+cpa.dataflowOf(s));
                layout.highlight(s,(cpa.dataflowOf(s))+"");
            }
            layout.callDot(proc);
        }

    }
}