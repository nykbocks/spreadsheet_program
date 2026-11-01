package spreadsheet_program;

import java.util.ArrayList;
import java.util.List;

public class SpTkTokenizer {
	public enum TkE {
		FUNC,
		OPEN_PTHESES,
		CLSE_PTHESES,
		NUMBER,
		COMMA,
		STRING,
		COLON
	}
	public static class Tk{
		public Tk(TkE a, String b) {
			this.tp = a;
			this.value = b;
		}
		public TkE tp; // type of token
		public String value;
	}
	public static boolean IsNumeric(String str) {
	    if (str == null) {
	        return false;
	    }
	    try {
	        double d = Double.parseDouble(str);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	public TkE EvaluateStr(String s) {
		TkE token = null;
		if(IsNumeric(s)) token = TkE.NUMBER;
		else if(s.equals("("))  token = TkE.OPEN_PTHESES;
		else if(s.equals(")"))  token = TkE.CLSE_PTHESES;
		else if(s.equals(",")) token = TkE.COMMA;
		else if(s.equals(":")) token = TkE.COLON;
		else token = TkE.STRING;
		return token;
	}
	public static void AddToken(List<Tk> list,TkE tp,String value) {
		list.add(new Tk(tp,value));
	}
	public void FlushBuffer(List<Tk> TkList,StringBuilder Buffer,boolean IsFunc) {
		String word = Buffer.toString().trim();
		if(word.isEmpty()) return;
		AddToken(TkList,IsFunc ? TkE.FUNC : EvaluateStr(word),word);
		Buffer.setLength(0);
	}
	public List<Tk> ComputeTokens(String CurTbl) {
		List<Tk> TkList = new ArrayList<>();
		StringBuilder buffer = new StringBuilder();
		for(int i = 0;i < CurTbl.length();i++) {
			char c = CurTbl.charAt(i);
			if(c == ':' || c == '(' || c == ',' || c == ')') {
				FlushBuffer(TkList,buffer,c == '(');
				String cs = String.valueOf(c);
				AddToken(TkList,EvaluateStr(cs),cs);
			} else {
				buffer.append(c);
			}
		}
		return TkList;
	}
	
}
