package de.tum.in.wwwseidl.programanalysis.cfg;
import java.io.StringReader;
import java.io.IOException;
%%
%class StraightSymtab
%public
%unicode
%standalone
%type String
%eofval{
	return "";
%eofval}
%{
	private SymbolTable symtab;
	public StraightSymtab(String input, SymbolTable symtab){
	       this.symtab = symtab;
	       this.yy_reader = new StringReader(input);
	}
	public static String getLine(String input, SymbolTable symtab){
	       StringBuffer sb = new StringBuffer();
	       StraightSymtab ss = new StraightSymtab(input,symtab);
	       try {
		   while (!ss.yy_atEOF) {
		      String s = ss.yylex();
		      if (s==null) return null;
		      sb.append(s);
		      }
		   }catch (IOException ioe) {};
	       return sb.toString();
	}
%}
%%

[a-zA-Z_][0-9a-zA-Z_]*  { return symtab.getInternal(yytext()); }
.|\n           { return yytext(); }