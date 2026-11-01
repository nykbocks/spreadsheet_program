//package spreadsheet_program;
package spreadsheet_program;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import spreadsheet_program.SpTkParser.PrE;
import spreadsheet_program.SpTkParser.PrNode;
import spreadsheet_program.SpTkTokenizer.Tk;
import spreadsheet_program.SpTkTokenizer.TkE;

public class SpTblHandler implements TableModelListener{
	private void printTree(PrNode n,int d) {
		String rep = "";
		 for(int i=0;i<d;i++) {
		    rep += "  ";
		 }
		System.out.println(rep+"TYPE: "+n.type+"|VALUE: "+n.value);
		for(PrNode a: n.args) {
			printTree(a,d+1);
		}
	}
	public String EvaluateNode(PrNode root) {
		String ret = new String();
		switch(root.value) {
		case "SUM":
			int result = 0;
			for(PrNode a: root.args) {
				switch(a.type) {
				case CALL:
					String s = EvaluateNode(a);
					result += Integer.valueOf(s);
					break;
				case NUMBER:
					result += Integer.valueOf(a.value);
					break;
				default:
					throw new RuntimeException("EXPECTED NUMBER AT "+root.value+"(...,"+a.value+",..)");
				}
				
			}
			ret = String.valueOf(result);
			break;
		}
		return ret;
	}
	@Override
	public void tableChanged(TableModelEvent e) {
		TableModel tbl_data = (TableModel) e.getSource();
		Object str = tbl_data.getValueAt(e.getLastRow(), e.getColumn());
		if(str == null) return;
		String CurTbl = str.toString();
		SpTkTokenizer Tk = new SpTkTokenizer();
		List<Tk> TkList = Tk.ComputeTokens(CurTbl);
		//return TkList;
		
		SpTkParser Parser = new SpTkParser();
		PrNode RootParse = Parser.Parse(TkList);
		//printTree(RootParse,0);
		System.out.println(EvaluateNode((RootParse)));
		/*
		//System.out.println(CurTbl);
		//Parser
		int oldi = 0;
		for(int i = 0;i < CurTbl.length();i++) {
			if(CurTbl.charAt(i) == '(' ||
			   CurTbl.charAt(i) == ',' ||
			   CurTbl.charAt(i) == ')') {
				if(i == 0) return;
				TkStrList.add(CurTbl.substring(oldi, i));
				TkStrList.add(String.valueOf(CurTbl.charAt(i)));
				oldi = i+1;
			}
		}
		if(TkStrList.isEmpty()) return;
		//System.out.println(TkStrList.size());
		for(int i = 0;i < TkStrList.size();i++) {
			String s = TkStrList.get(i);
			switch(s) {
			case "SUM":
				Tk token = new Tk();
				token.args = new ArrayList<>();
				token.func = TkE.SUM;
				
				int j = i+1;
				String ts = "";
				while(ts != ")" && j < TkStrList.size()) {
					ts = TkStrList.get(j).toString();
					//System.out.println(ts);
					token.args.add(ts);
					j++;
				}
				TkList.add(token);
			}
			//System.out.println(s);
		}
		if(TkList.isEmpty()) {
			System.out.println("OUT");
			return;
		}
		for(int i = 0;i<TkList.size();i++) {
			for(int j = 0;j < TkList.get(i).args.size();j++) {
				System.out.print("FUNC:" + TkList.get(i).func.toString() + " | ");
				System.out.println("ARG "+j+":"+TkList.get(i).args.get(j));
			}
		}
		*/
	}
}

