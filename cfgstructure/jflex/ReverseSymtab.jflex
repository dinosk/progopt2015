package de.tum.in.wwwseidl.programanalysis.cfg;
import java.io.StringReader;
import java.io.IOException;
%%
%class ReverseSymtab
%public
%unicode
%standalone
%type String
%eofval{
	return "";
%eofval}
%{
	private SymbolTable symtab;
	public ReverseSymtab(String input, SymbolTable symtab){
	       this.symtab = symtab;
	       this.yy_reader = new StringReader(input);
	}
	public static String getLine(String input, SymbolTable symtab){
	       StringBuffer sb = new StringBuffer();
	       ReverseSymtab rs = new ReverseSymtab(input,symtab);
	       try {
		   while (!rs.yy_atEOF) sb.append(rs.yylex());
		   }catch (IOException ioe) {};
	       return sb.toString();
	}
%}
%%

"var"[0-9]+  { String original = yytext(); String name = symtab.getName(original); return (name!=null)?name:original; }
.|\n         { return yytext(); }