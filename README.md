This is a simplified C-like frontend, which produces Compilation Units, which
hold Control Flow Graphs.

Both projects come with a Netbeans Project file -- but You can of course just
use the command line build tool ANT, whose build.xml files come with the
projects.

git clone git@versioncontrolseidl.in.tum.de:petter/simpleC.git
cd cfgstructure
ant

After compilation, you find the usable Frontend in cfgstructure/dist/Compiler.jar

The project is split in two subprojects, the SimpleC Frontend and the
Intermediate Representation CFGStructure. SimpleC contains the complete
Frontend of the SimpleC language; You can obtain a SimpleC Controlflowgraph via

CompilationUnit cu = petter.simplec.Compiler.parse(File f)

The data structures for the IR can be found in the cfgstructure subproject. The
root for all IR related data is petter.cfg.CompilationUnit . You can obtain
all procedures and fields from a compilation unit, as well as general information
like all states and translations of variable-ids (integers) to 
source-level-variable-names. A petter.cfg.Procedure is a model of a particular CFG.
It offers informations like programstates, beginstate, endstate, and transitions
between the states. Normally, You do not have to touch this one directly, instead
rely on the fixpoint engine:

