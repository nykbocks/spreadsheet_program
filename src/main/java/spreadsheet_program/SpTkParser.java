package spreadsheet_program;

import java.util.ArrayList;
import java.util.List;

import spreadsheet_program.SpTkTokenizer.Tk;
import spreadsheet_program.SpTkTokenizer.TkE;

public class SpTkParser {
	public enum PrE {
		CALL,
		STRING,
		NUMBER,
		CELLREF,
		RANGE
	}
	public class PrNode {
		public PrNode(PrE type, String value) {
			this.type = type;
			this.value = value;
		}
		public PrE type;
		public String value;
		public List<PrNode> args = new ArrayList<>();
	}
	List<Tk> tokens;
	int peek; // current token
	public PrNode Parse(List<Tk> tokens) {
		this.tokens = tokens;
		this.peek = 0;
		PrNode node = ParseExpression();
		if(Peek() != null) throw new RuntimeException("UNEXPECTED TOKEN AFTER EXPRESSION");
		return node;
	}
	private Tk Peek() {
		if(peek >= tokens.size()) return null;
		Tk t = tokens.get(peek);
		//System.out.println("PEEK "+peek+":"+t.tp);
		
		return t;
	}
	private Tk Advance() {
		Tk t = tokens.get(peek++);
		//System.out.println(t.tp);
		return t;
	}
	private Tk Expect(TkE type) {
		Tk t = Peek();
		if(t == null) {
			throw new RuntimeException("EXPECTED " + type + ",GOT END OF FUNC (NULL)");
		}
		if( type != t.tp) { 
			throw new RuntimeException("EXPECTED " + type + ",GOT " + t.tp);
		}
		//System.out.println("EXPECT "+type+":"+t.tp);	
		return Advance();
	}
	private PrNode ParseExpression() {
		Tk t = Peek();
		switch(t.tp) {
		case NUMBER:
			Advance();
			return new PrNode(PrE.NUMBER,t.value);
		//case STRING:
		case COLON:
			Advance();
			return ParseCall();
		default:
			throw new RuntimeException("UNEXPECTED TOKEN:"+t.value);
		}
	}
	private PrNode ParseCall() {
		PrNode node = new PrNode(PrE.CALL,Expect(TkE.FUNC).value);
		Expect(TkE.OPEN_PTHESES);
		if(Peek().tp != TkE.CLSE_PTHESES) {
			node.args.add(ParseExpression());
			while(Peek().tp == TkE.COMMA) {
				Advance();
				node.args.add(ParseExpression());
			}
		}
		Expect(TkE.CLSE_PTHESES);
		return node;
	}
}
