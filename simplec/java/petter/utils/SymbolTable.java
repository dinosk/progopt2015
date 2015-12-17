package petter.utils;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Stack;

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
    private Stack<Map<String,Tupel<Integer,Integer>>> stack= new Stack<Map<String,Tupel<Integer,Integer>>>();
    private IDGenerator gen;
    private List<Integer> locals;
    private List<Integer> globals = new ArrayList<Integer>();
    private List<Integer> parameter;


    public SymbolTable(){
        stack.push(new HashMap<String,Tupel<Integer,Integer>>());
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
	Tupel<Integer, Integer> t = stack.peek().get(id);
	if(t == null) return -1;
	return t.b;
    }

    /**
     * when entering a new block create new locals and parameter list
     * increase blocktiefe
     */
    public void enterBlock(){
        if (++blocktiefe == 1) {
            locals = new ArrayList<Integer>();
            parameter = new ArrayList<Integer>();
        }
        stack.push(new HashMap<String,Tupel<Integer,Integer>>(stack.peek()));
    }
    
   /**
     * when leaving a block delete locals and parameter list
     * decrease blocktiefe
     */
    public void leaveBlock(){
        if (blocktiefe-- == 1) {
            locals=null;
            parameter = null;
        }
        stack.pop();
    }

    //TOOD: int newLocal(String name, int type);
    /**
     * enter a new local/global variable
     * @param name of the identifier
     * @return internal number for this identifier
     */
    public int newLocal(String name) throws Exception{
	Tupel<Integer, Integer> t =stack.peek().get(name); 
        if ((t != null) && (t.b>=blocktiefe)) throw new Exception("Identifier "+name+" already declared as variable");
        int id = gen.create();
        this.name.put(id,name);
        stack.peek().put(name,new Tupel<Integer,Integer>(id,blocktiefe));
        if (blocktiefe==0) globals.add(id);
        else locals.add(id);
        return id;
    }
    /**
     * receive a temporary for this block
     * @return the internal number for this temporary
     */
    public int newTemporary(){
        int id = gen.create();
        stack.peek().put(".temporary"+id,new Tupel<Integer,Integer>(id,blocktiefe));
        name.put(id,".temporary"+id);
        locals.add(id);
	return id;
    }
    /**
     * enter a new formal parameter
     * @param name of the identifier
     * @return internal number for this identifier
     */
    public int newParameter(String name) throws Exception{
	Tupel<Integer, Integer> t =stack.peek().get(name); 
        if ((t != null) && (t.b>=blocktiefe)) throw new Exception("Identifier "+name+" already declared as variable");
        int id = gen.create();
        this.name.put(id,name);
        stack.peek().put(name,new Tupel<Integer,Integer>(id,blocktiefe));
        parameter.add(id);
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
        Tupel<Integer,Integer> t = stack.peek().get(name);
        if (t==null) return -1;
        return t.a;
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
}


