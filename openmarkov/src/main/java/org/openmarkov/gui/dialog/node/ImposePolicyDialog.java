/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.action.SetPotentialEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.dialog.common.OkCancelApplyUndoRedoHorizontalDialog;
import org.openmarkov.gui.dialog.common.PolicyTypePanel;
import org.openmarkov.gui.dialog.common.PotentialPanel;
import org.openmarkov.gui.dialog.common.TablePotentialPanel;
import org.openmarkov.gui.localize.StringDatabase;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") public class ImposePolicyDialog extends OkCancelApplyUndoRedoHorizontalDialog {

	private Node node;
	private PolicyTypePanel pnlPolicyType;
	private PotentialPanel potentialPanel;
	private boolean readOnly;
	private Node dummyNode;

	public ImposePolicyDialog(Window owner, Node node) {
		super(owner);
		this.node = node;
		this.readOnly = false;
		node.getProbNet().getPNESupport().openParenthesis();
		initialize();

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();

		Rectangle bounds = owner.getBounds();
		int width = screenSize.width / 2;
		int height = screenSize.height / 2;

		// center point of the owner window
		int x = bounds.x / 2 - width / 2;
		int y = bounds.y / 2 - height / 2;
		this.setBounds(x, y, width, height);
		setLocationRelativeTo(owner);
		setMinimumSize(new Dimension(width, height / 2));
		setResizable(true);

		pack();
	}

	/**
	 * This method configures the dialog box.
	 */
	private void initialize() {

		setTitle(StringDatabase.getUniqueInstance().getString("ImposePolicydialog.Title.Label") + ": " + (
				node == null ? "" : node.getName()
		));

		configureComponentsPanel();
		pack();
	}

	/**
	 * Sets up the panel where all components, except the buttons of the buttons
	 * panel, will be appear.
	 */
	private void configureComponentsPanel() {
		getComponentsPanel().setLayout(new BorderLayout(5, 5));
		// getComponentsPanel().setSize(294, 29);
		getComponentsPanel().setMaximumSize(new Dimension(180, 40));
		getComponentsPanel().add(getPoliticyTypePanel(), BorderLayout.NORTH);
		getComponentsPanel().add(getPotentialPanel(), BorderLayout.CENTER);

	}

	/**
	 * @return PolicyTypePanel with three radio buttons with the types of
	 * policy: optimal, deterministic, or probabilistic
	 */
	protected PolicyTypePanel getPoliticyTypePanel() {

		if (pnlPolicyType == null) {
			// pnlPolicyType = new PolicyTypePanel(this, node);
		}
		return pnlPolicyType;
	}

	/**
	 * Gets the panel that matches the type of potential to be edited
	 *
	 * @return the potential panel matching the potential edited.
	 */
	private PotentialPanel getPotentialPanel() {

		if (potentialPanel == null) {
			// before creating TablePotentialPanel we have to set a
			// tablePotential to the decision node
			List<Variable> variables = new ArrayList<Variable>();
			// conditiones variable
			variables.add(node.getVariable());
			// adding variable parents
			List<Node> nodes = node.getProbNet().getNodes();
			for (Node node : nodes) {
				if (node.isParent(this.node)) {
					variables.add(node.getVariable());
				}
			}
			try {
				// copy of the node
				this.dummyNode = new Node(node);
				TablePotential policy = new TablePotential(variables, PotentialRole.POLICY);
				SetPotentialEdit setPotentialEdit = new SetPotentialEdit(dummyNode, policy);

				node.getProbNet().doEdit(setPotentialEdit);

			} catch (WrongCriterionException | ConstraintViolationException | NonProjectablePotentialException | DoEditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			potentialPanel = new TablePotentialPanel(node);
			potentialPanel.setReadOnly(readOnly);
		}
		return potentialPanel;
	}

	/**
	 * @return An integer indicating the button clicked by the user when closing
	 * this dialog
	 */
	public int requestValues() {

		setVisible(true);
		return selectedButton;
	}

	public Node getDummyNode() {
		return dummyNode;
	}

	/**
	 * This method carries out the actions when the user presses the OK button
	 * before hiding the dialog.
	 *
	 * @return true if all the fields are correct.
	 */
	@Override protected boolean doOkClickBeforeHide() {
		getPotentialPanel().close();
		node.getProbNet().getPNESupport().closeParenthesis();
		return true;
	}

	@Override protected void doCancelClickBeforeHide() {
		node.getProbNet().getPNESupport().closeParenthesis();
	}

}
