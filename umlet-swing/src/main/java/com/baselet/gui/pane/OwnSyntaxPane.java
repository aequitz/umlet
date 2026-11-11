package com.baselet.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.baselet.control.basics.Converter;
import com.baselet.control.config.Config;
import com.baselet.control.config.DerivedConfig;
import com.baselet.control.config.SharedConfig;
import com.baselet.control.constants.Constants;
import com.baselet.diagram.CurrentDiagram;
import com.baselet.diagram.draw.helper.theme.Theme;
import com.baselet.diagram.draw.helper.theme.ThemeFactory;
import com.baselet.element.interfaces.GridElement;
import com.baselet.gui.AutocompletionText;

public class OwnSyntaxPane {

	private static final String SEPARATOR = "    ";

	private final DefaultCompletionProvider provider = new DefaultCompletionProvider() {
		@Override
		protected boolean isValidChar(char ch) {
			return ch != ' '; // every character except space can be part of an autocompletion
		}
	};

	List<AutocompletionText> words = new ArrayList<AutocompletionText>();

	private AutoCompletion autoCompletion;

	JPanel panel;
	RSyntaxTextArea textArea;
	RTextScrollPane scrollPane;

	public OwnSyntaxPane() {

		panel = new JPanel(new FlowLayout());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		textArea = new RSyntaxTextArea() {
			private static final long serialVersionUID = 7431070002967577129L;

			@Override
			public void undoLastAction() {
				CurrentDiagram.getInstance().getDiagramHandler().getController().undo();
			}

			@Override
			public void redoLastAction() {
				CurrentDiagram.getInstance().getDiagramHandler().getController().redo();
			}
		};
		setLineHighlightColor(Config.getInstance().getUiManager().equals(Constants.FLAT_DARCULA_THEME));
		// Setup highlighting
		createHightLightMap();
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping(OwnTokenMaker.ID, OwnTokenMaker.class.getName());
		textArea.setSyntaxEditingStyle(OwnTokenMaker.ID);

		textArea.getSyntaxScheme().getStyle(TokenTypes.RESERVED_WORD).foreground = Converter.convert(ThemeFactory.getCurrentTheme().getColor(Theme.ColorStyle.SYNTAX_HIGHLIGHTING));

		// Setup autocompletion
		createAutocompletionCompletionProvider();
		provider.setAutoActivationRules(true, "=<>-."); // without rules (default) the auto activation popup never opens
		autoCompletion = new AutoCompletion(provider);
		AutoCompletion ac = autoCompletion;
		// ac.setShowDescWindow(true);
		updateAutocompleteWhileTyping();
		ac.setAutoActivationDelay(200);
		ac.setAutoCompleteSingleChoices(false);
		ac.install(textArea);

		JLabel propertyLabel = new JLabel(" Properties");
		propertyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		propertyLabel.setFont(DerivedConfig.getPanelHeaderFont());
		panel.add(propertyLabel);

		textArea.setLineWrap(Config.getInstance().isPropertiesPanelLineWrap());
		textArea.setWrapStyleWord(Config.getInstance().isPropertiesPanelLineWrap());
		textArea.setAntiAliasingEnabled(true);
		textArea.setFont(DerivedConfig.getPanelContentFont());
		if (Config.getInstance().getUiManager().equals(Constants.FLAT_DARCULA_THEME)) {
			textArea.setBackground(Constants.DARK_BACKGROUND_COLOR);
			textArea.setForeground(Color.lightGray);
		}
		scrollPane = new RTextScrollPane(textArea, Config.getInstance().isPropertiesPanelLineNumbers());
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(scrollPane);

		textArea.getDocument().putProperty(PlainDocument.tabSizeAttribute, 3); // Reduce tab size
	}

	/**
	 * Applies the config option which defines if the suggestions are shown automatically while typing (like umletino/VS Code).
	 * If it is switched off, the suggestions are only shown with Ctrl+Space.
	 */
	public void updateAutocompleteWhileTyping() {
		autoCompletion.setAutoActivationEnabled(Config.getInstance().isAutocompleteWhileTyping());
	}

	public void setLineHighlightColor(boolean darkMode) {
		if (darkMode) {
			textArea.setCurrentLineHighlightColor(new Color(153, 102, 0));
			// the default selection color of the dark theme is a pale color which is hard to distinguish from the light text
			textArea.setSelectionColor(new Color(33, 66, 131));
			textArea.setSelectedTextColor(Color.WHITE);
			textArea.setUseSelectedTextColor(true);
		}
		else {
			textArea.setCurrentLineHighlightColor(new Color(255, 255, 153));
			Color defaultSelection = javax.swing.UIManager.getColor("TextArea.selectionBackground");
			if (defaultSelection != null) {
				textArea.setSelectionColor(defaultSelection);
			}
			textArea.setUseSelectedTextColor(false);
		}
	}

	/**
	 * create one per class
	 * @param strings
	 */
	private void createAutocompletionCompletionProvider() {
		provider.clear();
		for (AutocompletionText word : words) {
			BasicCompletion completion = new BasicCompletion(provider, word.getText(), word.getInfo()) {
				@Override
				public String toString() {
					if (getShortDescription() == null) {
						return getInputText();
					}
					return getInputText() + SEPARATOR + getShortDescription();
				}
			};
			ImageIcon icon = createIcon(word.getBase64Img());
			if (icon != null) {
				completion.setIcon(icon);
			}
			provider.addCompletion(completion);
		}

	}

	private static ImageIcon createIcon(String base64Img) {
		if (base64Img == null) {
			return null;
		}
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64Img)));
			return img == null ? null : new ImageIcon(img);
		} catch (Exception e) { // a broken preview image must never break autocompletion
			return null;
		}
	}

	private void createHightLightMap() {
		TokenMap myWordsToHighlight = new TokenMap();
		for (AutocompletionText word : words) {
			myWordsToHighlight.put(word.getText(), TokenTypes.RESERVED_WORD);
		}
		// use ugly static setter because OwnTokenMaker is unfortunately not instantiated by us
		OwnTokenMaker.setMyWordsToHighlight(myWordsToHighlight);
		// switch syntaxstyle to null and back to OwnTokenMaker to make sure the wordsToHighlight are reset!
		textArea.setSyntaxEditingStyle(null);
		textArea.setSyntaxEditingStyle(OwnTokenMaker.ID);
	}

	public String getText() {
		return textArea.getText();
	}

	public JPanel getPanel() {
		return panel;
	}

	public void invalidate() {
		panel.invalidate();
	}

	public JTextComponent getTextComponent() {
		return textArea;
	}

	public void switchToElement(GridElement e) {
		words = e.getAutocompletionList();
		setText(e.getPanelAttributes());
	}

	public void switchToNonElement(String text) {
		words = new ArrayList<AutocompletionText>();
		setText(text);

	}

	private void setText(String text) {
		if (!textArea.getText().equals(text)) {
			textArea.setText(text); // Always set text even if they are equal to trigger correct syntax highlighting (if words to highlight have changed but text not)
			textArea.setCaretPosition(0);
		}

		createHightLightMap();
		createAutocompletionCompletionProvider();
	}

}
