package com.baselet.gui.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.baselet.control.Main;
import com.baselet.control.basics.geom.Point;
import com.baselet.control.config.Config;
import com.baselet.control.config.SharedConfig;
import com.baselet.control.enums.Direction;
import com.baselet.control.enums.Program;
import com.baselet.control.enums.RuntimeType;
import com.baselet.diagram.CurrentDiagram;
import com.baselet.diagram.CustomPreviewHandler;
import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.PaletteHandler;
import com.baselet.element.interfaces.GridElement;
import com.baselet.gui.CurrentGui;
import com.baselet.gui.command.Command;
import com.baselet.gui.command.Macro;

public class GUIListener implements KeyListener {

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			SharedConfig.getInstance().setStickingEnabled(false);
		}

		DiagramHandler handler = CurrentDiagram.getInstance().getDiagramHandler();
		boolean typingGoesToPropertyPane = isTypingForwardedToPropertyPane(handler); // then + and - are normal characters and no zoom keys

		if (handler != null && !e.isAltDown() && !e.isAltGraphDown() /* && !e.isControlDown() && !e.isMetaDown() */) {

			/**
			 * Enter: jumps directly into the diagram
			 */
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				CurrentGui.getInstance().getGui().focusPropertyPane();
				if (typingGoesToPropertyPane) { // continue at the end of the text, like if a character had been typed
					JTextComponent propertyText = CurrentGui.getInstance().getGui().getPropertyPane().getTextComponent();
					propertyText.setCaretPosition(propertyText.getDocument().getLength());
				}
			}

			/**
			 * Ctrl +/-: Zoom diagram by 10%
			 */
			// KeyChar check doesn't check non-numpad + on some keyboards, therefore we also need KeyEvent.VK_PLUS
			else if (!typingGoesToPropertyPane && (e.getKeyChar() == '+' || e.getKeyCode() == KeyEvent.VK_PLUS)) {
				int actualZoom = handler.getGridSize();
				handler.setGridAndZoom(actualZoom + 1);
			}
			// KeyChar check doesn't check non-numpad - on some keyboards, therefore we also need KeyEvent.VK_MINUS
			else if (!typingGoesToPropertyPane && (e.getKeyChar() == '-' || e.getKeyCode() == KeyEvent.VK_MINUS)) {
				int actualZoom = handler.getGridSize();
				handler.setGridAndZoom(actualZoom - 1);
			}
			/**
			 * Cursors: Move diagram by a small distance
			 */
			else {
				int diffx = 0;
				int diffy = 0;

				if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN) {
					diffy = handler.getGridSize();
				}
				if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP) {
					diffy = -handler.getGridSize();
				}
				if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT) {
					diffx = -handler.getGridSize();
				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT) {
					diffx = handler.getGridSize();
				}

				if (diffx != 0 || diffy != 0) {
					// Move only selected entities or all if no entity is selected
					Collection<GridElement> entitiesToBeMoved = handler.getDrawPanel().getSelector().getSelectedElements();
					if (entitiesToBeMoved.isEmpty()) {
						entitiesToBeMoved = handler.getDrawPanel().getGridElements();
					}

					// iterator().next() will throw NoSuchElementException on
					// an empty collection
					if (entitiesToBeMoved.isEmpty()) {
						return;
					}

					Point opos = getOriginalPos(diffx, diffy, entitiesToBeMoved.iterator().next());
					Vector<Command> ALL_MOVE_COMMANDS = GridElementListener.calculateFirstMoveCommands(diffx, diffy, opos, entitiesToBeMoved, e.isShiftDown(), true, handler, Collections.<Direction> emptySet());
					handler.getController().executeCommand(new Macro(ALL_MOVE_COMMANDS));
					CurrentDiagram.getInstance().getDiagramHandler().getDrawPanel().updatePanelAndScrollbars();
				}
			}
		}

	}

	private Point getOriginalPos(int diffx, int diffy, GridElement ge) {
		return new Point(ge.getRectangle().x - diffx, ge.getRectangle().y - diffy);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			SharedConfig.getInstance().setStickingEnabled(true);
		}
	}

	/**
	 * true if typing on the diagram should write into the properties panel: exactly one element of a diagram (not of a palette) is selected
	 */
	private static boolean isTypingForwardedToPropertyPane(DiagramHandler handler) {
		return Config.getInstance().isTypeIntoProperties()
				&& Program.getInstance().getRuntimeType() == RuntimeType.STANDALONE
				&& handler != null
				&& !(handler instanceof PaletteHandler)
				&& !(handler instanceof CustomPreviewHandler)
				&& handler.getDrawPanel().getSelector().getSelectedElements().size() == 1
				&& Main.getInstance().getEditedGridElement() != null;
	}

	/**
	 * Typing a character while an element is selected continues in the properties panel of this element: the panel gets the focus and the character is appended to the text.
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		char typedChar = e.getKeyChar();
		if (Character.isISOControl(typedChar) || e.isControlDown() && !e.isAltGraphDown() || e.isAltDown() && !e.isAltGraphDown() || e.isMetaDown()) {
			return; // control characters (backspace, escape, ...) and shortcuts are not text
		}
		if (!isTypingForwardedToPropertyPane(CurrentDiagram.getInstance().getDiagramHandler())) {
			return;
		}
		JTextComponent propertyText = CurrentGui.getInstance().getGui().getPropertyPane().getTextComponent();
		propertyText.requestFocusInWindow();
		try {
			Document document = propertyText.getDocument();
			document.insertString(document.getLength(), String.valueOf(typedChar), null);
			propertyText.setCaretPosition(document.getLength());
		} catch (BadLocationException ex) {
			// cannot happen because the position is the end of the document
		}
		e.consume();
	}

}
