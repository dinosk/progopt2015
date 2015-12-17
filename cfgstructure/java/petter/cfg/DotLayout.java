/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package petter.cfg;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import petter.cfg.edges.Transition;

/**
 *
 * @author petter
 */
public class DotLayout {
    

    private boolean extremeRanks;
    private String command;
    private String fontname;
    private String fontsize;
    private String fontpath;
    private ProcessBuilder pb;

    

    public DotLayout(String format,String filename) {
        command = "dot";
        fontsize="12";
        fontname="Dialog-Bold";
        pb = new ProcessBuilder(command, "-T"+format,"-o"+filename);
        pb.redirectErrorStream(true);
    }



    public void callDot(Procedure p)
            throws InterruptedException, IOException {

        Iterable<State> nodes = p.getStates();
        Iterable<Transition> edges = p.getTransitions();
        Process dot = pb.start();
        Writer w = new BufferedWriter(new OutputStreamWriter(dot.getOutputStream(), "UTF-8"));
        w.write("digraph {\n" +
                "dpi=150;\n" +
                "charset=\"UTF-8\";\n" +
                "");
        w.write("edge [labeljust=l");
        if (fontname != null)
            w.write(", fontname=\"" + fontname.replace("\"", "\\\"") + "\"");
        if (fontsize != null)
            w.write(", fontsize=\"" + fontsize.replace("\"", "\\\"") + "\"");
        w.write("];\n");
        w.write("node [shape=box, fixedsize=true");
        w.write("];\n");

        w.write("subgraph cluster0 {");
        
        int id = 0;
        Map<State, String> n2s = new HashMap<>();
        Map<String, State> s2n = new HashMap<>();
        for (State n : nodes) {
            String color = "black";
            String s = "n" + n.getId();
            if (toHighlight.containsKey(n)) {
                color="red";
//                s+="_"+toHighlight.get(n);
            }
            n2s.put(n, s);
            s2n.put(s, n);
            w.write(s + " [label=\""+s+"\" width=" + n.toString().length()*4/72. +
                    ", height=" + 16/72. +
                    ", shape=box, fixedsize=true, color=\""+color+"\" ];\n");
        }

        if (extremeRanks) {
            // warning: this code often causes "trouble in int_rank"
            // error messages for current graphviz 2.6
            w.write("subgraph begin { rank=source; " +
                    n2s.get(p.getBegin()) + "; }\n");
            w.write("subgraph end { rank=sink; " +
                    n2s.get(p.getEnd()) + "; }\n");
        }
        Map<String, Transition> s2e = new HashMap<>();
        for (Transition e : edges) {
            String color = "black";
            if (toHighlight.containsKey(e)) color="red";
            String s = n2s.get(e.getSource()) + " -> " + n2s.get(e.getDest());
            s2e.put(s, e);
            w.write(s + " [label=\" " +
                    e.toString().replace("\"", "\\\"") + " \"" +
                    " color=\""+color+"\" ];\n");
        }

        w.write("}\n"); // cluster

        int highlightcounter = 0;
        for (Analyzable a : toHighlight.keySet()){
            if (a instanceof Transition) continue;
            //String botschaft = "[*[a, a] ≐ *[*[b, b], *[b, b]]]";
            String botschaft = toHighlight.get(a);
            w.write("no"+highlightcounter +" [color=\"lightgrey\" shape=box style=\"filled\", fixedsize=false, width=1, label=<"+botschaft+">];\n");
            w.write("no"+highlightcounter++ +" -> "+n2s.get(a)+" [style=\"dotted\" color=\"red\"];\n");
        }
        
        w.write("}\n");        
        
        w.close();
        dot.waitFor();
    }
    private Map<Analyzable,String> toHighlight = new HashMap<>();
    public void highlight(Analyzable toHighlight, String s) {
        this.toHighlight.put(toHighlight,s);
    }

    
}
