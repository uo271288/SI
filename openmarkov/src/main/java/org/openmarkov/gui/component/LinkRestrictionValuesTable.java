
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.gui.action.LinkRestrictionPotentialValueEdit;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;
import java.util.ArrayList;

/**
 * This table implementation is responsible for the graphical and data model
 * manipulation of the Link restriction potential.
 **/
@SuppressWarnings("serial") public class LinkRestrictionValuesTable extends ValuesTable
		implements PNUndoableEditListener {
	/***
	 * Constant value to describe compatibility of a position of the link
	 * restriction potential.
	 */
	private final String COMPATIBILITY_VALUE = "1";
	/***
	 * Constant value to describe incompatibility of a position of the link
	 * restriction potential.
	 */
	private final String INCOMPATIBILITY_VALUE = "0";
	/****
	 * The link with the link restriction.
	 **/
	private Link<Node> link;
	/****
	 * The parent node of the link
	 */
	private Node node1;
	/****
	 * The child node of the link
	 */
	private Node node2;
	/***
	 * The ProbNet containing the link.
	 */
	private ProbNet net;

	public LinkRestrictionValuesTable(Link<Node> link, ValuesTableModel tableModel, final boolean modifiable) {
		super(tableModel, modifiable);
		this.link = link;
		node1 = link.getNode1();
		node2 = link.getNode2();
		net = node1.getProbNet();
	}

	/**
	 * This method checks the value to modify in the table and sets the new
	 * value.
	 ***/
	public void setValueAt(Object newValue, int row, int col) {
		if (newValue != null) {
			Integer newNumericValue;
			try {
				newNumericValue = (Integer) newValue;
				if (!newNumericValue.equals(Integer.valueOf(INCOMPATIBILITY_VALUE)) && !newNumericValue
						.equals(Integer.valueOf(COMPATIBILITY_VALUE))) {
					newValue = Integer.parseInt(INCOMPATIBILITY_VALUE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, StringDatabase.getUniqueInstance().getString(e.getMessage()),
						StringDatabase.getUniqueInstance().getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
			LinkRestrictionPotentialValueEdit linkPotentialEdit = new LinkRestrictionPotentialValueEdit(link,
					(Integer) newValue, row, col);
			try {
				net.doEdit(linkPotentialEdit);
				super.getModel().setValueAt(newValue, row, col);
				int variable1Index = col - 1;
				int variable2Index = node2.getVariable().getNumStates() - row;
				if ((Integer) newValue == 0) {
					if (!node2.getPotentials().isEmpty() && node2.getPotentials().get(0) instanceof TablePotential) {
						Potential potential = LinkRestrictionPotentialOperations
								.updatePotentialByAddLinkRestriction(node2,
										(TablePotential) link.getRestrictionsPotential(), variable1Index,
										variable2Index);
						ArrayList<Potential> potentials = new ArrayList<Potential>();
						potentials.add(potential);
						node2.setPotentials(potentials);
					}
				}
			} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, StringDatabase.getUniqueInstance().getString(e.getMessage()),
						StringDatabase.getUniqueInstance().getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void undoableEditHappened(UndoableEditEvent event) {
		UndoableEdit unEdit = event.getEdit();
		if (unEdit instanceof LinkRestrictionPotentialValueEdit) {
			if (event.getEdit() instanceof LinkRestrictionPotentialValueEdit) {
				LinkRestrictionPotentialValueEdit edit = (LinkRestrictionPotentialValueEdit) event.getEdit();
				super.getModel().setValueAt(edit.getNewValue(), edit.getRowPosition(), edit.getColumnPosition());
			}
		}
	}

	public void undoEditHappened(UndoableEditEvent event) {
		if (event.getEdit() instanceof LinkRestrictionPotentialValueEdit) {
			LinkRestrictionPotentialValueEdit edit = (LinkRestrictionPotentialValueEdit) event.getEdit();
			super.getModel().setValueAt(edit.getNewValue(), edit.getRowPosition(), edit.getColumnPosition());
		}
	}
}
