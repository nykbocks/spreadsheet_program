//package spreadsheet_program;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class SpTblHandler implements TableModelListener{
	@Override
	public void tableChanged(TableModelEvent e) {
		TableModel tbl_data = (TableModel) e.getSource();
		//TODO: Add a parser for the scripting language.
	}
}

