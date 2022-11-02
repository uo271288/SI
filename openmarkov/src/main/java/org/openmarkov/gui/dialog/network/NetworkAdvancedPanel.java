
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.gui.dialog.node.NodePropertiesDialog;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.gui.util.Utilities;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial") public class NetworkAdvancedPanel extends JPanel implements ActionListener {
	private ProbNet probNet;
	private boolean newNetwork;
	private JButton agentsButton;
	private JButton decisionCriteriaButton;
	private JButton temporalOptionsButton;

	/**
	 * This method initialises this instance.
	 *
	 * @param newNetwork to indicate if the panel is for new networks
	 * @param probNet    manage the network access
	 */
	public NetworkAdvancedPanel(final boolean newNetwork, ProbNet probNet) {
		this.probNet = probNet;
		this.newNetwork = newNetwork;
		setName("NetworkAdvancedPanel");
		initialize();
		getAgentsButton().setEnabled(probNet.getAgents() != null);
		getDecisionCriteriaButton().setEnabled(!probNet.onlyChanceNodes());
		getTemporalOptionsButton().setEnabled(!probNet.hasConstraint(OnlyAtemporalVariables.class));
	}

	private void initialize() {
		GroupLayout groupLayout = new GroupLayout(this);
		ParallelGroup parallelGroup = groupLayout.createParallelGroup(Alignment.LEADING);
		SequentialGroup sequentialGroup = groupLayout.createSequentialGroup();
		sequentialGroup.addGap(173);
		sequentialGroup.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
				.addComponent(getAgentsButton(), Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE)
				.addComponent(getDecisionCriteriaButton(), Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 268,
						Short.MAX_VALUE)
				.addComponent(getTemporalOptionsButton(), Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap();

		parallelGroup.addGroup(sequentialGroup);
		groupLayout.setHorizontalGroup(parallelGroup);

		SequentialGroup verticalSequentialGroup = groupLayout.createSequentialGroup();
		verticalSequentialGroup.addGap(131);
		verticalSequentialGroup.addComponent(getDecisionCriteriaButton());
		verticalSequentialGroup.addPreferredGap(ComponentPlacement.UNRELATED);
		verticalSequentialGroup.addComponent(getAgentsButton());
		verticalSequentialGroup.addPreferredGap(ComponentPlacement.UNRELATED);
		//verticalSequentialGroup.addContainerGap (219, Short.MAX_VALUE);
		verticalSequentialGroup.addComponent(getTemporalOptionsButton());
		verticalSequentialGroup.addContainerGap(219, Short.MAX_VALUE);

		ParallelGroup verticalParallelGroup = groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(verticalSequentialGroup);

		groupLayout.setVerticalGroup(verticalParallelGroup);
		setLayout(groupLayout);
	}

	private JButton getAgentsButton() {
		if (agentsButton == null) {
			String buttonCaption = StringDatabase.getUniqueInstance().getString("NetworkAdvancedPanel.Agents.Text");
			agentsButton = new JButton(buttonCaption);
			// agentsButton.setMinimumSize();
			agentsButton.addActionListener(this);
		}
		return agentsButton;
	}

	private JButton getDecisionCriteriaButton() {
		if (decisionCriteriaButton == null) {
			String buttonCaption = StringDatabase.getUniqueInstance()
					.getString("NetworkAdvancedPanel.DecisionCriteria.Text");
			decisionCriteriaButton = new JButton(buttonCaption);
			// decisionCriteriaButton.setMinimumSize(60);
			decisionCriteriaButton.addActionListener(this);
		}
		return decisionCriteriaButton;
	}

	private JButton getTemporalOptionsButton() {
		if (temporalOptionsButton == null) {
			String buttonCaption = StringDatabase.getUniqueInstance()
					.getString("NetworkAdvancedPanel.TemporalOptions.Title");
			temporalOptionsButton = new JButton(buttonCaption);
			temporalOptionsButton.addActionListener(this);
		}
		return temporalOptionsButton;
	}

	@Override public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(agentsButton)) {
			actionPerformedAgents();
		} else if (e.getSource().equals(decisionCriteriaButton)) {
			actionPerformedDecisionCriteria();
		} else if (e.getSource().equals(temporalOptionsButton)) {
			actionPerformedTemporalOptions();
		}
	}

	protected void actionPerformedAgents() {
		NetworkAgentsDialog networkAgentsDialog = new NetworkAgentsDialog(Utilities.getOwner(this), probNet,
				newNetwork);
		if (networkAgentsDialog.requestValues() == NodePropertiesDialog.OK_BUTTON) {
		}
	}

	protected void actionPerformedDecisionCriteria() {
		DecisionCriteriaDialog decisionCriteriaDialog = new DecisionCriteriaDialog(Utilities.getOwner(this), probNet,
				newNetwork);
		if (decisionCriteriaDialog.requestValues() == NodePropertiesDialog.OK_BUTTON) {
		}
	}

	private void actionPerformedTemporalOptions() {
		NetworkTemporalOptionsDialog networkTemporalOptionsDialog = new NetworkTemporalOptionsDialog(
				Utilities.getOwner(this), probNet);
		networkTemporalOptionsDialog.setVisible(true);
	}

	public void update(ProbNet probNet) {
		this.probNet = probNet;
		getAgentsButton().setEnabled(probNet.getAgents() != null);
		getDecisionCriteriaButton().setEnabled(!probNet.onlyChanceNodes());
		getTemporalOptionsButton().setEnabled(!probNet.hasConstraint(OnlyAtemporalVariables.class));
	}
}
