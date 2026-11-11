package com.baselet.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import com.baselet.control.config.Config;
import com.baselet.diagram.DiagramHandler;

/**
 * Saves all changed diagrams in regular intervals (if enabled in the config).
 * Autosave works silently: it never shows a dialog, never changes the selection and ignores diagrams which have not been saved to a file yet.
 */
public final class AutoSaver {

	private static final AutoSaver instance = new AutoSaver();

	public static AutoSaver getInstance() {
		return instance;
	}

	private Timer timer;

	private AutoSaver() {}

	/**
	 * (Re)starts the timer according to the current config. Must be called after the config has been changed.
	 */
	public synchronized void restart() {
		if (timer != null) {
			timer.stop();
			timer = null;
		}
		if (Config.getInstance().isAutosave()) {
			int intervalMillis = Config.getInstance().getAutosaveSeconds() * 1000;
			timer = new Timer(intervalMillis, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					autosaveAll();
				}
			});
			timer.setRepeats(true);
			timer.start();
		}
	}

	/**
	 * Saves all changed diagrams which are stored in a file. Must be called on the event dispatch thread.
	 */
	public void autosaveAll() {
		for (DiagramHandler diagram : new ArrayList<DiagramHandler>(Main.getInstance().getDiagrams())) {
			diagram.doAutosave();
		}
	}
}
