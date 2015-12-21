package petter.cfg.expression;

public class MethodCallExpressionAnalysis extends AbstractExpressionVisitor{

	protected boolean defaultBehaviour(Expression e){
	   return true;
    }
    
	public boolean preVisit(MethodCall s){
		
	}
	public boolean preVisit(IntegerConstant s){return defaultBehaviour(s);}
	public boolean preVisit(Variable s){return defaultBehaviour(s);}
	public boolean preVisit(MethodCall s){return defaultBehaviour(s);}
	public boolean preVisit(UnknownExpression s){return defaultBehaviour(s);}
	public boolean preVisit(UnaryExpression s){return defaultBehaviour(s);}
	public boolean preVisit(BinaryExpression s){return defaultBehaviour(s);}
}