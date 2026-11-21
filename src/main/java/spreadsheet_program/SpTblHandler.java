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

	@Override
	public void tableChanged(TableModelEvent e) {
		TableModel tbl_data = (TableModel) e.getSource();
		Cell cell = (Cell) tbl_data.getValueAt(e.getLastRow(), e.getColumn());
		SpTables.ReCalcCell(e.getLastRow(),e.getColumn(),new java.util.HashSet<>());
		if(SpTables.Tbl != null) SpTables.Tbl.repaint();
		/*
		if(cell == null) return;
		if(cell.exp == null || cell.exp.isEmpty()) {
			cell.out = "";
			return;
		}
		
		CheckDeps(cell,tbl_data);
		if (SpTables.Tbl != null) SpTables.Tbl.repaint();
		if(!cell.exp.startsWith(":")) {
			cell.out = cell.exp;
			return;
		}
		SpTables.CallFunc(e.getLastRow(),e.getColumn());*/
	}
}

