/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.common;

import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jorge on 01/07/2015.
 */
public class ScopeSelectorPanel extends JPanel {

	HashMap<JComboBox<String>, Variable> selectedScenario;
	private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	private JPanel scopeTypePanel;
	private ButtonGroup scopeTypeSelector;
	private JPanel decisionSelectorPanel;
	private JPanel mainPanel;
	private ProbNet probNet;
	private Variable decisionSelected;
	private JRadioButton globalRadioButton;
	private JRadioButton decisionRadioButton;
	private JPanel decisionScenarioPanel;
	private JScrollPane decisionScenarioScroll;
	private ScopeType scopeType;
	private JComboBox<String> decisionSelector;

	private List<Finding> selectedFindings;

	private EvidenceCase preResolutionEvidence;

	public ScopeSelectorPanel(ProbNet probNet, EvidenceCase preResolutionEvidence) {
		super();
		this.probNet = probNet;
		this.preResolutionEvidence = preResolutionEvidence;
		selectedFindings = new ArrayList<>();
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(new TitledBorder(stringDatabase.getString("ScopeSelector.Title")));
		this.add(getMainPanel());
		this.setVisible(true);
		setMaximumSize(new Dimension(300, 300));
	}

	public JPanel getMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.add(getScopeTypePanel());

		mainPanel.add(getDecisionSelectorPanel());

		List<Node> decisionNodes = probNet.getNodes(NodeType.DECISION);
		if (decisionNodes == null || decisionNodes.size() < 1) {
			for (Component component : scopeTypePanel.getComponents()) {
				component.setEnabled(false);
			}
		}

