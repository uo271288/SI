/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.dialog.common.OkCancelHorizontalDialog;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

@SuppressWarnings("serial") public class DecisionCriteriaDialog extends OkCancelHorizontalDialog {

	private DecisionCriteriaTablePanel decisionCriteriaTablePanel;

	private JPanel componentsPanel;

	private ProbNet probNet;

	public DecisionCriteriaDialog(Window owner, ProbNet probNet, boolean newElement) {
		super(owner);
		this.probNet = probNet;

		probNet.getPNESupport().setWithUndo(true);
		probNet.getPNESupport().openParenthesis();
		initialize();
		setName("DecisionCriteriaDialog");
		setLocationRelativeTo(owner);
		pack();

	}

	/**
	 * This method configures the dialog box.
	 */
	private void initialize() {

		setTitle(stringDatabase.getString("DecisionCriteria.Title.Label"));
		configureComponentsPanel();
		pack();
	}

	private void configureComponentsPanel() {

		getComponentsPanel().add(getDecisionCriteriaPanel());
		// setFieldFromProperties(probNet);
	}

	/**
	 * This method initialises componentsPanel.
	 *
	 * @return a new components panel.
	 */
	protected JPanel getComponentsPanel() {

		if (componentsPanel == null) {
			componentsPanel = new JPanel();
		}

		return componentsPanel;

	}

	private DecisionCriteriaTablePanel getDecisionCriteriaPanel() {
		if (decisionCriteriaTablePanel == null) {
			StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

			String[] columnNames = {
					stringDatabase.getString("NetworkAdvancedPanel.DecisionCriteria.ValuesTable.Columns.Id.Text"),
					stringDatabase.getString("NetworkAdvancedPanel.DecisionCriteria.ValuesTable.Columns.Name.Text"),
					stringDatabase.getString("NetworkAdvancedPanel.DecisionCriteria.ValuesTable.Columns.Unit.Text") };

			decisionCriteriaTablePanel = new DecisionCriteriaTablePanel(columnNames, probNet, this.getOwner());

			decisionCriteriaTablePanel.setName("DecisionCriteriaPanel");
			decisionCriteriaTablePanel.setBorder(new EmptyBorder(0, 0, 0, 0));

		}

		return decisionCriteriaTablePanel;

	}

	public void setFieldFromProperties(ProbNet probNet) {

		// StringsWithProperties agents = probNet.getAgents();
		List<Criterion> decisionCriteria = probNet.getDecisionCriteria();
		if (decisionCriteria != null) {
			Object[][] data = new Object[decisionCriteria.size()][2];
			for (int i = 0; i < decisionCriteria.size(); i++) {
				data[i][0] = decisionCriteria.get(i).getCriterionName();
				data[i][1] = decisionCriteria.get(i).getCriterionUnit();
			}
			// initializing data structure for the table model
			getDecisionCriteriaPanel().setData(data);
			// initializing data structure for supervising data order in GUI
			getDecisionCriteriaPanel().setDataTable(data);
		}
	}

	public int requestValues() {
		setFieldFromProperties(probNet);
		setVisible(true);
		return selectedButton;
	}

	/**
	 * This method carries out the actions when the user press the Ok button
	 * before hide the dialog.
	 *
	 * @return true if the dialog box can be closed.
	 */
	protected boolean doOkClickBeforeHide() {
		probNet.getPNESupport().closeParenthesis();
		return true;
	}

	/**
	 * This method carries out the actions when the user press the Cancel button
	 * before hide the dialog.
	 */
	@Override protected void doCancelClickBeforeHide() {
		probNet.getPNESupport().closeParenthesis();
		probNet.getPNESupport().undoAndDelete();

		// TODO PNESupport must support more depth levels parenthesis
		// As current performance edits from NetworkAgentsPanel only be undone
		// when cancel
		// NodesPropertiesDialog
		/*
		 * for (int i = getDecisionCriteriaPanel().getEdits().size()-1; i >=0;
		 * i--) { getDecisionCriteriaPanel().getEdits().get(i).undo(); }
		 */

	}

}
