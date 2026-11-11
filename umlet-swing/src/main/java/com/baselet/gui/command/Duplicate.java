package com.baselet.gui.command;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.baselet.control.config.SharedConfig;
import com.baselet.control.constants.Constants;
import com.baselet.diagram.DiagramHandler;
import com.baselet.element.ElementFactorySwing;
import com.baselet.element.Selector;
import com.baselet.element.interfaces.GridElement;

/**
 * Duplicates the currently selected elements and places the copies close to the originals (in contrast to {@link Paste} which places the elements at the top left of the visible area).
 * The clipboard is not touched by this command.
 */
public class Duplicate extends Command {

	private final List<GridElement> originals;
	private Vector<GridElement> copies;
	private List<Point> positions; // fixed positions of the copies (so redo doesn't displace them again)

	public Duplicate(List<GridElement> originals) {
		this.originals = originals;
	}

	@Override
	public void execute(DiagramHandler handler) {
		super.execute(handler);

		// The copies are created exactly on top of the originals. Sticking must be disabled meanwhile, otherwise the sticking points of relations connected
		// to an original would also be "connected" to its copy and could be moved/attached to the copy.
		boolean stickingWasEnabled = SharedConfig.getInstance().isStickingEnabled();
		SharedConfig.getInstance().setStickingEnabled(false);
		try {
			int displacement = handler.getGridSize() * Constants.DUPLICATE_DISPLACEMENT_GRIDS;
			if (copies == null) {
				copies = new Vector<GridElement>();
				positions = new ArrayList<Point>();
				for (GridElement original : originals) {
					GridElement copy = ElementFactorySwing.createCopy(original, handler);
					int x = handler.realignToGrid(copy.getRectangle().x + displacement);
					int y = handler.realignToGrid(copy.getRectangle().y + displacement);
					copy.setLocation(x, y); // move away from the original immediately
					copies.add(copy);
					positions.add(new Point(x, y));
				}
				Selector.replaceGroupsWithNewGroups(copies, handler.getDrawPanel().getSelector());
			}
			if (copies.isEmpty()) {
				return;
			}

			for (int i = 0; i < copies.size(); i++) {
				new AddElement(copies.get(i), positions.get(i).x, positions.get(i).y, false).execute(handler);
			}
		} finally {
			SharedConfig.getInstance().setStickingEnabled(stickingWasEnabled);
		}

		handler.getDrawPanel().getSelector().deselectAll();
		for (GridElement copy : copies) {
			handler.getDrawPanel().getSelector().select(copy); // select the copies, so the user can directly move them or duplicate them again
		}
		handler.getDrawPanel().updatePanelAndScrollbars();
	}

	@Override
	public void undo(DiagramHandler handler) {
		super.undo(handler);
		new RemoveElement(copies, false).execute(handler);
		handler.getDrawPanel().updatePanelAndScrollbars();
	}
}
