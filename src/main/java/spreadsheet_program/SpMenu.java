//package spreadsheet_program;
package spreadsheet_program;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

public class SpMenu {
	public static final int MnFILE = 0;
	public static final int MnEDIT = 1;
	public JPanel Panel;
	public GridLayout Layout;
	
	public String[] MenuList = {"File","Edit"};
	public ActionListener Handler; 
	public JMenuBar Bar;
	public JMenu[] Menus;
	public SpTables Tbls;
	private JMenuItem MakeMItem(String title,String command) {
		JMenuItem Item = new JMenuItem(title);
		Item.setActionCommand(command);
		Item.addActionListener(Handler);
		return Item;
	}
	
	SpMenu(SpTables tbl){
		Tbls = tbl;
		Bar = new JMenuBar();
		Bar.setOpaque(false);
		Handler = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(e.getActionCommand()) {
				case "file.exit":
					System.exit(0);
					break;
				case "file.save_as":
					Tbls.saveFileAs("test.sp");
					break;
				case "file.load_from":
					Tbls.loadFrom("test.sp");
				}
			}
		};
		Layout = new GridLayout(1,0);
		Panel = new JPanel(Layout);
		Panel.setLayout(Layout);
		Menus = new JMenu[MenuList.length];
		for(int i = 0;i < MenuList.length;i++) {
			Menus[i] = new JMenu(MenuList[i]);
			Menus[i].setFont(new Font(Font.SANS_SERIF,Font.BOLD,15));
			Bar.add(Menus[i]);
		}
		Menus[MnFILE].add(MakeMItem("Save as","file.save_as"));
		Menus[MnFILE].add(MakeMItem("Load from","file.load_from"));
		Menus[MnFILE].add(MakeMItem("Exit","file.exit"));
	}
}
