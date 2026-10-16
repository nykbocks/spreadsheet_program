//package spreadsheet_program;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class SpKeys implements KeyListener{
	public boolean UpPressed,DownPressed,LeftPressed,RightPressed,SpacePressed,EscapePressed;
	private SpWin SpWin;
	SpKeys(SpWin spw){
		SpWin = spw;
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// not used
	}
	@Override
	public void keyPressed(KeyEvent e) {
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
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
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
