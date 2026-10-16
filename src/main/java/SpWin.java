//package spreadsheet_program;
//Spreadsheet's Window
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
public class SpWin extends JFrame implements Runnable {
	//Main
	public int SpWinWidth;
	public int SpWinHeight;
	public SpKeys SpKeys = new SpKeys(this);
	public SpMenu SpMenu;
	public SpTables SpTables;
	
	//Thread
	private Thread SpWinThread;
	SpWin(int ww,int wh){
		setTitle("SpreadSheet Program on Java");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpWinWidth = ww;
		SpWinHeight= wh;
		this.setPreferredSize(new Dimension(ww,wh));
	    this.setLayout(new BorderLayout());   // lets the table fill the panel
		//this.setDoubleBuffered(true);
		this.addKeyListener(SpKeys);
		//this.addMouseListener(MouseHandler);
		this.setFocusable(true);
		SpTables = new SpTables();
		SpMenu = new SpMenu(SpTables);
		add(SpTables.TblScroll);
		setJMenuBar(SpMenu.Bar);
		pack();
		setVisible(true);
		this.SpWinThread = new Thread(this);
		SpWinThread.run();
	}
	@Override
	public void run() {
		repaint();
	}
	/*
	@Override 
	protected void paintComponent(Graphics GMod){
		super.paintComponent(GMod);
	}
	*/
}
