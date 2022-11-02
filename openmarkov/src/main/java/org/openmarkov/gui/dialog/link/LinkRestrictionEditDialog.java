/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.link;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.dialog.common.OkCancelApplyUndoRedoHorizontalDialog;
import org.openmarkov.gui.dialog.common.ProbabilityTablePanel;
import org.openmarkov.gui.localize.StringDatabase;

import java.awt.*;
import java.text.MessageFormat;

/**
 * This class implements the link restriction dialog box for the edition of link
 * restrictions.
 */

@SuppressWarnings("serial") public class LinkRestrictionEditDialog extends OkCancelApplyUndoRedoHorizontalDialog {

	/****
	 * The link containing the link restrictions
	 */
	private Link<Node> link;

	/**
	 * Panel of the graphic editor
	 */
	private LinkRestrictionPanel linkRestrictionPanel;

	public LinkRestrictionEditDialog(Window owner, Link<Node> link) {
		super(owner);
		this.link = link;
		link.getNode1().getProbNet().getPNESupport().openParenthesis();
		initialize();
		setLocationRelativeTo(owner);
		setMinimumSize(new Dimension(750, 450));
		setResizable(true);
	}

	/**
	 * This method configures the dialog box.
	 */
	private void initialize() {

		Node node1 = link.getNode1();
		Node node2 = link.getNode2();
		String title = "";
		if (link != null) {
			MessageFormat messageForm = new MessageFormat(
					StringDatabase.getUniqueInstance().getString("LinkRestrictionDialog.Title.Label"));
			Object[] labelArgs = new Object[] { node1.getName(), node2.getName() };
			title = messageForm.format(labelArgs);
		}
		setTitle(title);
		configureComponentsPanel();
		pack();
	}

	/**
	 * Sets up the panel where all components, except the buttons of the buttons
	 * panel, will be appear.
	 */
	private void configureComponentsPanel() {
		getComponentsPanel().setLayout(new BorderLayout(5, 5));

		getComponentsPanel().add(getLinkRestrictionPanel(), BorderLayout.CENTER);
	}

	private ProbabilityTablePanel getLinkRestrictionPanel() {

		if (this.linkRestrictionPanel == null) {
			this.linkRestrictionPanel = new LinkRestrictionPanel(link);
		}
		return linkRestrictionPanel;
	}

	/**
	 * @return An integer indicating the button clicked by the user when closing
	 * this dialog
	 */
	public int requestValues() {
		setVisible(true);
		return selectedButton;
	}

	/**
	 * This method carries out the actions when the user presses the OK button
	 * before hiding the dialog.
	 *
	 * @return true if all the fields are correct.
	 */
	@Override protected boolean doOkClickBeforeHide() {

		getLinkRestrictionPanel().close();
		link.getNode1().getProbNet().getPNESupport().closeParenthesis();
		return true;
	}

	@Override protected void doCancelClickBeforeHide() {

		link.getNode1().getProbNet().getPNESupport().closeParenthesis();

	}

}
