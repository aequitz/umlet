package com.baselet.control.config;

/**
 * Defines where in the visible area of the diagram an element is inserted if it is added from a palette via double click.
 */
public enum PaletteInsertPosition {
	TOP_LEFT("Top left"),
	TOP_RIGHT("Top right"),
	CENTER("Center");

	private final String label;

	PaletteInsertPosition(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label; // used as text in the option dialog
	}
}
