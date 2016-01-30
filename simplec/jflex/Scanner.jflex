package petter.simplec;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import petter.utils.AnnotatingSymbolFactory.ExtendedLocation;
import petter.utils.Terminal;

%%

%class Lexer
%cup
%unicode
%cupdebug
%implements sym
%char
%line
%column
%public
%{
  private boolean reportComments=false;
  public Lexer(java.io.InputStream in, ComplexSymbolFactory sf) {
    this(in,sf,false);
  }
  public Lexer(java.io.InputStream in, ComplexSymbolFactory sf, boolean reportComments) {
    this(new java.io.InputStreamReader(in));
    symbolFactory = sf;
    this.reportComments = reportComments;
  }
    ComplexSymbolFactory symbolFactory;

  private Symbol symbol(String name, int sym) {
    return symbolFactory.newSymbol(name, sym, new ExtendedLocation(yyline+1,yycolumn+1,yychar), new ExtendedLocation(yyline+1,yycolumn+yylength(),yychar+yylength()));
  }
  
  private Symbol symbol(String name, int sym, Terminal val) {
      ExtendedLocation left = new ExtendedLocation(yyline+1,yycolumn+1,yychar);
      ExtendedLocation right=	  new ExtendedLocation(yyline+1,yycolumn+yylength(), yychar+yylength());
      val.putAnnotation("__location_left", left);
      val.putAnnotation("__location_right", right);
      return symbolFactory.newSymbol(name, sym, left, right,val);
  }       
  
  private void error(String message) {
    System.err.println("Error at line "+(yyline+1)+", column "+(yycolumn+1)+" : "+message);
  }
%} 
%eofval{
     return symbolFactory.newSymbol("EOF", EOF, new ExtendedLocation(yyline+1,yycolumn+1,yychar), new ExtendedLocation(yyline+1,yycolumn+1,yychar+1));
%eofval}
Ident = ([:jletter:] | "_" ) ([:jletterdigit:] | [:jletter:] | "_" )*
TraditionalComment = "/*" {CommentContent} \*+ "/"
EndOfLineComment = "//" [^\r\n]* {new_line}
CommentContent = ( [^*] | \*+[^*/] )*
Comment = {TraditionalComment} | {EndOfLineComment}
IntLiteral = 0 | [1-9][0-9]*

new_line = \r|\n|\r\n

white_space = {new_line} | [ \t\f]

%%

{Comment}         { if (reportComments) return symbol("comment",COMMENT); else yylength(); }

/* keywords */
"int"             { return symbol("int",     INT); }
"char"            { return symbol("char",   CHAR); }
"double"          { return symbol("double",   DOUBLE); }
"float"           { return symbol("float",   FLOAT); }
"long"            { return symbol("long",   LONG); }
"short"           { return symbol("short",   SHORT); }
"void"            { return symbol("void",    VOID ); }
"return"          { return symbol("return",  RETURN); }
"break"           { return symbol("break",   BREAK); }
"continue"        { return symbol("continue",CONTINUE); }
"goto"            { return symbol("goto",    GOTO); }
"switch"          { return symbol("switch",     SWITCH); }
"case"            { return symbol("case",    CASE); }
"default"         { return symbol("default", DEFAULT); }
"pragma"          { return symbol("pragma",  PRAGMA); }
"typedef"         { return symbol("typedef", TYPEDEF); }

/* control flow */
"for"             { return symbol("for",  FOR); }
"do"              { return symbol("do",   DO); }
"while"           { return symbol("while",  WHILE); }
"if"              { return symbol("if",  IF); }
"else"            { return symbol("else",  ELSE); }
","               { return symbol("Comma",COMMA); }
"#"               { return symbol("Hash", HASH); }
"{"               { return symbol("Left Bracket",BEGIN); }
"}"               { return symbol("Right Bracket",END); }
"("               { return symbol("Left paranthesis",LPAR); }
")"               { return symbol("Right paranthesis",RPAR); }
"["               { return symbol("Left square bracket",LSQ); }
"]"               { return symbol("Right square bracket",RSQ); }
";"               { return symbol("Semi colon",SEMI); }

/* literals */
{IntLiteral}      { return symbol("Integer Constant", INTCONST, new Terminal<Integer>(Integer.parseInt(yytext()))); }

/* unknown expression */
"?"               { return symbol("Question mark",QUESTIONMARK); }

/* separators */
":"               { return symbol("COLON operator", COLON); }
/* arith operators */
"+="               { return symbol("PLUSEQ operator", PLUSEQ); }
"-="               { return symbol("MINUSEQ operator", MINUSEQ); }
"*="               { return symbol("MULEQ operator",MULEQ); }
"/="               { return symbol("DIVEQ operator",DIVEQ); }

"++"              { return symbol("Increment",INCOP); }
"--"              { return symbol("Decrement",DECOP); }
"+"               { return symbol("Plus operator",ADDOP); }
"-"               { return symbol("Minus operator",SUBOP); }
"*"               { return symbol("Times operator",MULOP); }
"/"               { return symbol("Divide operator",DIVOP); }
"&"               { return symbol("Address-Of operator",ADDOFOP); }



/* bool operators */
"=="              { return symbol("Equals operator",EQ); }
"||"              { return symbol("Or operator",OR); }
"&&"              { return symbol("And operator",AND); }
">="              { return symbol("Greater or equals operator",GEQ); }
"<="              { return symbol("Less or equals operator",LEQ); }
"!="              { return symbol("Not equals operator",NEQ); }
"!"               { return symbol("Not operator",NOT); }
"<"               { return symbol("Less operator",LT); }
">"               { return symbol("Greater operator",GT); }
"="               { return symbol("Assignment operator",ASSIGN); }


{white_space}     { /* ignore */ yylength(); }


/* names */
{Ident}           { return symbol("Identifier",IDENT, new Terminal<String>(yytext())); }



/* error fallback */
.|\n              {  /* throw new Error("Illegal character <"+ yytext()+">");*/
		    return symbol("Illegal character (sequence): \""+ yytext()+"\"",ILLEGAL);
                  }
