//package spreadsheet_program;
package spreadsheet_program;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import spreadsheet_program.SpTables.Cell;
import spreadsheet_program.SpTkParser.PrE;
import spreadsheet_program.SpTkParser.PrNode;
import spreadsheet_program.SpTkTokenizer.Tk;
import spreadsheet_program.SpTkTokenizer.TkE;

public class SpTblHandler implements TableModelListener{

	public String GetCellRef(String at,TableModel tbl) {
		String letters = "";
		int row = 0;
		int col = 0;
		int i = 1;
		for(i = 1;i < at.length();i++) {
			letters = at.substring(0, i);
			if(!letters.matches("[A-Z]+")) {
				break;
			}
		}
		i--;
		letters = at.substring(0,i);
		if(i == at.length()) throw new RuntimeException("CELL REFERENCE CONTAINS NO ROW INDEX: "+at);
		for(int j = 0;j < letters.length();j++) {
			int cur = Character.valueOf(letters.charAt(j)) - Character.valueOf('A')+1;
			cur *= Math.pow(26, letters.length()-j-1);
			col += cur;
		}
		
		col--; // 0-based column-indexing;
		row = Integer.valueOf(at.substring(i));
		row--;
		System.out.println(row);
		Cell c = (Cell) tbl.getValueAt(row, col);
		return (c == null || c.out.isEmpty()) ? "0" : c.out;
	}
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
	public String EvaluateNode(PrNode root,TableModel tbl_data) {
		String ret = new String();
		switch(root.value) {
		case "SUM":
			int result = 0;
			for(PrNode a: root.args) {
				switch(a.type) {
				case CALL:
					String s = EvaluateNode(a,tbl_data);
					result += Integer.valueOf(s);
					break;
				case NUMBER:
					result += Integer.valueOf(a.value);
					break;
				case CELLREF:
					result += Integer.valueOf(GetCellRef(a.value, tbl_data));
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
		Cell cell = (Cell) tbl_data.getValueAt(e.getLastRow(), e.getColumn());
		if(cell == null) return;
		if(cell.exp == null || cell.exp.isEmpty()) {
			cell.out = "";
			return;
		}
		if(!cell.exp.startsWith(":")) {
			cell.out = cell.exp;
			return;
		}
		String CurTbl = cell.exp;
		SpTkTokenizer Tk = new SpTkTokenizer();
		List<Tk> TkList = Tk.ComputeTokens(CurTbl);
		for(int i = 0;i < TkList.size();i++) {
			System.out.println("TYPE:"+TkList.get(i).tp+" VALUE:"+TkList.get(i).value);
		}
		//return TkList;
		
		SpTkParser Parser = new SpTkParser();
		PrNode RootParse = Parser.Parse(TkList);
		if(cell.exp.startsWith(":")) {
			try {
				cell.out = EvaluateNode(RootParse,tbl_data);
			} catch (RuntimeException ex){
				cell.out = "#ERROR";
			}
		} else {
			cell.out = cell.exp;
		}
	}
}

