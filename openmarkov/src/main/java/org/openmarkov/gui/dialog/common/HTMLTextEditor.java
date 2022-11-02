/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import com.hexidec.ekit.EkitCore;
import com.hexidec.ekit.compoment.ExtendedHTMLDocument;
import com.hexidec.ekit.compoment.ExtendedHTMLEditorKit;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;

/**
 * initialises a HTML Editor for the different comments in EditNodeDialog
 *
 * @author Alberto Manuel Ruiz Lafuente UCLM 2008
 * @version 1.1 jlgozalo - javadocs, undo variables and localize methods
 */
public class HTMLTextEditor extends JDialog {

	/**
	 * ToolBar elements for the dialog
	 */
	public static final String TOOLBAR_OPENMARKOV_SINGLE = "CT|CP|PS|SP|UN|RE|SP|BL|IT|UD|SP|UC|SP|SR|SP|FO";
	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 7066844472238575449L;
	private JPanel jContentPane = null;

	private JToolBar jToolBarEditorHTML = null;

	private EkitCore ekitCoreEditorHTMLPanel = null;

	private JButton jButtonAcceptHTML = null;

	private JButton jButtonCancelHTML = null;

	private boolean okButton = false;

	/**
	 * Document with the new comment to modify
	 */
	private String updateComment = "";

	/**
	 * Document to keep the original document text for undo
	 * and the new one if it is accepted after the edition
	 */
	private String commentText = "";

	private ExtendedHTMLEditorKit extendedHTMLEditorKit = null;
	private ExtendedHTMLDocument extendedHTMLDocument = null;

	/**
	 * HTMLTextEditor dialog constructor
	 *
	 * @param owner         The frame where the dialog belongs to
	 * @param updateComment the comment to update
	 */
	public HTMLTextEditor(Frame owner, final String updateComment) {

		super(owner);

		this.updateComment = updateComment;
		this.commentText = this.updateComment; //to be used for undo
		initialize();
	}

	/**
	 * initialises the dialog
	 */
	private void initialize() {

		this.setSize(636, 321);
		this.setLocation(new Point(240, 250));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(true);
		this.setVisible(false);
		this.setTitle(StringDatabase.getUniqueInstance().getString("HTMLTextEditor.Title.Text"));
		this.setContentPane(getJContentPane());
		this.setIconImage(OpenMarkovLogoIcon.getUniqueInstance().
				getOpenMarkovLogoIconImage16());

	}

	/**
	 * This method initialises jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {

		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setSize(new Dimension(600, 300));
			jContentPane.add(getEkitCoreEditorHTMLPanel(), null);
			jContentPane.add(getJToolBarEditorHTML(), null);
			jContentPane.add(getJButtonAcceptHTML(), null);
			jContentPane.add(getJButtonCancelHTML(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initialises ekitCoreEditorHTMLPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getEkitCoreEditorHTMLPanel() {
		String sLanguage = StringDatabase.getUniqueInstance().getLanguage();
        /*
        Country is used in Locale.java just to set the LocaleExtensions as
        LocaleExtensions.CALENDAR_JAPANESE or LocaleExtensions.NUMBER_THAI
        But it cannot be set up as null or empty. So we will not use the "real country"
        but just the same string as the language.
         */
		String sCountry = StringDatabase.getUniqueInstance().getLanguage();

		String toolbar = EkitCore.TOOLBAR_DEFAULT_SINGLE;
		if (ekitCoreEditorHTMLPanel == null) {
			ekitCoreEditorHTMLPanel = new EkitCore(null, null, updateComment, null, null, true, false, true, true,
					sLanguage, sCountry, false, false, true, false, toolbar);
			ekitCoreEditorHTMLPanel.setBounds(new Rectangle(2, 34, 619, 220));
			ekitCoreEditorHTMLPanel.setVisible(true);
		}
		return ekitCoreEditorHTMLPanel;
	}

	/**
	 * This method initialises jToolBarEditorHTML
	 *
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getJToolBarEditorHTML() {

		if (jToolBarEditorHTML == null) {
			jToolBarEditorHTML = ekitCoreEditorHTMLPanel.getToolBar(true);
			jToolBarEditorHTML.setBounds(new Rectangle(1, 1, 617, 30));
		}
		return jToolBarEditorHTML;
	}

	/**
	 * This method initialises jButtonAcceptHTML
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonAcceptHTML() {

		if (jButtonAcceptHTML == null) {
			jButtonAcceptHTML = new JButton();
			jButtonAcceptHTML.setBounds(new Rectangle(183, 258, 106, 21));
			jButtonAcceptHTML
					.setText(StringDatabase.getUniqueInstance().getString("HTMLTextEditor.jButtonAcceptHTML.Text"));
			jButtonAcceptHTML.addActionListener(new java.awt.event.ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {
					//update the commentText
					commentText = ekitCoreEditorHTMLPanel.getDocumentBody();
					extendedHTMLEditorKit = ekitCoreEditorHTMLPanel.gethtmlKit();
					extendedHTMLDocument = ekitCoreEditorHTMLPanel.getExtendedHtmlDoc();
					setVisible(false);
					okButton = true;
				}
			});
		}
		return jButtonAcceptHTML;
	}

	/**
	 * This method initialises jButtonCancelHTML
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancelHTML() {

		if (jButtonCancelHTML == null) {
			jButtonCancelHTML = new JButton();
			jButtonCancelHTML.setBounds(new Rectangle(306, 258, 106, 21));
			jButtonCancelHTML
					.setText(StringDatabase.getUniqueInstance().getString("HTMLTextEditor.jButtonCancelHTML.Text"));
			// setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			jButtonCancelHTML.addActionListener(new java.awt.event.ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {

					extendedHTMLEditorKit = ekitCoreEditorHTMLPanel.gethtmlKit();
					extendedHTMLDocument = ekitCoreEditorHTMLPanel.getExtendedHtmlDoc();
					setVisible(false);
				}
			});

		}
		return jButtonCancelHTML;
	}

	/**
	 * Method to return the document text
	 *
	 * @return String with the Text
	 */
	public String getCommentText() {

		return commentText;
	}

	/**
	 * Return an object to set the EditorKit of JTextPane
	 *
	 * @return extendedHTMLEditorKit
	 */
	public ExtendedHTMLEditorKit getExtendedHTMLEditorKit() {

		return extendedHTMLEditorKit;
	}

	/**
	 * Return an object to set the Document of JTextPane
	 *
	 * @return extendedHTMLDocument
	 */
	public ExtendedHTMLDocument getEextendedHTMLDocument() {

		return extendedHTMLDocument;
	}

	public boolean getOkButtonStatus() {
		return okButton;
	}

}
