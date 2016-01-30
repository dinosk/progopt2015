package petter.utils;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;
import petter.cfg.State;
import petter.cfg.TransitionFactory;
import petter.cfg.expression.types.Type;

public class SymbolTable{
    /**
     * private IDGenerator for the handled internal variables
     * @author Michael Petter
     */
    public static class IDGenerator {
        private static int id;
        static {
            id = 0;
        }
        static int create(){
            assert id < Integer.MAX_VALUE;
            return id++;
        }
        public static void reset(){
            id=0;
        }
    }

    private HashMap<Integer,String> name= new HashMap<Integer,String>();;
    private int blocktiefe = 0;
    //TODO: statt Tupel ein Tripel, das zusätzlich noch den Typ(Variable, Label, Funktion) speichert
    private Stack<Map<String,Tripel<Integer,Integer,Type>>> stack= new Stack<Map<String,Tripel<Integer,Integer,Type>>>();
    private IDGenerator gen;
    private List<Integer> locals;
    private List<Integer> globals = new ArrayList<Integer>();
    private List<Integer> parameter;

    public SymbolTable(){
        stack.push(new HashMap<String,Tripel<Integer,Integer,Type>>());
    }

    /**
     * internal variable representation is key of HashMap
     * @return current SymbolTable where each variable name is assigned to an internal id
     */
    public Map<Integer,String> getGlobalSymbolTable(){
	return name;
    }

    /**
     * @return current block depth 
     */
    public int getBlockDepth(){
	return blocktiefe;
    }

    /**
     * @param id variable name
     * @return block depth for a variable
     */
    public int getBlockDepth(String id){
	Tripel<Integer, Integer,Type> t = stack.peek().get(id);
	if(t == null) return -1;
	return t.b;
    }

    /**
     * when entering a new block create new locals and parameter list
     * increase blocktiefe
     */
    public void enterBlock(){
		//System.out.println(this.blocktiefe+" enter!");
    	if (++blocktiefe == 1) {
            locals = new ArrayList<>();
            parameter = new ArrayList<>();
            gotos = new HashMap<>();
            labels = new HashMap<>();
        }
        stack.push(new HashMap<>(stack.peek()));
    }
    
   /**
     * when leaving a block delete locals and parameter list
     * decrease blocktiefe
     */
    public void leaveBlock(){
		//System.out.println(this.blocktiefe+" leave!");
    	if (blocktiefe-- == 1) {
//            locals=null;
//            parameter = null;
            if (!gotos.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String str : gotos.keySet()) {
                    sb.append(str);sb.append(", ");
                }
                throw new RuntimeException("Could not resolve gotos to the labels "+sb.toString());
            }
//            gotos = null;
//            labels = null;
        }
        undo = stack.pop();
    }
	private Map<String, Tripel<Integer, Integer, Type>> undo;
	public void undoLeave(){
		//System.out.println(this.blocktiefe+" undo!");
		if (undo!=null){
			++blocktiefe;
			stack.push(undo);
		}
		else throw new RuntimeException("Cannot undo twice in a row");
		undo=null;
	}
	
    
    //TOOD: int newLocal(String name, int type);
    Type typecache;
    /**
     * enter a new local/global variable
     * @param name of the identifier
     * @return internal number for this identifier
     */
    public int newLocal(String name) throws Exception{
	Tripel<Integer, Integer,Type> t =stack.peek().get(name); 
        if ((t != null) && (t.b>=blocktiefe)) throw new Exception("Identifier "+name+" already declared as "+t.c);
        int id = gen.create();
        this.name.put(id,name);
        Tripel entry = new Tripel<Integer,Integer,Type>(id,blocktiefe,typecache);
        stack.peek().put(name,entry);
        if (blocktiefe==0) globals.add(id);
        else locals.add(id);
        //System.out.println(this.blocktiefe+"Registered global/local "+name);

        return id;
    }
    /**
     * Newly introduced variables need to have a type assigned - c-type declarations suck!
     * @param t Type for the newest introduced variables 
     */
    public void setLastParsedType(Type t){
        typecache = t;
    }
    public Type getLastParsedType(){ return typecache; }
    /**
     * receive a temporary for this block
     * @return the internal number for this temporary
     */
    public int newTemporary(Type t){
        int id = gen.create();
        stack.peek().put("$"+id,new Tripel<Integer,Integer,Type>(id,blocktiefe,t));
        name.put(id,"$"+id);
        locals.add(id);
	return id;
    }
    /**
     * enter a new formal parameter
     * @param name of the identifier
     * @return internal number for this identifier
     */
    public int newParameter(String name,Type typ) throws Exception{
	Tripel<Integer, Integer,Type> t =stack.peek().get(name); 
        if ((t != null) && (t.b>=blocktiefe)) throw new Exception("Identifier "+name+" already declared as variable");
        int id = gen.create();
        this.name.put(id,name);
        stack.peek().put(name,new Tripel<Integer,Integer,Type>(id,blocktiefe,typ));
        parameter.add(id);
        //System.out.println(this.blocktiefe+"Registered Parameter "+name);
        return id;
        
    }

    /**
     * get external presentation of an internal identifier
     * @param id internal representation to query
     * @return external presentation, null if not declared
     */
    public String getName(int id){
	assert id >= 0 :"only positive values are valid as internal representation";
        String name = this.name.get(id);
        return name;
    }
    /**
     * get external presentation of an internal identifier
     * @param name official name in this scope
     * @return internal ID, -1 if not declared
     */
    public int getId(String name){
        Tripel<Integer,Integer,Type> t = stack.peek().get(name);
        if (t==null) return -1;
        return t.a;
    }
    /**
     * get type for the identifier
     * @param name
     * @return null if no name like this is found
     */
    public Type getType(String name){
        Tripel<Integer,Integer,Type> t = stack.peek().get(name);
        if (t==null) {
            return null;
        }
        return t.c;
    }
    public Type getType(int id){
        return getType(name.get(id));
    }

    /**
     * get List of all global variables
     * @return internal presentation
     */
    public List<Integer> getGlobals(){
	return globals;
    }
    /**
     * get List of all local variables
     * @return internal presentation
     */
    public List<Integer> getLocals(){
	return locals;
    }
    /**
     * get List of all parameters
     * @return internal presentation
     */
    public List<Integer> getParameters(){
	return parameter;
    }
    private Map<String,State> labels = new HashMap<>();
    public void enterLabel(String id, State s) throws Exception {
        if (labels.containsKey(id)) throw new Exception("Label "+id+" exists already");
        labels.put(id, s);
        if (gotos.containsKey(id)){
            for (State st: gotos.get(id))
                TransitionFactory.createNop(st, s);;
            gotos.remove(id);
        }
    }
    public State getStateForLabel(String id){
        return labels.get(id);
    }
    private Map<String,List<State>> gotos= new HashMap<>();
    public void registerGoto(String id,State src){
        if (!gotos.containsKey(id)) gotos.put(id, new LinkedList<>());
        gotos.get(id).add(src);
    }
}


