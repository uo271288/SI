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
import org.openmarkov.core.model.network.potential.DiscretizedCauchyPotential;
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

@SuppressWarnings("serial") @PotentialPanelPlugin(potentialType = "Discretized Cauchy") public class DiscretizedCauchyPotentialPanel
		extends PotentialPanel implements PNUndoableEditListener {

	private JButton editMedianButton;
	private JButton editScaleButton;
	private ProbNet probNet;
	private Node medianDummyNode = null;
	private Node scaleDummyNode = null;
	private Potential oldPotential;
	private DiscretizedCauchyPotential newPotential;

	public DiscretizedCauchyPotentialPanel(Node node) {
		super();
		initComponents();
		this.probNet = node.getProbNet();
		this.oldPotential = node.getPotentials().get(0);
		this.newPotential = (DiscretizedCauchyPotential) oldPotential.copy();
		setData(node);
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		editMedianButton = new JButton("Edit median potential");
		editMedianButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editMedianPotential();
			}
		});
		editScaleButton = new JButton("Edit scale potential");
		editScaleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editScalePotential();
			}
		});
		buttonPanel.add(editMedianButton);
		buttonPanel.add(editScaleButton);
		add(buttonPanel, BorderLayout.PAGE_START);
	}

	private void editMedianPotential() {
		PotentialEditDialog potentialEditDialog = new PotentialEditDialog(Utilities.getOwner(this), medianDummyNode,
				false, isReadOnly());
		if (potentialEditDialog.requestValues() == NodePropertiesDialog.OK_BUTTON) {
			// Do nothing?
		} else {
			medianDummyNode.getProbNet().getPNESupport().undoAndDelete();
		}
	}

	private void editScalePotential() {
		PotentialEditDialog potentialEditDialog = new PotentialEditDialog(Utilities.getOwner(this), scaleDummyNode,
				false, isReadOnly());
		if (potentialEditDialog.requestValues() == NodePropertiesDialog.OK_BUTTON) {
			// Do nothing?
		} else {
			scaleDummyNode.getProbNet().getPNESupport().undoAndDelete();
		}
	}

	@Override public void setData(Node node) {
		ProbNet medianDummyNet = new ProbNet(probNet.getNetworkType());
		medianDummyNode = medianDummyNet.addPotential(newPotential.getMedian());
		medianDummyNet.getPNESupport().addUndoableEditListener(this);

		ProbNet scaleDummyNet = new ProbNet(probNet.getNetworkType());
		scaleDummyNode = scaleDummyNet.addPotential(newPotential.getScale());
		scaleDummyNet.getPNESupport().addUndoableEditListener(this);
	}

	@Override public void close() {
		medianDummyNode.getProbNet().getPNESupport().removeUndoableEditListener(this);
		scaleDummyNode.getProbNet().getPNESupport().removeUndoableEditListener(this);
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
			newPotential.setMedian(medianDummyNode.getPotentials().get(0));
			newPotential.setScale(scaleDummyNode.getPotentials().get(0));

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