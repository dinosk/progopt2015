package petter.simplec;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import org.junit.Test;

import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.Transition;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.Variable;
import petter.cfg.expression.types.Function;
import petter.cfg.expression.types.Int;
import petter.cfg.expression.types.PointerTo;
import petter.utils.AnnotatingSymbolFactory;

public class Declarations {
	private static String declAndStatement(String declaration, String expression){
		return "int a;"
				+ "int b;"
				+ "int c;"
				+ declaration
				+ "int *p;"
				+ "int main(){"
				+ expression
				+ "}";
		
	}
	private static CompilationUnit compile(String programcode) throws Exception{
		InputStream is = new ByteArrayInputStream(programcode.getBytes(StandardCharsets.UTF_8));
        AnnotatingSymbolFactory sf = new AnnotatingSymbolFactory();
        Parser parser = new Parser(new Lexer(is,sf),sf);
        return (CompilationUnit)parser.parse().value;
	}
	private static Transition extractMainTransition(CompilationUnit cu){
		Procedure main = cu.getProcedure("main");
		return main.getBegin().getOut().iterator().next();
	}
	private static Transition directNextTransition(Transition now){
		return now.getDest().getOut().iterator().next();
	}
	private static boolean isLastTransition(Transition now){
		return now.getDest().isEnd();
	}

	@Test
	public void testGlobalSingularInt() {
		try {
			// 
			Transition transition = extractMainTransition(compile(declAndStatement("","a=a;")));
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			Variable v = (Variable)a.getLhs();
			assertTrue(v.getType().equals(Int.create()));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}
	@Test
	public void testGlobalSingularIntInitializer() {
		try {
			boolean b = false;
			for(Transition transition : compile(declAndStatement("int z=42;","z=z;")).getProcedure("$init").getTransitions()){
				assertTrue(transition instanceof Assignment);
				Assignment a = (Assignment)transition;
				assertTrue(a.getLhs() instanceof Variable);
				Variable v = (Variable)a.getLhs();
				if (a.getRhs() instanceof IntegerConstant)
					if (((IntegerConstant)a.getRhs()).getIntegerConst()==42) b=true;
			}
			assertTrue(b);
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}
	
	@Test
	public void testGlobalSeveralInts() {
		try {
			//$1 = main() 
			Transition transition = extractMainTransition(compile(declAndStatement("int i,j;","i=j;")));
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			Variable v = (Variable)a.getLhs();
			assertTrue(v.getType().equals(Int.create()));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}

	@Test
	public void testLocalSingularInt() {
		try {
			// 
			Transition transition = extractMainTransition(compile(declAndStatement("","int z;z=z;")));
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			Variable v = (Variable)a.getLhs();
			assertTrue(v.getType().equals(Int.create()));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}
	@Test
	public void testLocalSingularIntInitializer() {
		try {
			boolean b = false;
			for(Transition transition : compile(declAndStatement("","int z=42; z=z;")).getProcedure("main").getTransitions()){
				assertTrue(transition instanceof Assignment);
				Assignment a = (Assignment)transition;
				assertTrue(a.getLhs() instanceof Variable);
				Variable v = (Variable)a.getLhs();
				if (a.getRhs() instanceof IntegerConstant)
					if (((IntegerConstant)a.getRhs()).getIntegerConst()==42) b=true;
			}
			assertTrue(b);
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}
	
	@Test
	public void testLocalSeveralInts() {
		try {
			//$1 = main() 
			Transition transition = extractMainTransition(compile(declAndStatement("","int x,y,z;")));
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			Variable v = (Variable)a.getLhs();
			assertTrue(v.getType().equals(Int.create()));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}

	
	@Test
	public void testPointer() {
		try {
			//$1 = main() 
			Transition transition = extractMainTransition(compile(declAndStatement("int (*g)();","g=g;")));
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getRhs() instanceof Variable);
			Variable v = (Variable)a.getRhs();
			System.out.println(v.getType());
			assertTrue(v.getType().equals(new PointerTo(new Function(Int.create(),new LinkedList<>()))));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}

}