		decisionScenarioScroll = new JScrollPane(getDecisionScenarioPanel());
		mainPanel.add(decisionScenarioScroll);
		return mainPanel;
	}

	public JPanel getScopeTypePanel() {
		scopeTypePanel = new JPanel();
		scopeTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel scopeLabel = new JLabel(stringDatabase.getString("ScopeSelector.Type"));
		scopeTypePanel.add(scopeLabel);

		JPanel scopeTypeSelectorPanel = new JPanel();
		scopeTypeSelectorPanel.setLayout(new FlowLayout());

		scopeTypeSelector = new ButtonGroup();
		for (ScopeType scopeTypeEnum : ScopeType.values()) {
			JRadioButton selectedScopeType = new JRadioButton(stringDatabase.getString(scopeTypeEnum.toString()));
			scopeTypeSelector.add(selectedScopeType);

			//            scopeTypeSelector.addItem(stringDatabase.getValuesInAString(scopeTypeEnum.toString()));

			selectedScopeType.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					JRadioButton scopeSelector = (JRadioButton) e.getSource();
					if (scopeSelector.getText().equals(stringDatabase.getString(ScopeType.GLOBAL.toString()))) {
						setScopeType(ScopeType.GLOBAL);
						decisionSelected = null;

						if (decisionSelectorPanel != null) {
							for (Component component : decisionSelectorPanel.getComponents()) {
								component.setEnabled(false);
							}
						}
					} else {
						setScopeType(ScopeType.DECISION);
						if (decisionSelector != null) {
							try {
								decisionSelected = probNet.getVariable(decisionSelector.getSelectedItem().toString());
							} catch (NodeNotFoundException e1) {
								e1.printStackTrace();
							}
						}
						if (decisionSelectorPanel != null) {
							for (Component component : decisionSelectorPanel.getComponents()) {
								component.setEnabled(true);
							}
						}
					}
					refreshScenario();
				}
			});
			if (scopeTypeEnum.equals(ScopeType.GLOBAL)) {
				globalRadioButton = selectedScopeType;
				scopeTypeSelectorPanel.add(globalRadioButton);
			} else if (scopeTypeEnum.equals(ScopeType.DECISION)) {
				decisionRadioButton = selectedScopeType;
				scopeTypeSelectorPanel.add(decisionRadioButton);
			}
		}
		scopeTypePanel.add(scopeTypeSelectorPanel);

		boolean couldBeGlobal = true;
		boolean couldBeDecision = true;

		// Get all the avaible decision nodes (without policy)
		List<Node> avaibleDecisionNodes = new ArrayList<>();
		for (Node node : probNet.getNodes(NodeType.DECISION)) {
			if (node.getPotentials().size() == 0) {
				avaibleDecisionNodes.add(node);
			}
		}

		if (avaibleDecisionNodes.size() == 0) {
			couldBeDecision = false;
		}

		if (probNet.getNetworkType() instanceof DecisionAnalysisNetworkType) {
			couldBeDecision = false;
		}

		if (!couldBeDecision || !couldBeGlobal) {
			if (couldBeGlobal) {
				//scopeTypeSelector.setSelectedItem(stringDatabase.getValuesInAString(ScopeType.GLOBAL.toString()));
				globalRadioButton.setSelected(true);
				decisionRadioButton.setEnabled(false);
				setScopeType(ScopeType.GLOBAL);
			} else {
				//scopeTypeSelector.setSelectedItem(stringDatabase.getValuesInAString(ScopeType.DECISION.toString()));
				decisionRadioButton.setSelected(true);
				globalRadioButton.setEnabled(false);
				setScopeType(ScopeType.DECISION);
			}
			for (Component component : scopeTypePanel.getComponents()) {
				component.setEnabled(false);
			}
		} else {
			//            scopeTypeSelector.setSelectedItem(stringDatabase.getValuesInAString(ScopeType.GLOBAL.toString()));
			decisionRadioButton.setSelected(true);
			setScopeType(ScopeType.DECISION);

		}

		return scopeTypePanel;
	}

	public JPanel getDecisionSelectorPanel() {

		decisionSelectorPanel = new JPanel();
		decisionSelectorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		decisionSelectorPanel.add(new JLabel(stringDatabase.getString("ScopeSelector.DecisionSelector")));

		decisionSelector = new JComboBox<>();
		for (Node node : probNet.getNodes(NodeType.DECISION)) {
			// If the decision has not an imposed policy, add to the selector
			if (node.getPotentials().size() == 0) {
				decisionSelector.addItem(node.getName());
			}
		}
		decisionSelector.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				String itemSelected = (String) ((JComboBox) e.getSource()).getSelectedItem();
				try {
					setDecisionSelected(probNet.getVariable(itemSelected));
				} catch (NodeNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		if (decisionSelector.getItemCount() > 0) {
			decisionSelector.setSelectedIndex(0);
		}
		decisionSelectorPanel.add(decisionSelector);

		if (scopeType.equals(ScopeType.GLOBAL)) {
			setDecisionSelected(null);
			for (Component component : decisionSelectorPanel.getComponents()) {
				component.setEnabled(false);
			}
		} else {
			try {
				decisionSelected = probNet.getVariable(decisionSelector.getSelectedItem().toString());
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
		}

		return decisionSelectorPanel;
	}

	public JPanel getDecisionScenarioPanel() {

		decisionScenarioPanel = new JPanel();
		decisionScenarioPanel.setLayout(new BoxLayout(decisionScenarioPanel, BoxLayout.PAGE_AXIS));
		decisionScenarioPanel.setBorder(new TitledBorder(stringDatabase.getString("ScopeSelector.Scenario")));

		if (decisionSelected != null && scopeType == ScopeType.DECISION) {
			selectedScenario = new HashMap<>();
			this.selectedFindings = new ArrayList<>();

			List<Variable> informationalPredecessors = ProbNetOperations
					.getInformationalPredecessors(probNet, decisionSelected);
			for (Variable variable : informationalPredecessors) {
				JPanel informationalPredecessorPanel = new JPanel();
				informationalPredecessorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				if (!variable.equals(decisionSelected)) {
					informationalPredecessorPanel.add(new JLabel(variable.getName()));
					JComboBox<String> stateSelector = new JComboBox<>();
					for (State state : variable.getStates()) {
						stateSelector.addItem(state.getName());
					}
					if (preResolutionEvidence.contains(variable)) {
						stateSelector.setSelectedItem(preResolutionEvidence.getFinding(variable).getState());
						stateSelector.setEnabled(false);
					}
					selectedScenario.put(stateSelector, variable);

					stateSelector.addActionListener(new ActionListener() {
						@Override public void actionPerformed(ActionEvent e) {
							JComboBox<String> stateSelector = (JComboBox<String>) e.getSource();
							String selectedStateString = stateSelector.getSelectedItem().toString();
							Variable selectedVariable = selectedScenario.get(stateSelector);
							try {
								State selectedState = selectedVariable.getState(selectedStateString);
								updateSelectedScenario();
							} catch (InvalidStateException e1) {
								e1.printStackTrace();
							}

						}
					});

					informationalPredecessorPanel.add(stateSelector);
				}
				decisionScenarioPanel.add(informationalPredecessorPanel);
			}
			updateSelectedScenario();
		}
		return decisionScenarioPanel;
	}

	private void updateSelectedScenario() {
		List<Finding> selectedFindings = new ArrayList<>();

		for (JComboBox<String> comboBox : selectedScenario.keySet()) {
			Variable variable = selectedScenario.get(comboBox);
			try {
				Finding finding = new Finding(variable, variable.getState(comboBox.getSelectedItem().toString()));
				selectedFindings.add(finding);
			} catch (InvalidStateException e) {
				e.printStackTrace();
			}
		}

		this.selectedFindings = selectedFindings;
	}

	private void refreshScenario() {
		mainPanel.setVisible(false);
		if (decisionScenarioScroll != null) {
			mainPanel.remove(decisionScenarioScroll);
			decisionScenarioScroll = new JScrollPane(getDecisionScenarioPanel());
			mainPanel.add(decisionScenarioScroll);
		}
		mainPanel.setVisible(true);
	}

	public ScopeType getScopeType() {
		return scopeType;
	}

	public void setScopeType(ScopeType scopeType) {
		this.scopeType = scopeType;
	}

	public Variable getDecisionSelected() {
		return decisionSelected;
	}

	public void setDecisionSelected(Variable decisionSelected) {
		this.decisionSelected = decisionSelected;
		refreshScenario();
	}

	public List<Finding> getSelectedFindings() {
		return selectedFindings;
	}
}
