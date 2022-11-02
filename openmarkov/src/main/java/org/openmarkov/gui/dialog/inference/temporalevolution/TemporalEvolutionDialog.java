/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.dialog.common.OkCancelHorizontalDialog;
import org.openmarkov.gui.dialog.inference.common.ScopeSelectorPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Created by Jorge on 29/07/2015.
 */
public class TemporalEvolutionDialog extends OkCancelHorizontalDialog {

	private JLabel numSlicesLabel;
	private JTextField numSlicesTextField;
	private Integer numSlices;
	private ProbNet probNet;
	private ScopeSelectorPanel scopeSelectorPanel;
	private Node selectedNode;
	private EvidenceCase preResolutionEvidence;

	/**
	 * Constructor. initialises the instance.
	 *
	 * @param owner window that owns the dialog.
	 */
	public TemporalEvolutionDialog(Window owner, Node selectedNode, EvidenceCase preResolutionEvidence) {
		super(owner);
		setMinimumSize(new Dimension(300, 300));
		this.setResizable(true);
		this.probNet = selectedNode.getProbNet();
		this.selectedNode = selectedNode;
		this.preResolutionEvidence = preResolutionEvidence;
		this.setTitle(stringDatabase.getString("TemporalEvolutionResultDialog.Title.Label") + selectedNode.getProbNet()
				.getName());
		getComponentsPanel().setLayout(new BoxLayout(getComponentsPanel(), BoxLayout.PAGE_AXIS));
		getComponentsPanel().add(getSlicesPanel());
		getComponentsPanel().add(getScopeSelectorPanel());
		setLocationRelativeTo(owner);
		this.pack();
		this.setVisible(true);

	}

	public JPanel getSlicesPanel() {
		JPanel slicesPanel = new JPanel();
		slicesPanel.add(getJLabelNumSlices());
		slicesPanel.add(getNumSlicesTextField());
		slicesPanel.setBorder(new TitledBorder(stringDatabase.getString("Inference.TemporalOptions.Label")));
		slicesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		return slicesPanel;
	}

	private JLabel getJLabelNumSlices() {
		if (numSlicesLabel == null) {
			numSlicesLabel = new JLabel(stringDatabase.getString("CostEffectiveness.NumberOfCycles"));
		}
		return numSlicesLabel;
	}

	private JTextField getNumSlicesTextField() {
		if (numSlicesTextField == null) {
			numSlices = probNet.getInferenceOptions().getTemporalOptions().getHorizon();
			numSlicesTextField = new JTextField();
			numSlicesTextField.setText("" + numSlices);
			numSlicesTextField.setColumns(10);
			numSlicesTextField.setName("numSlicesTextField");
		}
		return numSlicesTextField;
	}

	public ScopeSelectorPanel getScopeSelectorPanel() {
		if (scopeSelectorPanel == null) {
			scopeSelectorPanel = new ScopeSelectorPanel(probNet, preResolutionEvidence);
		}
		return scopeSelectorPanel;
	}

	@Override protected boolean doOkClickBeforeHide() {
		try {
			preResolutionEvidence.addFindings(scopeSelectorPanel.getSelectedFindings());
		} catch (InvalidStateException | IncompatibleEvidenceException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
					stringDatabase.getString("LoadEvidence.Error.IncompatibleEvidence"), JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			numSlices = Integer.parseInt(numSlicesTextField.getText());
			probNet.getInferenceOptions().getTemporalOptions().setHorizon(numSlices);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, stringDatabase.getString("NumberFormatException.Text.Label"),
					stringDatabase.getString("NumberFormatException.Title.Label"), JOptionPane.ERROR_MESSAGE);
			return false;
		}

        /*
        Window owner, Node node, EvidenceCase evidence,
												Variable decisionSelected, List<Finding> scenario
         */
		TraceTemporalEvolutionDialog dialog = new TraceTemporalEvolutionDialog(getOwner(), selectedNode,
				preResolutionEvidence, scopeSelectorPanel.getDecisionSelected());

		return super.doOkClickBeforeHide();
	}
}
