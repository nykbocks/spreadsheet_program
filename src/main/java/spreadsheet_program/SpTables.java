//package spreadsheet_program;
package spreadsheet_program;

import java.awt.Color;
import java.awt.Component;
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
public class SpTables {
	public class Cell {
		String out; // output; is read-only (only EvaluateNode must change it)
		String exp; // expression; (may be just a string)
		@Override
		public String toString() {
			return out;
		}
	}
	//Tables
	private Gson gson;
	public AbstractTableModel TblDataModel;
	public JTable Tbl;
	public JScrollPane TblScroll;
	public Cell[][] TblData;
	public SpTblHandler TblHandler;
	public int TblW = 100; 
	public int TblH = 100;
	public JList<String> TblRows;
	public String GetCellRef(String at) {
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
		return TblData[row][col].exp;
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
	private void OpenEditDialog(int row,int col) {
		
	    Cell current = TblData[row][col];
	    JTextArea area = new JTextArea(current == null ? "" : current.exp, 20, 50);
	    area.setLineWrap(true);
	    area.setWrapStyleWord(true);
	    int result = JOptionPane.showConfirmDialog(
	            Tbl,
	            new JScrollPane(area),
	            "Edit " + Tbl.getColumnName(col) + row+1,   // your row header starts at 0, so no +1
	            JOptionPane.OK_CANCEL_OPTION,
	            JOptionPane.PLAIN_MESSAGE);

	    if (result == JOptionPane.OK_OPTION) {
	        Tbl.setValueAt(area.getText(), row, col);      // goes through the model, fires TblHandler
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
	/*
	 * This code is to make buttons that "updates" 
	 * a cell to re-run its code. I do not know
	 * if im going to use this outside of debug.
	 */
	
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
			//button debug
			TblDataModel.fireTableCellUpdated(row, col);
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
	    /*
	    TblDataModel.setValueAt("6", 0, 0);
	    TblDataModel.setValueAt("7", 0, 1);
	    TblDataModel.setValueAt(":SUM(:SUM(A1,B1),2)", 1, 0);
	    */
	    GetCellRef("A1");
	    // ONLY FOR TESTING!!! end
	    Tbl = new JTable(TblDataModel);
	    //Tbl.setDefaultEditor(Object.class, new EditDialog());
	    Tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    TblScroll = new JScrollPane(Tbl);
	    Tbl.getTableHeader().setReorderingAllowed(false);
	    Tbl.getTableHeader().addMouseListener(new ColListener());
	    Tbl.addMouseListener(new TblListener());
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
	}
}
