//package spreadsheet_program;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

public class MainClass {
	public static JFrame MainFrame;
	public static SpWin SpWin;
	public static final int WINDOW_W = 1280;
	public static final int WINDOW_H = 720;
	//public static void MainLoop() {
	//	MainFrame.repaint();
	//}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				/*
				MainFrame = new JFrame("Spreadsheet program");
				MainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				MainFrame.setResizable(false);
				MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				SpWin = new SpWin(WINDOW_W, WINDOW_H);
				MainFrame.add(SpWin);
				MainFrame.pack();
				MainFrame.setLocationRelativeTo(null);
				MainFrame.setVisible(true);
				*/
				SpWin = new SpWin(WINDOW_W, WINDOW_H);
			}
		});
	}

}

