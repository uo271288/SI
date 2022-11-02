/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.action.PotentialChangeEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.ConditionalGaussianPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.dialog.node.NodePropertiesDialog;
import org.openmarkov.gui.dialog.node.PotentialEditDialog;
import org.openmarkov.gui.util.Utilities;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial") @PotentialPanelPlugin(potentialType = "Conditional Gaussian") public class ConditionalGaussianPotentialPanel
		extends PotentialPanel implements PNUndoableEditListener {

	private JButton editMeanButton;
	private JButton editVarianceButton;
	private ProbNet probNet;
	private Node meanDummyNode = null;
	private Node varianceDummyNode = null;
	private Potential oldPotential;
	private ConditionalGaussianPotential newPotential;

	public ConditionalGaussianPotentialPanel(Node node) {
		super();
		initComponents();
		this.probNet = node.getProbNet();
		this.oldPotential = node.getPotentials().get(0);
		this.newPotential = (ConditionalGaussianPotential) oldPotential.copy();
		setData(node);
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		editMeanButton = new JButton("Edit mean potential");
		editMeanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editMeanPotential();
			}
		});
		editVarianceButton = new JButton("Edit variance potential");
		editVarianceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editVariancePotential();
			}
		});
		buttonPanel.add(editMeanButton);
		buttonPanel.add(editVarianceButton);
		add(buttonPanel, BorderLayout.PAGE_START);
	}

	private void editMeanPotential() {
		PotentialEditDialog potentialEditDialog = new PotentialEditDialog(Utilities.getOwner(this), meanDummyNode,
				false, isReadOnly());
		if (potentialEditDialog.requestValues() == NodePropertiesDialog.OK_BUTTON) {
			// Do nothing?
		} else {
			meanDummyNode.getProbNet().getPNESupport().undoAndDelete();
		}
	}

	private void editVariancePotential() {
		PotentialEditDialog potentialEditDialog = new PotentialEditDialog(Utilities.getOwner(this), varianceDummyNode,
				false, isReadOnly());
		if (potentialEditDialog.requestValues() == NodePropertiesDialog.OK_BUTTON) {
			// Do nothing?
		} else {
			varianceDummyNode.getProbNet().getPNESupport().undoAndDelete();
		}
	}

	@Override public void setData(Node node) {
		ProbNet meanDummyNet = new ProbNet(probNet.getNetworkType());
		meanDummyNode = meanDummyNet.addPotential(newPotential.getMean());
		meanDummyNet.getPNESupport().addUndoableEditListener(this);

		ProbNet varianceDummyNet = new ProbNet(probNet.getNetworkType());
		varianceDummyNode = varianceDummyNet.addPotential(newPotential.getVariance());
		varianceDummyNet.getPNESupport().addUndoableEditListener(this);
	}

	@Override public void close() {
		meanDummyNode.getProbNet().getPNESupport().removeUndoableEditListener(this);
		varianceDummyNode.getProbNet().getPNESupport().removeUndoableEditListener(this);
	}

	@Override public boolean saveChanges() {
		boolean result = super.saveChanges();

		newPotential.setComment(oldPotential.getComment());
		PotentialChangeEdit edit = new PotentialChangeEdit(probNet, oldPotential, newPotential);
		try {
			probNet.doEdit(edit);
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}

		return result;
	}

	private void update() {
		try {
			TablePotential projectedPotential = newPotential.tableProject(new EvidenceCase(), null).get(0);
			// TODO update table with projected potential
		} catch (NonProjectablePotentialException | WrongCriterionException e) {
			e.printStackTrace();
		}
	}

	@Override public void undoableEditHappened(UndoableEditEvent event) {
		// Update new potential and potential panel
		if (event.getEdit() instanceof PotentialChangeEdit) {
			newPotential.setMean(meanDummyNode.getPotentials().get(0));
			newPotential.setVariance(varianceDummyNode.getPotentials().get(0));

			//update();
		}
	}

	@Override public void undoableEditWillHappen(UndoableEditEvent event)
			throws ConstraintViolationException, NonProjectablePotentialException,
			WrongCriterionException {
		// Ignore
	}

	@Override public void undoEditHappened(UndoableEditEvent event) {
		// Ignore
	}
}
