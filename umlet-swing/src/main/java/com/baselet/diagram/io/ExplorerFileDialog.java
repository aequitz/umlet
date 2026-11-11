package com.baselet.diagram.io;

import java.awt.EventQueue;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.baselet.control.constants.SystemInfo;
import com.baselet.control.enums.Os;
import com.baselet.util.logging.Logger;
import com.baselet.util.logging.LoggerFactory;

/**
 * Shows the modern Windows Explorer style open/save dialog.
 * Plain Java cannot show this dialog (the AWT FileDialog shows the legacy one), therefore a short PowerShell helper process shows the standard Windows Forms dialog and returns the choice.
 * If this is not possible (other OS, PowerShell missing or blocked by a policy) the result has the outcome {@link Outcome#UNAVAILABLE} and the caller has to use another dialog.
 */
public final class ExplorerFileDialog {

	private static final Logger log = LoggerFactory.getLogger(ExplorerFileDialog.class);

	public enum Outcome {
		/** the dialog could not be shown, use a different dialog */
		UNAVAILABLE,
		/** the user closed the dialog without choosing a file */
		CANCELLED,
		/** the user chose at least one file */
		OK
	}

	public static final class Result {
		private final Outcome outcome;
		private final List<String> files;
		private final int filterIndex;

		private Result(Outcome outcome, List<String> files, int filterIndex) {
			this.outcome = outcome;
			this.files = files;
			this.filterIndex = filterIndex;
		}

		public Outcome getOutcome() {
			return outcome;
		}

		/** the chosen files (empty if the outcome is not OK) */
		public List<String> getFiles() {
			return files;
		}

		/** zero based index of the file type which was selected in the dialog */
		public int getFilterIndex() {
			return filterIndex;
		}
	}

	private static final String RESULT_OK = "UMLET_RESULT_OK";
	private static final String RESULT_CANCEL = "UMLET_RESULT_CANCEL";
	private static final String FILTER_PREFIX = "UMLET_FILTER_INDEX=";

	/**
	 * The script reads its input from environment variables (no quoting problems) and prints the result in UTF-8:
	 * RESULT_CANCEL, or RESULT_OK followed by the selected filter index and one path per line.
	 * The invisible top most owner form makes sure that the dialog is shown in front of the application.
	 */
	private static final String POWERSHELL_SCRIPT = "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8;"
			+ "Add-Type -AssemblyName System.Windows.Forms;"
			+ "[System.Windows.Forms.Application]::EnableVisualStyles();"
			+ "$owner = New-Object System.Windows.Forms.Form;"
			+ "$owner.TopMost = $true; $owner.ShowInTaskbar = $false; $owner.FormBorderStyle = 'None'; $owner.Opacity = 0; $owner.Size = New-Object System.Drawing.Size(1,1); $owner.StartPosition = 'CenterScreen';"
			+ "$owner.Show();"
			+ "if ($env:UMLET_DIALOG_MODE -eq 'save') {"
			+ "  $dialog = New-Object System.Windows.Forms.SaveFileDialog; $dialog.OverwritePrompt = $true; $dialog.AddExtension = $true;"
			+ "  if ($env:UMLET_DIALOG_FILENAME) { $dialog.FileName = $env:UMLET_DIALOG_FILENAME }"
			+ "} else {"
			+ "  $dialog = New-Object System.Windows.Forms.OpenFileDialog; $dialog.Multiselect = $true;"
			+ "};"
			+ "$dialog.Title = $env:UMLET_DIALOG_TITLE; $dialog.Filter = $env:UMLET_DIALOG_FILTER; $dialog.FilterIndex = [int]$env:UMLET_DIALOG_FILTER_INDEX;"
			+ "if ($env:UMLET_DIALOG_DIR -and (Test-Path -LiteralPath $env:UMLET_DIALOG_DIR)) { $dialog.InitialDirectory = $env:UMLET_DIALOG_DIR };"
			+ "$result = $dialog.ShowDialog($owner);"
			+ "$owner.Close();"
			+ "if ($result -eq [System.Windows.Forms.DialogResult]::OK) { '" + RESULT_OK + "'; '" + FILTER_PREFIX + "' + $dialog.FilterIndex; $dialog.FileNames } else { '" + RESULT_CANCEL + "' }";

	private ExplorerFileDialog() {}

