/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.gui.window.edition.EditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog box to add a finding in a node. The result of using this class is
 * equivalent to a double-click on a node's state when the working mode is
 * 'Inference mode'
 *
 * @author asaez
 * @version 1.0
 */
public class AddFindingDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 5618641549380924577L;
	/**
	 * Object where the finding will be set.
	 */
	protected VisualNode visualNode = null;
	/**
	 * Button group that holds the radio buttons that will be shown. There is a
	 * radio button for each state of the node.
	 */
	private ButtonGroup buttonGroup = null;

	private JSpinner evidenceSpinner;

	private EditorPanel editorPanel = null;

	private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

	/**
	 * This method initialises this instance.
	 *
	 * @param owner       window that owns this dialog.
	 * @param visualNode  the node to which this dialog is associated.
	 * @param finding
	 * @param g           the graphics context in which to paint.
	 * @param editorPanel the editor panel that called this dialog.
	 */
	public AddFindingDialog(Window owner, VisualNode visualNode, Finding finding, Graphics2D g,
			EditorPanel editorPanel) {
		this.visualNode = visualNode;
		this.editorPanel = editorPanel;
		this.buttonGroup = new ButtonGroup();
		JPanel principalPanel = new JPanel();
		JPanel textPanel = new JPanel();
		JPanel radioButtonsPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton(stringDatabase.getString("AddFindingDialog.OKButton.Label"));
		JButton cancelButton = new JButton(stringDatabase.getString("AddFindingDialog.CancelButton.Label"));
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		setTitle(stringDatabase.getString("AddFindingDialog.Title.Label"));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(principalPanel, BorderLayout.CENTER);
		principalPanel.setLayout(new BorderLayout());
		textPanel.setLayout(new GridLayout(3, 1));
		textPanel.add(new JLabel(""));
		textPanel.add(new JLabel(visualNode.getNode().getName(), SwingConstants.CENTER));
		textPanel.add(new JLabel(""));
		principalPanel.add(textPanel, BorderLayout.NORTH);
		Variable variable = visualNode.getNode().getVariable();
		if (variable.getVariableType() == VariableType.FINITE_STATES) {
			State[] states = variable.getStates();
			radioButtonsPanel.setLayout(new GridLayout(states.length, 1));
			for (int i = states.length - 1; i >= 0; i--) {
				String stateName = states[i].getName();
				JRadioButton jRadioButton = new JRadioButton(stateName);
				if (finding != null) {
					jRadioButton.setSelected(finding.getState().equals(stateName));
				}
				radioButtonsPanel.add(jRadioButton);
				jRadioButton.setActionCommand(stateName);
				if (i == 0) {
					jRadioButton.setSelected(true);
				}
				buttonGroup.add(jRadioButton);
			}
			principalPanel.add(radioButtonsPanel, BorderLayout.CENTER);
		} else {
			PartitionedInterval variableDomain = variable.getPartitionedInterval();
			double minValue = (variableDomain.isLeftClosed()) ?
					variableDomain.getMin() :
					variableDomain.getMin() + variable.getPrecision();
			double maxValue = (variableDomain.isRightClosed()) ?
					variableDomain.getMax() :
					variableDomain.getMax() - variable.getPrecision();
			double defaultValue = (finding != null) ? finding.getNumericalValue() : minValue;
			SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue,
					variable.getPrecision());
			evidenceSpinner = new JSpinner(model);
			evidenceSpinner.setPreferredSize(new Dimension(100, 20));
			JLabel valueLabel = new JLabel("Numeric value:");
			valueLabel.setLabelFor(evidenceSpinner);
			JPanel namelessPanel = new JPanel();
			namelessPanel.add(valueLabel);
			namelessPanel.add(evidenceSpinner);
			principalPanel.add(namelessPanel, BorderLayout.CENTER);
		}
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		principalPanel.add(buttonsPanel, BorderLayout.SOUTH);
		pack();
		setMinimumSize(new Dimension(260, getHeight()));
		int posX = owner.getX() + (owner.getWidth() - this.getWidth()) / 2;
		int posY = owner.getY() + (owner.getHeight() - this.getHeight()) / 2;
		this.setLocation(posX, posY);
		setModal(true);
		setIconImage(null);
	}

	@Override public void actionPerformed(ActionEvent actionEvent) {
		String command = actionEvent.getActionCommand();
		try {
			if (command.equals(stringDatabase.getString("AddFindingDialog.OKButton.Label"))) {
				Variable variable = visualNode.getNode().getVariable();
				if (variable.getVariableType() == VariableType.FINITE_STATES) {
					String selectedState = buttonGroup.getSelection().getActionCommand();
					editorPanel
							.setNewFinding(visualNode, new Finding(variable, variable.getState(selectedState)), false);
				} else {
					double evidenceValue = Double.parseDouble(evidenceSpinner.getValue().toString());
					editorPanel.setNewFinding(visualNode, new Finding(variable, evidenceValue), false);
				}
			}
			setVisible(false);
		} catch (InvalidStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
