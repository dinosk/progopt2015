# SimpleC

This is a simplified C-like frontend, which produces Compilation Units, which
hold Control Flow Graphs.

Both projects come with a Netbeans Project file -- but You can of course just
use the command line build tool ANT, whose build.xml files come with the
projects.

```bash
git clone git@versioncontrolseidl.in.tum.de:petter/simpleC.git
cd simplec
ant
```

After compilation, you find the usable Frontend in cfgstructure/dist/Compiler.jar

## simplec - subproject

The project is split in two subprojects, the SimpleC Frontend and the
Intermediate Representation CFGStructure. SimpleC contains the complete
Frontend of the SimpleC language; You can obtain a SimpleC Controlflowgraph via

```java
CompilationUnit cu = petter.simplec.Compiler.parse(File f);
```

## cfgstructure - subproject

The data structures for the IR can be found in the cfgstructure subproject. The
root for all IR related data is petter.cfg.CompilationUnit . You can obtain
all procedures and fields from a compilation unit, as well as general information
like all states and translations of variable-ids (integers) to 
source-level-variable-names. A petter.cfg.Procedure is a model of a particular CFG.
It offers informations like programstates, beginstate, endstate, and transitions
between the states. Normally, You do not have to touch this one directly, instead
rely on the fixpoint engine:

## Fixpoint engine

For the fixpoint-engine, you need to create your own class:

```java
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;


public class ReachabilityAnalysis extends AbstractPropagatingVisitor<Boolean>{

    static Boolean lub(Boolean b1,Boolean b2){
	    if (b1==null) return b2;
	    if (b2==null) return b1;
	    return b1&&b2;
    }
    static boolean lessoreq(Boolean b1,Boolean b2){
	    if (b1==null) return true;
	    if (b2==null) return false;
	    return ((!b1) || b2);
    }

    CompilationUnit cu;
    public ReachabilityAnalysis(CompilationUnit cu){
        super(true); // forward reachability
	    this.cu=cu;
    }
    public Boolean visit(MethodCall m,Boolean b){
	    enter(cu.getProcedure(m.getCallExpression().getName()),true);
	    return b;
    }
    public Boolean visit(State s,Boolean newflow){
	    Boolean oldflow = dataflowOf(s);
	    if (!lessoreq(newflow,oldflow)){
	        Boolean newval = lub(oldflow,newflow);
	        dataflowOf(s,newval);
	        return newval;
	    }
	    return null;
    }
   
    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        ReachabilityAnalysis ra = new ReachabilityAnalysis(cu);
        Procedure foo = cu.getProcedure("main");
	    DotLayout layout = new DotLayout("jpg","main.jpg");
        ra.enter(foo,true);
        ra.fullAnalysis();
	    for (State s: foo.getStates()){
	        layout.highlight(s,(ra.dataflowOf(s))+"");
	    }
	    layout.callDot(foo);
    }
}


```

## Graphical output

You can use the petter.cfg.DotLayout class to generate nice output of your
Analysis project; you create it with a file-format-specifier and a file name, and
it creates an OS-process of dot, from the graph layout software Graphviz. If you
then call callDot(p) with a cfgstructure-Procedure, then the tool will feed dot
with a graphical output of the CFG. If you call highlight(a,s) on the DotLayout
object with an Analyzable from the CFG and a custom string, then a speech bubble
with the string s pointing to a will be added to the graph.