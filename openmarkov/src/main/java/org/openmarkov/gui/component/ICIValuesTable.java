
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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.State;
import org.openmarkov.gui.action.ICITablePotentialValueEdit;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;
import java.util.ListIterator;

@SuppressWarnings("serial") public class ICIValuesTable extends ValuesTable implements PNUndoableEditListener {
	/**
	 * Define the last column of the table that was modified
	 */
	private int lastCol = -1;

	public ICIValuesTable(Node node, ValuesTableModel tableModel, final boolean modifiable) {
		super(node, tableModel, modifiable);
	}

	/**
	 * set the number of columns in the table for canonical models adding one
	 * column per parent state and adding one more for the id column (hidden)
	 *
	 * @param properties - node properties
	 * @return the number of columns in the table
	 */
	public static int howManyCanonicalColumns(Node properties) {
		int numColumns = 0;
		if (properties.getParents() != null) {
			int aux = 1;// first column for child states
			for (Node parent : properties.getParents()) {
				State[] parentStates = parent.getVariable().getStates();
				aux += parentStates.length;
			}
			numColumns = aux + 1; // last column for the leak potential
		} else {
			numColumns = 1;
		}
		// numColumns = FIRST_EDITABLE_COLUMN + numColumns;
		return numColumns;
	}

	public static int toPositionOnJtable(int index, int col, int numOfStates, int numOfParents) {
		return numOfParents - 1 + numOfStates + (numOfStates * (col - 1)) - index;
	}

	/**
	 * check the value to modify in the table and sets
	 */
	public void setValueAt(Object newValue, int row, int col) {
		Object oldValue = getValueAt(row, col);
		// TODO Verificar si la ubicaci??n del siguiente c??digo es adecuada
		if (((Double) newValue).isNaN()) {
			newValue = oldValue;
			JOptionPane.showMessageDialog(this.getParent(), "Introduced value is not a number");
		} else if (((Double) newValue) < 0) {
			newValue = oldValue;
			JOptionPane.showMessageDialog(this.getParent(), "Introduced value can not be negative");
		}
		if (!oldValue.equals(newValue)) {
			if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION) {
				if (lastCol != col) {
					priorityList.clear();
					lastCol = col;
				}
				ICITablePotentialValueEdit nodePotentialEdit = new ICITablePotentialValueEdit(node, (Double) newValue,
						row, col, priorityList);
				try {
					node.getProbNet().doEdit(nodePotentialEdit);
				} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, StringDatabase.getUniqueInstance().getString(e.getMessage()),
							StringDatabase.getUniqueInstance().getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
				}
			}
		} // else it is not required to update values
	}

	public void undoableEditHappened(UndoableEditEvent arg0) {
		int priorityListPosition = 0;
		UndoableEdit edit = arg0.getEdit();
		if (edit instanceof ICITablePotentialValueEdit) {
			ICITablePotentialValueEdit iciEdit = (ICITablePotentialValueEdit) arg0.getEdit();
			priorityList = iciEdit.getPriorityList();
			if (!iciEdit.getLeakyFlag()) {// noisy parameters
				double[] noisyPotential = iciEdit.getNewNoisyValues();
				ListIterator<Integer> listIterator = priorityList.listIterator();
				while (listIterator.hasNext() == true) {
					priorityListPosition = (Integer) listIterator.next();
					super.getModel().setValueAt(noisyPotential[priorityListPosition],
							iciEdit.getRowPosition(priorityListPosition), iciEdit.getColumnPosition());
				}
			} else {// leaky parametes
				double[] leakyPotential = iciEdit.getNewLeakyValues();
				ListIterator<Integer> listIterator = priorityList.listIterator();
				while (listIterator.hasNext() == true) {
					priorityListPosition = (Integer) listIterator.next();
					super.getModel().setValueAt(leakyPotential[priorityListPosition],
							iciEdit.getRowPosition(priorityListPosition), iciEdit.getColumnPosition());
				}
			}
		}
	}

	public void undoableEditWillHappen(UndoableEditEvent event)
			throws ConstraintViolationException {
		// TODO Auto-generated method stub
	}

	public void undoEditHappened(UndoableEditEvent event) {
		int priorityListPosition = 0;
		UndoableEdit edit = event.getEdit();
		if (edit instanceof ICITablePotentialValueEdit) {
			ICITablePotentialValueEdit iciEdit = (ICITablePotentialValueEdit) edit;
			priorityList = iciEdit.getPriorityList();
			if (!iciEdit.getLeakyFlag()) {// noisy parameters
				double[] lastNoisyPotential = iciEdit.getLastNoisyValues();
				ListIterator<Integer> listIterator = priorityList.listIterator();
				while (listIterator.hasNext() == true) {
					priorityListPosition = (Integer) listIterator.next();
					super.getModel().setValueAt(lastNoisyPotential[priorityListPosition],
							iciEdit.getRowPosition(priorityListPosition), iciEdit.getColumnPosition());
				}
			} else {// leaky parametes
				double[] lastLeakyPotential = iciEdit.getLastNoisyValues();
				ListIterator<Integer> listIterator = priorityList.listIterator();
				while (listIterator.hasNext() == true) {
					priorityListPosition = (Integer) listIterator.next();
					super.getModel().setValueAt(lastLeakyPotential[priorityListPosition],
							iciEdit.getRowPosition(priorityListPosition), iciEdit.getColumnPosition());
				}
			}
			super.getModel().setValueAt(iciEdit.getNewValue(), iciEdit.getRowPosition(), iciEdit.getColumnPosition());
		}
	}
}
