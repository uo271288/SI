/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import com.hexidec.ekit.EkitCore;
import com.hexidec.ekit.compoment.ExtendedHTMLDocument;
import org.openmarkov.gui.dialog.CommentListener;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;

/**
 * Comment HTML Box Scroll Pane This class encapsulate all the behaviour as a
 * single component so the programmer must not be worried about internal
 * initialization of the components
 *
 * @author jlgozalo
 * @version 1.0 based on definition made by alberto
 */
public class CommentHTMLScrollPane extends JScrollPane implements MouseListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -8678529566501560594L;
	private static int HTML_COMMENT_HEIGHT = 10;
	private static int HTML_COMMENT_WIDTH = 30;
	private JTextPane jTextPaneCommentHTML = null;
	private HTMLTextEditor hTMLTextEditor = null;
	/**
	 * Listener to the comment changes.
	 */
	private HashSet<CommentListener> commentListeners = new HashSet<CommentListener>();
	private String title = "";
	private boolean isChanged = false;
	private boolean isEmpty = true;
	private boolean isEditable = true;
	/**
	 * Double Click Selector for the HTML Comment area
	 */
	private MouseListener doubleClickSelector = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		}
	};

	/**
	 * This method initialises this instance.
	 */
	public CommentHTMLScrollPane() {
		initialize();
	}

	/**
	 * This method initialises this instance.
	 */
	public CommentHTMLScrollPane(final String title) {
		this.title = title;
		initialize();
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
		if (hTMLTextEditor != null) {
			hTMLTextEditor.setTitle(title);
		}
	}

	/**
	 * This method configures the dialog box.
	 */
	private void initialize() {
		setBorder(BorderFactory.createLineBorder(SystemColor.activeCaption, 2));
		setViewportView(getJTextPaneCommentHTML());
		setSize(HTML_COMMENT_WIDTH, HTML_COMMENT_HEIGHT);
		hTMLTextEditor = new HTMLTextEditor(null, "");
		hTMLTextEditor.setTitle(title);
		hTMLTextEditor.setVisible(false);
	}

	/**
	 * This method initialises jTextPaneCommentHTML
	 *
	 * @return the JTextPane with the HTML Comment
	 */
	private JTextPane getJTextPaneCommentHTML() {
		if (jTextPaneCommentHTML == null) {
			jTextPaneCommentHTML = new JTextPane();
			jTextPaneCommentHTML.setEditable(false);
			jTextPaneCommentHTML.setSize(new Dimension(HTML_COMMENT_WIDTH, HTML_COMMENT_HEIGHT));
			// only to put an initial value just in case
			jTextPaneCommentHTML.setText(
					StringDatabase.getUniqueInstance().getString("CommentHTMLScrollPane.jTextPaneCommentHTML.Text"));
			jTextPaneCommentHTML.addMouseListener(doubleClickSelector);
			jTextPaneCommentHTML.addMouseListener(this);
		}
		return jTextPaneCommentHTML;
	}

	/**
	 * This method set the text of jTextPaneCommentHTML
	 *
	 * @param text the text to put in the comment
	 */
	public void setCommentHTMLTextPaneText(String text) {
		String sLanguage = StringDatabase.getUniqueInstance().getLanguage();
        /*
        Country is used in Locale.java just to set the LocaleExtensions as
        LocaleExtensions.CALENDAR_JAPANESE or LocaleExtensions.NUMBER_THAI
        But it cannot be set up as null or empty. So we will not use the "real country"
        but just the same string as the language.
         */
		String sCountry = StringDatabase.getUniqueInstance().getLanguage();

		// creates the HTML object
		EkitCore ekitCoreEditorHTML = new EkitCore(null, null, text, null, null, true, false, true, true, sLanguage,
				sCountry, false, false, true, false, EkitCore.TOOLBAR_DEFAULT_SINGLE);
		try {
			getJTextPaneCommentHTML().setEditorKit((StyledEditorKit) ekitCoreEditorHTML.gethtmlKit());
			getJTextPaneCommentHTML().setDocument(ekitCoreEditorHTML.getExtendedHtmlDoc());
			getJTextPaneCommentHTML().setText(text);
			getJTextPaneCommentHTML().setCaretPosition(0);
		} catch (IllegalArgumentException ex) {
		}
	}

	/**
	 * This method get the text of jTextPaneCommentHTML
	 *
	 * @return the text of the comment
	 */
	public String getCommentText() {
		String text = "";
		if (jTextPaneCommentHTML == null) {
			// do nothing
		} else {
			text = jTextPaneCommentHTML.getText();
		}
		return text;
	}

	/**
	 * public method to set the document hTMLTextEditor in the JTextPane
	 *
	 * @param doc - the HTML document to put in the hTMLTextEditor
	 */
	public void setCommentHTMLText(ExtendedHTMLDocument doc) {
		jTextPaneCommentHTML.setEditorKit(getHTMLTextEditor().getExtendedHTMLEditorKit());
		jTextPaneCommentHTML.setDocument(doc);
		jTextPaneCommentHTML.setText(hTMLTextEditor.getCommentText());
	}

	/**
	 * initialize the HTMLTextEditor component
	 */
	private HTMLTextEditor getHTMLTextEditor() {
		if (hTMLTextEditor == null) {
			hTMLTextEditor = new HTMLTextEditor(null, jTextPaneCommentHTML.getText());
			hTMLTextEditor.setTitle(title);
			hTMLTextEditor.setVisible(true);
		}
		return hTMLTextEditor;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		if ((e.getClickCount() == 2) && (isEditable)) {
			try {
				String comment = jTextPaneCommentHTML.getText() != null ? jTextPaneCommentHTML.getText() : "";
				hTMLTextEditor = new HTMLTextEditor(null, comment);
				hTMLTextEditor.setTitle(title);
				hTMLTextEditor.setVisible(true);
				if (hTMLTextEditor.getOkButtonStatus()) {
					jTextPaneCommentHTML.setEditorKit((StyledEditorKit) hTMLTextEditor.getExtendedHTMLEditorKit());
					jTextPaneCommentHTML.setDocument(hTMLTextEditor.getEextendedHTMLDocument());
					jTextPaneCommentHTML.setText(hTMLTextEditor.getCommentText());
					isChanged = true;
					isEmpty = hTMLTextEditor.getCommentText().trim().replaceAll("[\r\n]", "").equals("");
					notifyCommentChanged();
				}
			} catch (IllegalArgumentException ex) {
			}
		}
	}

	private void notifyCommentChanged() {
		for (CommentListener listener : commentListeners) {
			listener.commentHasChanged();
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void addCommentListener(CommentListener newCommentListener) {
		commentListeners.add(newCommentListener);
	}

	public boolean isChanged() {
		return isChanged;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}
}
