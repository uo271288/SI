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
import org.openmarkov.core.model.network.potential.AugmentedTable;
import org.openmarkov.core.model.network.potential.AugmentedTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.gui.action.AugmentedPotentialValueEdit;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * This table implementation is responsible for the graphical and data model
 * manipulation of the Node Potentials (either in a general family or in a
 * canonical family potential). This table also shows the data in several ways,
 * depending upon the type of user selection:
 * <ul>
 * <li>Probabilities or states values</li>
 * <li>Probabilistic or Deterministic values allowed</li>
 * <li>All parameters or Only independent parameters</li>
 * <li>TPC or canonical parameters(for the Canonical families)</li>
 * <li>Net or Compound values (for the Canonical families)</li>
 * </ul>
 *
 * @author carmenyago
 * @version 1 Apr/2017
 */
public class AugmentedValuesTable extends ValuesTable implements PNUndoableEditListener {
	/**
	 * default serial ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 *
	 * @param node       - the node with the TablePotential or ExactDistrPotential
	 * @param tableModel - the model of the TablePotential or ExactDistrPotential
	 * @param modifiable - true if the table can be edited and modified
	 */
	public AugmentedValuesTable(Node node, ValuesTableModel tableModel, final boolean modifiable) {
		super(node, tableModel, modifiable);
		this.tablePotential = ((AugmentedTablePotential) potential).getAugmentedTable();
	}

	/**
	 * @param node
	 * @param tableModel
	 * @param augmentedTable
	 * @param modifiable
	 */
	public AugmentedValuesTable(Node node, ValuesTableModel tableModel, AugmentedTable augmentedTable,
			final boolean modifiable) {
		super(node, tableModel, modifiable);
		this.tablePotential = augmentedTable;
	}

	/**
	 * Constructor for ValuesTable
	 * revised--&gt;not changed
	 */
	public AugmentedValuesTable(ValuesTableModel tableModel, final boolean modifiable) {
		super(tableModel, modifiable);

	}

	/**
	 * Default display configuration for this table
	 */
	@Override protected void defaultConfiguration() {
		super.defaultConfiguration();
		//CMI
		//CHANGED
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		//CMF
	}

