//package spreadsheet_program;
package spreadsheet_program;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class SpKeys implements KeyListener{
	public boolean AnyKeyPressed,UpPressed,DownPressed,LeftPressed,RightPressed,SpacePressed,EscapePressed;
	@Override
	public void keyTyped(KeyEvent e) {
		// not used
	}
	@Override
	public void keyPressed(KeyEvent e) {
		AnyKeyPressed = true;
		switch(e.getKeyCode()) {
		case KeyEvent.VK_W:
			UpPressed = true;
			break;
		case KeyEvent.VK_A:
			LeftPressed = true;
			break;
		case KeyEvent.VK_S:
			DownPressed = true;
			break;
		case KeyEvent.VK_D:
			RightPressed = true;
			break;
		case KeyEvent.VK_SPACE:
			SpacePressed = true;
			break;
		case KeyEvent.VK_ESCAPE:
			EscapePressed = true;
			break;
		case KeyEvent.VK_DELETE:
			if(SpTables.Tbl == null) return;
			int r = SpTables.Tbl.getSelectedRow();
			int c = SpTables.Tbl.getSelectedColumn();
			
		}
		if(Character.isAlphabetic(e.getKeyChar())) {
			if(SpTables.Tbl == null) return;
			int r = SpTables.Tbl.getSelectedRow();
			int c = SpTables.Tbl.getSelectedColumn();
			SpTables.OpenEditDialog(r,c);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		AnyKeyPressed = false;
		switch(e.getKeyCode()) {
		case KeyEvent.VK_W:
			UpPressed = false;
			break;
		case KeyEvent.VK_A:
			LeftPressed = false;
			break;
		case KeyEvent.VK_S:
			DownPressed = false;
			break;
		case KeyEvent.VK_D:
			RightPressed = false;
			break;
		case KeyEvent.VK_SPACE:
			SpacePressed = false;
			break;
		case KeyEvent.VK_ESCAPE:
			EscapePressed = false;
			break;
		}
	}

}
