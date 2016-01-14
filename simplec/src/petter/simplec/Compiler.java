package petter.simplec;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.utils.AnnotatingSymbolFactory;
import petter.simplec.Lexer;
import petter.simplec.Parser;


import java_cup.runtime.*;
import java.io.*;
import petter.cfg.DotLayout;
public class Compiler{
//    public static Lexer syntaxHighlighter(File f) throws Exception{
//	InputStream is = new FileInputStream(f);
//	AnnotatingSymbolFactory sf = new AnnotatingSymbolFactory(f);
//	return new Lexer(is,sf,true);
//    }
    public static CompilationUnit parse(File f) throws Exception{
            InputStream is = new FileInputStream(f);
            AnnotatingSymbolFactory sf = new AnnotatingSymbolFactory(f);
            Parser parser = new Parser(new Lexer(is,sf),sf);
            return (CompilationUnit)parser.parse().value;
        
    }
    public static void main(String[] args){
        if (args.length<1) {
            System.out.println("Usage: java -jar Compiler.jar input.simplec");
            return;
        }
        try{
            CompilationUnit c = parse(new File(args[0]));
            for (Procedure p : c.getProcedures().values()){
                DotLayout layout = new DotLayout("jpg",args[0]+"."+p.getName()+".jpg");
                layout.callDot(p);
            }
        }catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
            return;
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
}