	/**
	 * check the value to modify in the table and sets
	 */
	@Override public void setValueAt(Object newValue, int row, int col) {

		Object oldValue = getValueAt(row, col);

		String oldValueString = (String) oldValue;
		String newValueString = (String) newValue;
		if (oldValueString.equals(newValueString))
			return;

		AugmentedPotentialValueEdit nodePotentialEdit = new AugmentedPotentialValueEdit(node, newValueString, row, col,
				priorityList, getTableModel().getNotEditablePositions());
		try {
			probNet.doEdit(nodePotentialEdit);
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if newValue is a String or a Double
	 *
	 * @param newValue - new value to validate
	 * @author carmenyago
	 */
	protected boolean castValue(Object newValue) {

		if (newValue instanceof String)
			try {
				Double.parseDouble((String) newValue);
				return true;
			} catch (Exception ex) {
				return false;
			}
		if (newValue instanceof Double)
			return true;
		return false;
	}

	/**
	 * print the NodePotentialTable
	 * carmenyago only removed the println of the deterministic attribute
	 *
	 * @author carmenyago revised--&gt;minor changes
	 */
	@Override public void printTable() {
		System.out.println("NodePotentialTable: ");
		if (getVariable() != null) {
			System.out.println("    variable = " + getVariable().getName());
		} else {
			System.out.println("    variable = not defined yet");
		}
		if (tableModel != null) {
			System.out.println("    tableModel.firstEditableRow = " + tableModel.getFirstEditableRow());
			System.out.println("    tableModel.rowCount = " + tableModel.getRowCount());
			System.out.println("    tableModel.columnCount = " + tableModel.getColumnCount());
		} else {
			System.out.println("    tableModel.firstEditableRow = not tableModel yet");
		}
		System.out.println("    lastEditableRow = " + lastEditableRow);
		System.out.println("    usingGeneralPotencial = " + isUsingGeneralPotential());
		System.out.println("    showingAllParameters = " + isShowingAllParameters());
		System.out.println("    showingProbabilitiesValues = " + isShowingProbabilitiesValues());
		System.out.println("    showingTPCvalues = " + isShowingTPCvalues());
	}

	/**
	 * Updates the edited column
	 */
	@Override public void undoableEditHappened(UndoableEditEvent event) {
		UndoableEdit edit = event.getEdit();
		if (edit instanceof AugmentedPotentialValueEdit) {
			augmentedPotentialValueEditHappened((AugmentedPotentialValueEdit) edit);
		}
	}

	/**
	 * @param edit
	 */
	public void augmentedPotentialValueEditHappened(AugmentedPotentialValueEdit edit) {
		int position = edit.getIndexSelected();
		Potential editPotential = edit.getPotential();
		AugmentedTable editTable;
		if (editPotential instanceof AugmentedTablePotential) {
			editTable = ((AugmentedTablePotential) editPotential).getAugmentedTable();

		} else {
			editTable = ((UnivariateDistrPotential) editPotential).getAugmentedTable();
		}
		String[] functionValues = editTable.getFunctionValues();
		if (position >= 0) {
			int rowPosition = edit.getRowPosition(position);
			int columnPosition = edit.getColumnPosition();
			if (editPotential instanceof AugmentedTablePotential) {
				for (int i = lastEditableRow; i <= lastEditableRow; i++) {
					super.getModel().setValueAt("Complement", i, columnPosition);
				}
			}

			String a = functionValues[position];
			super.getModel().setValueAt(a, rowPosition, columnPosition);
		}
	}

	/**
	 *
	 */
	public void undoableEditWillHappen(UndoableEditEvent event)
			throws ConstraintViolationException {
		// Ignore
	}

	/**
	 * UNCLEAR--&gt;Priority list
	 */
	@Override public void undoEditHappened(UndoableEditEvent event) {
		//if (event.getEdit () instanceof AugmentedPotentialValueEdit)
		try {
			AugmentedPotentialValueEdit edit = (AugmentedPotentialValueEdit) event.getEdit();
			Potential editPotential = edit.getPotential();
			AugmentedTable editTable;
			if (editPotential instanceof AugmentedTablePotential) {
				editTable = ((AugmentedTablePotential) editPotential).getAugmentedTable();
			} else {
				editTable = ((UnivariateDistrPotential) editPotential).getAugmentedTable();
			}
			super.getModel().setValueAt(editTable.getFunctionValues()[edit.getIndexSelected()], edit.getRowPosition(),
					edit.getColumnPosition());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method edits the cell at row, column.
	 * If isSelectAllForMouseEvent,isSelectAllForActionEvent, or isSelectAllForKeyEvent the entire cell is selected
	 * UNCLEAR isSelectAllForMouseEvent,isSelectAllForActionEvent, or isSelectAllForKeyEvent values never change
	 * Overrided to provide Select All editing functionality
	 *
	 * @param row    - the row of the edited cell
	 * @param column - the column of the edited cell
	 * @param e      - event to pass into shouldSelectCell;
	 *               revised--&gt; not changed
	 */
	@Override public boolean editCellAt(int row, int column, EventObject e) {
		boolean result = super.editCellAt(row, column, e);

		if (e instanceof MouseEvent)
			System.err.println("CLICK COUNT" + ((MouseEvent) e).getClickCount());

		return result;

	}

	/**
	 * If the editor that is handling the editing session is not a JTextComponent, the method does nothing
	 * If the editor is a JTextComponent then:
	 * If e is and instance of KeyEvent, ActionEvent or MouseEvent, the method select all the text of the cell
	 * @param e event which provoked the edition and selection
	 *           revised --&gt; not changed
	 */
	private void selectAll(EventObject e) {
		// Returns the component that is handling the editing session.
		final Component editor = getEditorComponent();
		if (editor == null || !(editor instanceof JTextComponent))
			return;
		if (e == null) {
			((JTextComponent) editor).selectAll();
			return;
		}
		// Typing in the cell was used to activate the editor
		if (e instanceof KeyEvent && isSelectAllForKeyEvent) {
			((JTextComponent) editor).selectAll();
			return;
		}
		// F2 was used to activate the editor
		if (e instanceof ActionEvent && isSelectAllForActionEvent) {
			((JTextComponent) editor).selectAll();
			return;
		}
		// A mouse click was used to activate the editor.
		// Generally this is a double click and the second mouse click is
		// passed to the editor which would remove the text selection unless
		// we use the invokeLater()
		if (e instanceof MouseEvent && isSelectAllForMouseEvent) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((JTextComponent) editor).selectAll();
				}
			});
		}
	}

}