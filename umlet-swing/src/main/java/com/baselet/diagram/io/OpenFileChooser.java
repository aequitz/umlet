package com.baselet.diagram.io;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.baselet.control.config.Config;
import com.baselet.control.constants.SystemInfo;
import com.baselet.control.enums.Os;
import com.baselet.control.enums.Program;
import com.baselet.util.logging.Logger;
import com.baselet.util.logging.LoggerFactory;

/**
 * Lets the user choose the diagram files to open using the system file dialog:
 * <ul>
 * <li>Windows: the modern Windows Explorer dialog (see {@link ExplorerFileDialog}). If it is not available, the older native dialog of the AWT is used.</li>
 * <li>macOS: the native dialog of the AWT.</li>
 * <li>Other systems: the Swing file chooser.</li>
 * </ul>
 */
public class OpenFileChooser {

	private static final Logger log = LoggerFactory.getLogger(OpenFileChooser.class);

	private static String extensionSuffix() {
		return "." + Program.getInstance().getExtension();
	}

	private static String dialogTitle() {
		return "Open " + Program.getInstance().getProgramName() + " diagram";
	}

	public List<String> getFilesToOpen(Frame mainFrame) {
		if (SystemInfo.OS == Os.WINDOWS) {
			String filter = Program.getInstance().getProgramName() + " diagram format (*" + extensionSuffix() + ")|*" + extensionSuffix() + "|All files (*.*)|*.*";
			ExplorerFileDialog.Result result = ExplorerFileDialog.showOpenDialog(dialogTitle(), filter, Config.getInstance().getOpenFileHome());
			if (result.getOutcome() == ExplorerFileDialog.Outcome.OK) {
				List<String> existingFiles = new ArrayList<String>();
				for (String path : result.getFiles()) {
					if (new File(path).isFile()) {
						existingFiles.add(path);
					}
				}
				return existingFiles;
			}
			if (result.getOutcome() == ExplorerFileDialog.Outcome.CANCELLED) {
				return new ArrayList<String>();
			}
			log.info("The Windows Explorer dialog is not available, using the AWT file dialog instead");
		}
		if (SystemInfo.OS == Os.WINDOWS || SystemInfo.OS == Os.MAC) {
			return getFilesToOpenAwt(mainFrame);
		}
		return getFilesToOpenSwing(mainFrame); // the AWT dialog does not look good on other systems
	}

	private List<String> getFilesToOpenSwing(Frame mainFrame) {
		JFileChooser fileChooser = new JFileChooser(Config.getInstance().getOpenFileHome());
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(extensionSuffix()) || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return Program.getInstance().getProgramName() + " diagram format (*" + extensionSuffix() + ")";
			}
		});
		fileChooser.setAcceptAllFileFilterUsed(false);

		List<String> fileNames = new ArrayList<String>();
		if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
			for (File file : fileChooser.getSelectedFiles()) {
				fileNames.add(file.getAbsolutePath());
			}
		}
		return fileNames;
	}

	private List<String> getFilesToOpenAwt(Frame mainFrame) {
		List<String> fileNames = new ArrayList<String>();
		FileDialog dialog = new FileDialog(mainFrame, dialogTitle(), FileDialog.LOAD);
		dialog.setMultipleMode(true);
		String openFileHome = Config.getInstance().getOpenFileHome();
		if (openFileHome != null && new File(openFileHome).isDirectory()) {
			dialog.setDirectory(openFileHome);
		}
		dialog.setFile("*" + extensionSuffix()); // on Windows a wildcard in the file name works as filter for the shown files
		dialog.setFilenameFilter(new FilenameFilter() { // on macOS this filter is used
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(extensionSuffix()) || new File(dir, name).isDirectory();
			}
		});
		dialog.setVisible(true); // blocks until the dialog is closed
		File[] selectedFiles = dialog.getFiles();
		if (selectedFiles != null) {
			for (File file : selectedFiles) {
				fileNames.add(file.getAbsolutePath());
			}
		}
		return fileNames;
	}

}
