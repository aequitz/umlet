package com.baselet.control.config;

public class SharedConfig {
	private static final SharedConfig instance = new SharedConfig();

	public static SharedConfig getInstance() {
		return instance;
	}

	private boolean show_stickingpolygon = true;
	private boolean stickingEnabled = true;
	private boolean autofitShapes = true; // if true, shapes (class, state, use case, ...) are resized to fit their text unless the element defines a style
	private boolean dev_mode = false; // TODO should be moved to a shared config class

	private SharedConfig() {}

	public boolean isShow_stickingpolygon() {
		return show_stickingpolygon;
	}

	public void setShow_stickingpolygon(boolean show_stickingpolygon) {
		this.show_stickingpolygon = show_stickingpolygon;
	}

	public boolean isStickingEnabled() {
		return stickingEnabled;
	}

	public void setStickingEnabled(boolean stickingEnabled) {
		this.stickingEnabled = stickingEnabled;
	}

	public boolean isAutofitShapes() {
		return autofitShapes;
	}

	public void setAutofitShapes(boolean autofitShapes) {
		this.autofitShapes = autofitShapes;
	}

	public boolean isDev_mode() {
		return dev_mode;
	}

	public void setDev_mode(boolean dev_mode) {
		this.dev_mode = dev_mode;
	}
}