	/**
	 * @param filter file types in the format of the Windows Forms dialogs: "description|pattern|description|pattern" (e.g. "UMLet diagram (*.uxf)|*.uxf|All files (*.*)|*.*")
	 */
	public static Result showOpenDialog(String title, String filter, String initialDirectory) {
		return show("open", title, filter, 0, initialDirectory, null);
	}

	/**
	 * @param selectedFilterIndex zero based index of the file type which is preselected
	 * @param fileName the preset name (without extension, the dialog adds the extension of the selected file type)
	 */
	public static Result showSaveDialog(String title, String filter, int selectedFilterIndex, String initialDirectory, String fileName) {
		return show("save", title, filter, selectedFilterIndex, initialDirectory, fileName);
	}

	private static Result show(final String mode, final String title, final String filter, final int selectedFilterIndex, final String initialDirectory, final String fileName) {
		if (SystemInfo.OS != Os.WINDOWS) {
			return unavailable();
		}
		final List<Result> resultHolder = new ArrayList<Result>(); // stays empty if the dialog could not be shown
		Runnable showDialog = new Runnable() {
			@Override
			public void run() {
				try {
					Result result = runPowerShellDialog(mode, title, filter, selectedFilterIndex, initialDirectory, fileName);
					synchronized (resultHolder) {
						resultHolder.add(result);
					}
				} catch (Exception e) {
					log.error("Cannot show the Windows Explorer dialog", e);
				}
			}
		};
		runAndKeepGuiResponsive(showDialog);

		synchronized (resultHolder) {
			if (resultHolder.isEmpty()) {
				return unavailable();
			}
			return resultHolder.get(0);
		}
	}

	private static Result unavailable() {
		return new Result(Outcome.UNAVAILABLE, new ArrayList<String>(), 0);
	}

	/**
	 * Runs the (blocking) task in a separate thread. If this method is called on the event dispatch thread, the events are still processed while waiting (e.g. the application window is repainted).
	 */
	private static void runAndKeepGuiResponsive(final Runnable task) {
		if (SwingUtilities.isEventDispatchThread()) {
			EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
			final SecondaryLoop loop = eventQueue.createSecondaryLoop();
			new Thread("File dialog") {
				@Override
				public void run() {
					try {
						task.run();
					} finally {
						loop.exit();
					}
				}
			}.start();
			loop.enter();
		}
		else {
			task.run();
		}
	}

	private static Result runPowerShellDialog(String mode, String title, String filter, int selectedFilterIndex, String initialDirectory, String fileName) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-STA", "-Command", POWERSHELL_SCRIPT);
		pb.environment().put("UMLET_DIALOG_MODE", mode);
		pb.environment().put("UMLET_DIALOG_TITLE", title);
		pb.environment().put("UMLET_DIALOG_FILTER", filter);
		pb.environment().put("UMLET_DIALOG_FILTER_INDEX", Integer.toString(selectedFilterIndex + 1)); // Windows Forms counts from 1
		pb.environment().put("UMLET_DIALOG_DIR", initialDirectory == null ? "" : initialDirectory);
		pb.environment().put("UMLET_DIALOG_FILENAME", fileName == null ? "" : fileName);
		pb.redirectError(ProcessBuilder.Redirect.to(new File("NUL"))); // errors are ignored, a missing result line is handled as failure
		Process process = pb.start();
		process.getOutputStream().close();

		List<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.replace("﻿", "").trim();
				if (!line.isEmpty()) {
					lines.add(line);
				}
			}
		} finally {
			reader.close();
		}
		process.waitFor();

		if (lines.isEmpty()) {
			return unavailable();
		}
		if (lines.get(0).equals(RESULT_CANCEL)) {
			return new Result(Outcome.CANCELLED, new ArrayList<String>(), 0);
		}
		if (lines.get(0).equals(RESULT_OK) && lines.size() > 2 && lines.get(1).startsWith(FILTER_PREFIX)) {
			int filterIndex = Integer.parseInt(lines.get(1).substring(FILTER_PREFIX.length())) - 1;
			List<String> files = new ArrayList<String>(lines.subList(2, lines.size()));
			return new Result(Outcome.OK, files, Math.max(filterIndex, 0));
		}
		return unavailable(); // unexpected output: PowerShell is restricted or the script failed
	}
}
