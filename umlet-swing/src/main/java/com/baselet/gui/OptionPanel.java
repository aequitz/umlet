package com.baselet.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import javax.swing.UIManager.LookAndFeelInfo;

import com.baselet.control.Main;
import com.baselet.control.config.Config;
import com.baselet.control.AutoSaver;
import com.baselet.control.config.PaletteInsertPosition;
import com.baselet.control.config.SharedConfig;
import com.baselet.control.constants.Constants;
import com.baselet.control.enums.Program;
import com.baselet.control.enums.RuntimeType;
import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.draw.helper.theme.ThemeFactory;
import com.baselet.gui.menu.MenuFactorySwing;
import com.baselet.diagram.draw.helper.theme.ThemeFactory.THEMES;
import com.baselet.util.logging.Logger;
import com.baselet.util.logging.LoggerFactory;

@SuppressWarnings("serial")
public class OptionPanel extends JPanel implements ActionListener {

	private final Logger log = LoggerFactory.getLogger(OptionPanel.class);
	private static OptionPanel optionpanel;

	public static OptionPanel getInstance() {
		if (optionpanel == null) {
			optionpanel = new OptionPanel();
		}
		return optionpanel;
	}

	private final JFrame optionframe;
	private final JCheckBox show_stickingpolygon = new JCheckBox();
	private final JCheckBox show_grid = new JCheckBox();
	private final JCheckBox openPreviousFiles = new JCheckBox();
	private final JCheckBox selectWithoutCtrl = new JCheckBox();
	private final JCheckBox multiSelectWithShift = new JCheckBox();
	private final JCheckBox typeIntoProperties = new JCheckBox();
	private final JCheckBox autocompleteWhileTyping = new JCheckBox();
	private final JCheckBox autosave = new JCheckBox();
	private final JSpinner autosaveSeconds = new JSpinner(new SpinnerNumberModel(300, 1, 86400, 5)); // the number can be typed in or changed with the arrows
	private final JComboBox<PaletteInsertPosition> paletteInsertPosition = new JComboBox<PaletteInsertPosition>(PaletteInsertPosition.values());
	private final JCheckBox autofitShapes = new JCheckBox();
	private final JCheckBox enable_custom_elements = new JCheckBox();
	private final JCheckBox checkForUpdates = new JCheckBox();
	private final JCheckBox developerMode = new JCheckBox();
	private final JCheckBox propertiesPanelLineWrap = new JCheckBox();
	private final JCheckBox propertiesPanelLineNumbers = new JCheckBox();
	private final JTextField pdfFont = new HintTextField("Path to font e.g.; c:/windows/fonts/msgothic.ttc,1");
	private final JTextField pdfFontBold = new HintTextField("same as above but used for bold text");
	private final JTextField pdfFontItalic = new HintTextField("same as above but used for italic text");
	private final JTextField pdfFontBoldItalic = new HintTextField("same as above but used for bold+italic text");
	private final JComboBox ui_manager;
	private final JComboBox default_fontsize = new JComboBox(new Integer[] { 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 });
	private final JComboBox propertiesPanelFontsize = new JComboBox(new Integer[] { 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 });
	private final JComboBox<String> uiScale = new JComboBox<>(new String[] { "Default", "1", "1.5", "2", "2.5", "3", "3.5", "4" });
	private final JComboBox exportScale = new JComboBox(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8 });
	private final JComboBox default_fontfamily = new JComboBox(Constants.fontFamilyList.toArray(new String[Constants.fontFamilyList.size()]));

	private final Vector<String> uis_technicalNames = new Vector<String>();

	private OptionPanel() {
		setLayout(new GridLayout(0, 2, 4, 4));
		setAlignmentX(Component.LEFT_ALIGNMENT);

		Vector<String> uis_humanReadableNameVector = new Vector<String>();
		LookAndFeelInfo[] lookAndFeelInfoArray = Constants.lookAndFeels.toArray(new LookAndFeelInfo[Constants.lookAndFeels.size()]);
		for (LookAndFeelInfo info : lookAndFeelInfoArray) {
			uis_technicalNames.add(info.getClassName());
			uis_humanReadableNameVector.add(info.getName());
		}
		ui_manager = new JComboBox(uis_humanReadableNameVector);

		this.add(new JLabel("Show sticking polygon"));
		this.add(show_stickingpolygon);
		this.add(new JLabel("Show grid"));
		this.add(show_grid);
		JLabel autofitLabel = new JLabel("Autofit shapes to their text by default");
		String autofitToolTip = "Shapes (class, state, use case, ...) resize to fit their text. Overwrite per element with style=manual, style=wordwrap or style=noresize";
		autofitLabel.setToolTipText(autofitToolTip);
		autofitShapes.setToolTipText(autofitToolTip);
		this.add(autofitLabel);
		this.add(autofitShapes);
		JLabel openPreviousLabel = new JLabel("Open previously opened files on startup");
		String openPreviousToolTip = "On startup, reopen all saved diagrams which were open when " + Program.getInstance().getProgramName() + " was closed";
		openPreviousLabel.setToolTipText(openPreviousToolTip);
		openPreviousFiles.setToolTipText(openPreviousToolTip);
		this.add(openPreviousLabel);
		this.add(openPreviousFiles);
		JLabel paletteInsertLabel = new JLabel("Insert position for elements added from a palette");
		String paletteInsertToolTip = "Where in the visible area of the diagram an element is placed if you double-click it in a palette";
		paletteInsertLabel.setToolTipText(paletteInsertToolTip);
		paletteInsertPosition.setToolTipText(paletteInsertToolTip);
		this.add(paletteInsertLabel);
		this.add(paletteInsertPosition);
		JLabel selectWithoutCtrlLabel = new JLabel("Select without Ctrl (drag = lasso)");
		String selectWithoutCtrlToolTip = "On: drag the background to select with a lasso, Ctrl+click adds elements to the selection, Ctrl+drag on the background moves the whole diagram. "
				+ "Off (classic): Ctrl+click and Ctrl+drag select, dragging the background moves the whole diagram";
		selectWithoutCtrlLabel.setToolTipText(selectWithoutCtrlToolTip);
		selectWithoutCtrl.setToolTipText(selectWithoutCtrlToolTip);
		this.add(selectWithoutCtrlLabel);
		this.add(selectWithoutCtrl);
		JLabel multiSelectShiftLabel = new JLabel("Select multiple elements with Shift (instead of Ctrl)");
		String multiSelectShiftToolTip = "On: Shift+click adds/removes elements to/from the selection (and Shift+drag on the background draws a lasso in classic mode). Off: Ctrl is used (Cmd on macOS)";
		multiSelectShiftLabel.setToolTipText(multiSelectShiftToolTip);
		multiSelectWithShift.setToolTipText(multiSelectShiftToolTip);
		this.add(multiSelectShiftLabel);
		this.add(multiSelectWithShift);
		JLabel typeIntoPropertiesLabel =new JLabel("Typing edits the selected element");
		String typeIntoPropertiesToolTip = "If exactly one element is selected, typing on the diagram writes into the properties panel of this element (then the + and - keys no longer zoom, use the mouse wheel with Ctrl instead)";
		typeIntoPropertiesLabel.setToolTipText(typeIntoPropertiesToolTip);
		typeIntoProperties.setToolTipText(typeIntoPropertiesToolTip);
		this.add(typeIntoPropertiesLabel);
		this.add(typeIntoProperties);
		JLabel autocompleteWhileTypingLabel = new JLabel("Show suggestions while typing");
		String autocompleteWhileTypingToolTip = "Opens the autocomplete suggestions (e.g. the possible values after lt= or style=) automatically while typing in the properties panel. If switched off, they are only shown with Ctrl+Space";
		autocompleteWhileTypingLabel.setToolTipText(autocompleteWhileTypingToolTip);
		autocompleteWhileTyping.setToolTipText(autocompleteWhileTypingToolTip);
		this.add(autocompleteWhileTypingLabel);
		this.add(autocompleteWhileTyping);
		JLabel autosaveLabel = new JLabel("Autosave diagrams");
		String autosaveToolTip = "Regularly saves all changed diagrams which are already stored in a file (diagrams without a file are never autosaved)";
		autosaveLabel.setToolTipText(autosaveToolTip);
		autosave.setToolTipText(autosaveToolTip);
		this.add(autosaveLabel);
		this.add(autosave);
		this.add(new JLabel("Autosave interval (seconds)"));
		this.add(autosaveSeconds);
		this.add(new JLabel("Enable Custom Elements"));
		this.add(enable_custom_elements);
		this.add(new JLabel("Check for " + Program.getInstance().getProgramName() + " updates"));
		this.add(checkForUpdates);
		if (Program.getInstance().getRuntimeType() == RuntimeType.STANDALONE) {
			this.add(new JLabel(Program.getInstance().getProgramName() + " style"));
			this.add(ui_manager);
		}
		this.add(new JLabel("Default fontsize"));
		this.add(default_fontsize);
		this.add(new JLabel("Properties panel fontsize (requires restart)"));
		this.add(propertiesPanelFontsize);
		this.add(new JLabel("Properties panel line wrap (requires restart)"));
		this.add(propertiesPanelLineWrap);
		this.add(new JLabel("Properties panel line numbers (requires restart)"));
		this.add(propertiesPanelLineNumbers);
		JLabel uiScaleLabel = new JLabel("UI Scale for hi res monitors (requires restart)");
		String uiScaleToolTip = "Overwrite the default scaling which is determined by your OS (some OS only accept integers)";
		uiScaleLabel.setToolTipText(uiScaleToolTip);
		uiScale.setToolTipText(uiScaleToolTip);
		this.add(uiScaleLabel);
		this.add(uiScale);
		this.add(new JLabel("Export Scale for image export"));
		this.add(exportScale);
		this.add(new JLabel("Default fontfamily"));
		this.add(default_fontfamily);
		this.add(new JLabel("Developer Mode (show extended Element Info)"));
		this.add(developerMode);
		this.add(new JLabel("Optional font to embedd in PDF - normal text"));
		this.add(pdfFont);
		this.add(new JLabel("Optional font to embedd in PDF - bold text"));
		this.add(pdfFontBold);
		this.add(new JLabel("Optional font to embedd in PDF - italic text"));
		this.add(pdfFontItalic);
		this.add(new JLabel("Optional font to embedd in PDF - bold+italic"));
		this.add(pdfFontBoldItalic);

		JButton button_ok = new JButton("Ok");
		button_ok.setActionCommand("Ok");
		button_ok.addActionListener(this);
		JButton button_cancel = new JButton("Cancel");
		button_cancel.setActionCommand("Cancel");
		button_cancel.addActionListener(this);

		JPanel button_panel = new JPanel();
		button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));
		button_panel.add(Box.createHorizontalGlue());
		button_panel.add(button_cancel);
		button_panel.add(Box.createRigidArea(new Dimension(20, 0)));
		button_panel.add(button_ok);
		button_panel.add(Box.createHorizontalGlue());
		button_panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel parent = new JPanel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		parent.add(Box.createRigidArea(new Dimension(10, 10)));
		parent.add(this);
		parent.add(Box.createRigidArea(new Dimension(0, 20)));
		parent.add(button_panel);
		parent.add(Box.createRigidArea(new Dimension(0, 20)));

		optionframe = new JFrame(Program.getInstance().getProgramName() + " Options");
		optionframe.setContentPane(parent);
		optionframe.pack(); // autoresize of the optionframe
	}

	public void showOptionPanel() {
		show_stickingpolygon.setSelected(SharedConfig.getInstance().isShow_stickingpolygon());
		show_grid.setSelected(Config.getInstance().isShow_grid());
		openPreviousFiles.setSelected(Config.getInstance().isOpenPreviousFiles());
		selectWithoutCtrl.setSelected(Config.getInstance().isSelectWithoutCtrl());
		multiSelectWithShift.setSelected(Config.getInstance().isMultiSelectWithShift());
		typeIntoProperties.setSelected(Config.getInstance().isTypeIntoProperties());
		autocompleteWhileTyping.setSelected(Config.getInstance().isAutocompleteWhileTyping());
		autosave.setSelected(Config.getInstance().isAutosave());
		autosaveSeconds.setValue(Config.getInstance().getAutosaveSeconds());
		paletteInsertPosition.setSelectedItem(Config.getInstance().getPaletteInsertPosition());
		autofitShapes.setSelected(SharedConfig.getInstance().isAutofitShapes());
		enable_custom_elements.setSelected(Config.getInstance().isEnable_custom_elements());
		propertiesPanelLineWrap.setSelected(Config.getInstance().isPropertiesPanelLineWrap());
		propertiesPanelLineNumbers.setSelected(Config.getInstance().isPropertiesPanelLineNumbers());
		checkForUpdates.setSelected(Config.getInstance().isCheckForUpdates());
		developerMode.setSelected(SharedConfig.getInstance().isDev_mode());
		ui_manager.setSelectedIndex(uis_technicalNames.indexOf(Config.getInstance().getUiManager()));
		default_fontsize.setSelectedItem(Config.getInstance().getDefaultFontsize());
		propertiesPanelFontsize.setSelectedItem(Config.getInstance().getPropertiesPanelFontsize());
		uiScale.setSelectedItem(Config.getInstance().getUiScale());
		exportScale.setSelectedItem(Config.getInstance().getExportScale());
		default_fontfamily.setSelectedItem(Config.getInstance().getDefaultFontFamily());
		pdfFont.setText(Config.getInstance().getPdfExportFont());
		pdfFontBold.setText(Config.getInstance().getPdfExportFontBold());
		pdfFontItalic.setText(Config.getInstance().getPdfExportFontItalic());
		pdfFontBoldItalic.setText(Config.getInstance().getPdfExportFontBoldItalic());
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				optionframe.setLocationRelativeTo(CurrentGui.getInstance().getGui().getMainFrame());
				optionframe.setVisible(true);
				optionframe.toFront();
			}
		});
	}

	private void hideOptionPanel() {
		optionframe.setVisible(false);
	}

	// ok or cancel button pressed
	@Override
	public void actionPerformed(ActionEvent ae) {
		hideOptionPanel();

		if (ae.getActionCommand().equals("Ok")) {
			SharedConfig.getInstance().setShow_stickingpolygon(show_stickingpolygon.isSelected());
			Config.getInstance().setShow_grid(show_grid.isSelected());
			Config.getInstance().setOpenPreviousFiles(openPreviousFiles.isSelected());
			Config.getInstance().setSelectWithoutCtrl(selectWithoutCtrl.isSelected());
			Config.getInstance().setMultiSelectWithShift(multiSelectWithShift.isSelected());
			Config.getInstance().setTypeIntoProperties(typeIntoProperties.isSelected());
			Config.getInstance().setAutocompleteWhileTyping(autocompleteWhileTyping.isSelected());
			CurrentGui.getInstance().getGui().getPropertyPane().updateAutocompleteWhileTyping();
			Config.getInstance().setAutosave(autosave.isSelected());
			try {
				autosaveSeconds.commitEdit(); // takes over a number which has been typed in but not confirmed with Enter yet
			} catch (java.text.ParseException e) {
				// invalid text: the spinner keeps the last valid value
			}
			Config.getInstance().setAutosaveSeconds(((Number) autosaveSeconds.getValue()).intValue());
			AutoSaver.getInstance().restart();
			MenuFactorySwing.getInstance().updateAutosaveMenuItem();
			Config.getInstance().setPaletteInsertPosition((PaletteInsertPosition) paletteInsertPosition.getSelectedItem());
			SharedConfig.getInstance().setAutofitShapes(autofitShapes.isSelected());
			Config.getInstance().setEnable_custom_elements(enable_custom_elements.isSelected());
			Config.getInstance().setPropertiesPanelLineWrap(propertiesPanelLineWrap.isSelected());
			Config.getInstance().setPropertiesPanelLineNumbers(propertiesPanelLineNumbers.isSelected());
			Config.getInstance().setCheckForUpdates(checkForUpdates.isSelected());
			SharedConfig.getInstance().setDev_mode(developerMode.isSelected());
			Config.getInstance().setDefaultFontsize((Integer) default_fontsize.getSelectedItem());
			Config.getInstance().setPdfExportFont(pdfFont.getText());
			Config.getInstance().setPdfExportFontBold(pdfFontBold.getText());
			Config.getInstance().setPdfExportFontItalic(pdfFontItalic.getText());
			Config.getInstance().setPdfExportFontBoldItalic(pdfFontBoldItalic.getText());

			String newui = uis_technicalNames.get(ui_manager.getSelectedIndex());
			// only set look and feel if it has changed, because it messes up frame-size
			if (newui != null && !newui.equals(Config.getInstance().getUiManager())) {
				ThemeFactory.changeTheme(newui.equals(Constants.FLAT_DARCULA_THEME) ? THEMES.DARK : THEMES.LIGHT, null, true);
				Config.getInstance().setUiManager(newui);
				CurrentGui.getInstance().getGui().setLookAndFeel(newui, optionframe);
			}

			// redraw every element to apply changes (like show stickingpolygon, fontsize, ...)
			for (DiagramHandler d : Main.getInstance().getDiagramsAndPalettes()) {
				d.getFontHandler().resetFontSize();
				d.getDrawPanel().updateElements();
				if (ThemeFactory.getActiveThemeEnum() == THEMES.DARK) {
					d.getDrawPanel().setBackground(Constants.DARK_BACKGROUND_COLOR);
				}
				else {
					d.getDrawPanel().setBackground(Color.WHITE);
				}
				d.getDrawPanel().repaint();
			}
			Config.getInstance().setPropertiesPanelFontsize((Integer) propertiesPanelFontsize.getSelectedItem());
			Config.getInstance().setUiScale((String) uiScale.getSelectedItem());
			Config.getInstance().setExportScale((Integer) exportScale.getSelectedItem());

			String newfamily = (String) default_fontfamily.getSelectedItem();
			Config.getInstance().setDefaultFontFamily(newfamily);
		}
	}
}
