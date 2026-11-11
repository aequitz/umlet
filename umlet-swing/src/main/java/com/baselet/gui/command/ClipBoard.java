package com.baselet.gui.command;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import com.baselet.control.config.Config;
import com.baselet.control.constants.Constants;
import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.io.OutputHandler;
import com.baselet.element.ElementFactorySwing;
import com.baselet.element.interfaces.GridElement;
import com.baselet.gui.CurrentGui;

/** Copies and Pastes images to the system clipboard. Requires Java 2, v1.4. */
public class ClipBoard implements Transferable {

	private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	private List<GridElement> entities;

	private static ClipBoard _instance = new ClipBoard();

	public static ClipBoard getInstance() {
		return _instance;
	}

	public void copyAndZoomToDefaultLevel(List<GridElement> entities, DiagramHandler handler) {
		if (entities.isEmpty()) {
			return;
		}
		// clipboard zooms entities to 100% (to make them zoom-independent)
		// therefore we need to set a DiagramHandler with 100% zoom and resize the elements
		this.entities = new ArrayList<GridElement>(entities.size());
		DiagramHandler dhNew = new DiagramHandler(null);
		for (GridElement entitiy : entities) {
			this.entities.add(ElementFactorySwing.createCopy(entitiy, dhNew));
		}
		DiagramHandler.zoomEntities(handler.getGridSize(), Constants.DEFAULTGRIDSIZE, this.entities);
		CurrentGui.getInstance().getGui().enablePasteMenuEntry();

		if (clipboard != null) { // Issue 230: copy after zooming the entities
			clipboard.setContents(this, null);
		}
	}

	/**
	 * Puts the given elements as an image on the system clipboard (using the configured export scale).
	 * In contrast to {@link #copyAndZoomToDefaultLevel(List, DiagramHandler)} the elements are not copied into the internal clipboard, therefore a following paste is not influenced.
	 * @return false if there is nothing to copy
	 */
	public boolean copyAsImage(List<GridElement> entities, DiagramHandler handler) {
		if (entities.isEmpty()) {
			return false;
		}
		// the image must be independent of the current zoom level, therefore render copies which are zoomed to 100%
		List<GridElement> zoomedEntities = new ArrayList<GridElement>(entities.size());
		DiagramHandler dhNew = new DiagramHandler(null);
		for (GridElement entity : entities) {
			zoomedEntities.add(ElementFactorySwing.createCopy(entity, dhNew));
		}
		DiagramHandler.zoomEntities(handler.getGridSize(), Constants.DEFAULTGRIDSIZE, zoomedEntities);
		final Image image = OutputHandler.createImageForGridElements(zoomedEntities, null, Config.getInstance().getExportScale());
		clipboard.setContents(new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.imageFlavor };
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.imageFlavor.equals(flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
				if (!isDataFlavorSupported(flavor)) {
					throw new UnsupportedFlavorException(flavor);
				}
				return image;
			}
		}, null);
		return true;
	}

	public List<GridElement> paste() {
		return entities;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.imageFlavor.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return OutputHandler.createImageForGridElements(entities, null, 1);
	}

}
