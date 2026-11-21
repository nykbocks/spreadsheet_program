//package spreadsheet_program;
package spreadsheet_program;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import java.io.Reader;
import javax.swing.AbstractCellEditor;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.w3c.dom.Text;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import spreadsheet_program.SpTables.Cell;
import spreadsheet_program.SpTkParser.PrNode;
import spreadsheet_program.SpTkTokenizer.Tk;
import spreadsheet_program.SpTkTokenizer.TkE;
public class SpTables {
	public class Cell {
		String out; // output; is read-only (only EvaluateNode must change it)
		String exp; // expression; (may be just a string)
		List<String> deps = new ArrayList<>(); // depedencies; functions that use this cell  
		@Override
		public String toString() {
			return out;
		}
	}
	//Tables
	private Gson gson;
	public static AbstractTableModel TblDataModel;
	public static JTable Tbl;
	public JScrollPane TblScroll;
	public static Cell[][] TblData;
	public SpTblHandler TblHandler;
	public static int TblW = 100; 
	public static int TblH = 100;
	public static JList<String> TblRows;
	public void SetZoom(int z) {
		Font zoom = new Font("Arial",Font.PLAIN,z);
		TblRows.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(
					JList<? extends Object> list,
					Object value,
					int index,
					boolean isSelected,
					boolean cellHasFocus) 
				{				
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
				label.setFont(zoom);
				label.setHorizontalAlignment(CENTER);
				label.setOpaque(true);
				label.setBackground(UIManager.getColor("TableHeader.background"));
				label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.black));
				return label;
			}
	    });
		int h = (int)TblRows.getFontMetrics(zoom).getStringBounds("100", TblRows.getGraphics()).getHeight();
		int w = (int)TblRows.getFontMetrics(zoom).getStringBounds("1000", TblRows.getGraphics()).getWidth();
		FontMetrics font_metrics = TblRows.getFontMetrics(zoom);
		
		TblRows.setFixedCellHeight(h);
		TblRows.setFixedCellWidth(w);
		Tbl.setFont(zoom);
		Tbl.setRowHeight(h);
		Tbl.getTableHeader().setFont(zoom);
		int cw = font_metrics.stringWidth("MMM")+16;
		TableColumnModel m = Tbl.getColumnModel();
		for(int i = 0; i< m.getColumnCount();i++) {
			TableColumn col = m.getColumn(i);
			col.setPreferredWidth(cw);
		}
		
	}
	public static String ColName(int col) {
	    StringBuilder sb = new StringBuilder();
	    for (col++; col > 0; col = (col - 1) / 26)
	        sb.append((char) ('A' + (col - 1) % 26));
	    return sb.reverse().toString();
	}
	public static void CollectRefs(PrNode node,List<String> out){
		switch(node.type) {
		case CELLREF:
			out.add(node.value);
			break;
		case RANGE:
			for(String s: GetCellRangeRefs(TblDataModel,node.rangeStart,node.rangeEnd)) {
				System.out.println(s);
				out.add(s);
			}
			break;
		case CALL:
			for(PrNode arg: node.args) {
				CollectRefs(arg,out);
			}
			break;
		default:
			break;
		}
	}
	public static void CallFunc(int r,int c) {
		Cell cell = TblData[r][c];
		String CurTbl = cell.exp;
		if(!cell.exp.startsWith(":")) {
			cell.out = cell.exp;
			return;
		}
		SpTkTokenizer Tk = new SpTkTokenizer();
		List<Tk> TkList = Tk.ComputeTokens(CurTbl);
		SpTkParser Parser = new SpTkParser();
		PrNode RootParse = Parser.Parse(TkList);
		//get self cell reference
		String at = ColName(c) + (r+1);
		List<String> refs = new ArrayList<>();
		CollectRefs(RootParse,refs);
		for(String cellref: refs) {
			int ar = GetCellRefROW(cellref,TblDataModel);
			int ac = GetCellRefCOL(cellref,TblDataModel);
			Cell cur = GetCell(ar,ac);
			if (cur.equals(at)) throw new RuntimeException("CELL REFERENCES ITSELF");
			if (cur != null && !cur.deps.contains(at)) cur.deps.add(at.toString());
		}
		if(cell.exp.startsWith(":")) {
			try {
				cell.out = SpFunctions.EvaluateNode(RootParse,TblDataModel);
			} catch (RuntimeException ex){
				cell.out = "#ERROR";
			}
		} else {
			cell.out = cell.exp;
		}
	}
	public static void ReCalcCell(int r,int c,java.util.Set<String> visiting) {
		String cur = ColName(c) + (r+1);
		//cant add ourself to the visiting list (in a loop)
		if(!visiting.add(cur)) {
			TblData[r][c].out = "#CYCLE";
			return;
		}
		CallFunc(r,c);
		for(String dep: TblData[r][c].deps) {
			int dr = GetCellRefROW(dep,TblDataModel);
			int dc = GetCellRefCOL(dep,TblDataModel);
			ReCalcCell(dr,dc,visiting);
		}
		visiting.remove(cur);
	}
	public static int GetCellRefCOL(String at,TableModel tbl) {
		String letters = "";
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
		if(i == at.length()) throw new RuntimeException("NOT A CELL INDEX: "+ at);
		for(int j = 0;j < letters.length();j++) {
			int cur = Character.valueOf(letters.charAt(j)) - Character.valueOf('A')+1;
			cur *= Math.pow(26, letters.length()-j-1);
			col += cur;
		}
		col--; // 0-based column-indexing;
		return col;
	}
	public static int GetCellRefROW(String at,TableModel tbl) {
		String letters = "";
		int row = 0;
		int i = 1;
		for(i = 1;i < at.length();i++) {
			letters = at.substring(0, i);
			if(!letters.matches("[A-Z]+")) {
				break;
			}
		}
		i--;
		letters = at.substring(0,i);
		row = Integer.valueOf(at.substring(i));
		row--;
		return row;
	}
	public static List<String> GetCellRangeRefs(TableModel data,String rSta,String rEnd){
		List<String> list = new ArrayList<>();
		int rsCol = GetCellRefCOL(rSta,data);
		int rsRow = GetCellRefROW(rSta,data);
		int reCol = GetCellRefCOL(rEnd,data);
		int reRow = GetCellRefROW(rEnd,data);
		for(int row = rsRow;row <= reRow;row++) {
			for(int col = rsCol;col <= reCol;col++) {
				list.add(ColName(col) + (row+1));
			}
		}
		return list;
	}
	public static List<String> GetCellRange(TableModel data,String rSta,String rEnd){
		List<String> list = new ArrayList<>();
		int rsCol = GetCellRefCOL(rSta,data);
		int rsRow = GetCellRefROW(rSta,data);
		int reCol = GetCellRefCOL(rEnd,data);
		int reRow = GetCellRefROW(rEnd,data);
		for(int row = rsRow;row <= reRow;row++) {
			for(int col = rsCol;col <= reCol;col++) {
				list.add(data.getValueAt(row, col).toString());
			}
		}
		return list;
	}
	public static String GetCellRef(String at,TableModel tbl) {
		int row = SpTables.GetCellRefROW(at,tbl);
		int col = SpTables.GetCellRefCOL(at,tbl);
		Cell c = (Cell) tbl.getValueAt(row, col);
		return (c == null || c.out.isEmpty()) ? "0" : c.out;
	}
	public static Cell GetCell(int r,int c) {
		if(r > TblH || r < 0) return null;
		if(c > TblW || c < 0) return null;
		return TblData[r][c]; 
	}
	public String GetFilePath() {
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter ef = new FileNameExtensionFilter("SP Files", "json","sp");
		fc.setFileFilter(ef);
		fc.setCurrentDirectory(new java.io.File("."));
		fc.setLocation(0, 0);
		int rfc = fc.showOpenDialog(Tbl);
		String path = null;
		if(rfc == JFileChooser.APPROVE_OPTION) {
			path = fc.getSelectedFile().getAbsolutePath();
		}
		return path;
	}
	/*
	 * On the Load and Save methods:
	 * 
	 * This is not the best way to do it,
	 * it would be more clever to use a database 
	 * to store the cells the user has changed,
	 * because this approach does not scale
	 * well. For large spreadsheets with more
	 * than 100x100 sized tables, it can load 
	 * a lot of memory into ram even it is 
	 * mostly empty.
	 * 
	 * TODO: Make this use a database (SQLite,probably)
	 * to query and save the spreadsheet. Not now,
	 * as it is a small project.
	 * 
	 * Also,as we refactored the cell structure,the functions
	 * run even when their arguments have not loaded,i am going
	 * to fix it now.
	 */
	public void saveFileAs(String fileloc) {
		String path = GetFilePath();
		if(path == null) return;
		try (Writer w = new FileWriter(path)){
			List<List<Object>> data = new ArrayList<>();
			for(int i = 0;i < Tbl.getColumnCount(); i++) {
				List<Object> row = new ArrayList<>();
				for(int j = 0;j < Tbl.getRowCount(); j++) {
					Cell a = (Cell) Tbl.getValueAt(j, i);
					if(a == null) continue;
					row.add(a.exp.toString());
				}
				//if(row.isEmpty()) continue;
				data.add(row);
			}
			gson.toJson(data, w);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void loadFrom(String fileloc) {
		String path = GetFilePath();
		if(path == null) return;
		try (Reader r = new FileReader(path)){
			List<List<Object>> data = new ArrayList<>();
			Type type = new TypeToken<List<List<Object>>>(){}.getType();
			data = gson.fromJson(r, type);
			//this is genuinly so bad kek
			for(int i = 0;i < data.size(); i++) {
				if(data.get(i).size() == 0) continue;
				for(int j = 0;j < data.get(i).size(); j++) {
					TblData[j][i].exp = data.get(i).get(j).toString();
				}
			}
			//refresh cells outputs 
			for(int row = 0; row < TblH;row++) {
				for(int col = 0; col < TblW;col++) {
					TblDataModel.fireTableCellUpdated(row, col);
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	private class EditDialog extends AbstractCellEditor implements TableCellEditor{
		private String value = "";
		@Override
		public Object getCellEditorValue() {
			return value;
		}
		@Override
		public Component getTableCellEditorComponent(
				JTable table,
				Object val,
				boolean isSelected,
				int row,
				int column)
		{
				value = (val == null) ? "" : val.toString();
			SwingUtilities.invokeLater(() ->{
					JTextArea text = new JTextArea(value,20,50);
					//int finish = JOptionPane.showConfirmDialog(table, "Finish editing","test",JOptionPane.YES_NO_OPTION);
					int finish = JOptionPane.showConfirmDialog(table, new JScrollPane(text),"Edit window", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
					text.requestFocus();
					if(finish == JOptionPane.OK_OPTION) {
						//table.setValueAt(text.getText(), row, column);
						value = text.getText();
						this.fireEditingStopped();
					} else {
						this.fireEditingCanceled();
					}
				}
			);
			return new JTable();
		}
	}
	static void OpenEditDialog(int row,int col) {
	    Cell current = TblData[row][col];
	    JTextArea area = new JTextArea(current == null ? "" : current.exp, 20, 50);
	    area.setLineWrap(true);
	    area.setWrapStyleWord(true);
	    area.setFont(new Font("Arial",Font.BOLD,20));
	    int result = JOptionPane.showConfirmDialog(
	            SwingUtilities.getWindowAncestor(Tbl),
	            new JScrollPane(area),
	            "Edit " + Tbl.getColumnName(col) + (row+1),  
	            JOptionPane.OK_CANCEL_OPTION,
	            JOptionPane.PLAIN_MESSAGE);

	    if (result == JOptionPane.OK_OPTION) {
	        Tbl.setValueAt(area.getText(), row, col);      
	    }
	}
	private class ColListener implements MouseListener{
		@Override
		public void mouseClicked(MouseEvent e) {
			JTableHeader t = Tbl.getTableHeader();
			int c = t.columnAtPoint(e.getPoint());
			if(c > -1) {
				int icol = Tbl.convertColumnIndexToModel(c);
				if(e.getClickCount() == 2) {
					TableColumn col = Tbl.getColumnModel().getColumn(icol);
					int biggest = 0;
					int j = 0;
					for(int i = 0; i < TblH; i++) {
						if(TblData[i][icol] == null) continue;
						if(TblData[i][icol].out.length() > biggest) {
							biggest = TblData[i][icol].out.length();
							j = i;
						}
					}
					double w = Tbl.getFontMetrics(Tbl.getFont()).getStringBounds(TblData[j][icol].out, Tbl.getGraphics()).getWidth();
					//System.out.println(w);
					col.setPreferredWidth((biggest == 0) ? 75 : (int)Math.ceil(w)+10);
				}
			}
		}
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub	
		}
	}
	private class TblListener implements MouseListener{
		@Override
		public void mouseClicked(MouseEvent e) {
			int row = Tbl.rowAtPoint(e.getPoint());
			int col = Tbl.columnAtPoint(e.getPoint());
			Cell get = TblData[row][col];
			if (e.getClickCount() == 2) {
				if (row < 0 || col < 0) return;
				OpenEditDialog(row, col);
				return;
			}
			String val = (get.exp == null ? "": get.exp);
			if(val == null) return;
		}
		@Override
		public void mouseEntered(MouseEvent arg0) {
		}
		@Override
		public void mouseExited(MouseEvent arg0) {
		}
		@Override
		public void mousePressed(MouseEvent arg0) {
		}
		@Override
		public void mouseReleased(MouseEvent arg0) {
		}
		
	}
	SpTables() {	
		gson = new GsonBuilder().setPrettyPrinting().create();
		SpKeys keys = new SpKeys();
		TblHandler = new SpTblHandler();
		TblData = new Cell[TblH][TblW];
		for(int i = 0;i<TblH;i++) {
			for(int j = 0;j<TblW;j++) {
				TblData[i][j] = new Cell();
				TblData[i][j].out = new String();
				TblData[i][j].exp = new String();
			}
		}
		TblDataModel = new AbstractTableModel() {
			@Override
	    	public int getColumnCount() { 
	    		return TblW; 
	    	}
			@Override
	    	public int getRowCount() { 
	    		return TblH;
	    	}
			@Override
	        public Object getValueAt(int row, int col) { 
	        	return TblData[row][col]; 
	        }
			@Override
			public boolean isCellEditable(int row,int col) {
				return false;
			}
			@Override
			public void setValueAt(Object value,int row,int col) {
				TblData[row][col].exp = value.toString();
		        this.fireTableCellUpdated(row, col);
			}
			
	    };

	    TblDataModel.addTableModelListener(TblHandler);
	    // ONLY FOR TESTING!!! 
	    // ONLY FOR TESTING!!! end
	    Tbl = new JTable(TblDataModel);
	    //Tbl.setDefaultEditor(Object.class, new EditDialog());
	    Tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    TblScroll = new JScrollPane(Tbl);
	    Tbl.getTableHeader().setReorderingAllowed(false);
	    Tbl.getTableHeader().addMouseListener(new ColListener());
	    Tbl.addMouseListener(new TblListener());
	    Tbl.addKeyListener(keys);
	    TblRows = new JList<>(new AbstractListModel<String>() {
			@Override
			public int getSize() {
				return Tbl.getRowCount();
			}
			@Override
			public String getElementAt(int index) {
				return String.valueOf(index+1);
			}
		});
	    TblRows.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(
					JList<? extends Object> list,
					Object value,
					int index,
					boolean isSelected,
					boolean cellHasFocus) 
				{				
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
				label.setFont(Tbl.getFont());
				label.setHorizontalAlignment(CENTER);
				label.setOpaque(true);
				label.setBackground(UIManager.getColor("TableHeader.background"));
				label.setBorder(BorderFactory.createMatteBorder(0, 0, 1 , 1, Color.black));
				return label;
			}

	    });
	    TableColumnModel m = Tbl.getColumnModel();
	    for(int i = 0 ; i < m.getColumnCount();i++) {
	    	m.getColumn(i).setIdentifier(i);
	    }
	    TblRows.setFixedCellWidth(50);
	    TblRows.setFixedCellHeight(Tbl.getRowHeight());
	    TblScroll.setRowHeaderView(TblRows);
	    Tbl.setDragEnabled(false);
	    TblDataModel.setValueAt("1", 0, 0);
	    TblDataModel.setValueAt("2", 0, 1);
	    TblDataModel.setValueAt("3", 1, 0);
	    TblDataModel.setValueAt("4", 1, 1);
	    TblDataModel.setValueAt(":SUB([A1,B2],0)", 0, 5);
	    TblDataModel.setValueAt("BUTTON,A5", 2, 5);
	}
}
