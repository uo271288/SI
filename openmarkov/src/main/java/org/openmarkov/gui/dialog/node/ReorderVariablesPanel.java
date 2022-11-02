
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AugmentedTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.gui.dialog.common.KeyTablePanel;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") public class ReorderVariablesPanel extends KeyTablePanel
		implements TableModelListener, PNUndoableEditListener {
	private Node node;
	private List<PNEdit> edits = new ArrayList<PNEdit>();

	public ReorderVariablesPanel(Node node) {
		super(new String[] { "Variable name" }, getData(node), true, false);
		initialize();
		getAddValueButton().setVisible(false);
		getRemoveValueButton().setVisible(false);
		this.node = node;
		// dataTable = newData;
		tableModel = new DefaultTableModel(data, columns);
		// valuesTable.setModel(tableModel);
		valuesTable.setModifiable(false);
		valuesTable.setModel(tableModel);
		tableModel.addTableModelListener(this);
		defineTableLookAndFeel();
		setData(data);
		// define specific listeners
		// defineTableSpecificListeners();
		// getTableModel().addTableModelListener(this);
	}

	/**
	 * Sets a new table model with new data.
	 *
	 * @param node The node to extract the data from.
	 */
	private static Object[][] getData(Node node) {
		Potential potential = node.getPotentials().get(0);
		List<Variable> variables = potential.getVariables();
		//CMI
		if (node.getPotentials().get(0) instanceof UnivariateDistrPotential) {
			variables = ((UnivariateDistrPotential) node.getPotentials().get(0)).getAugmentedTable().getVariables();
		} else if (node.getPotentials().get(0) instanceof AugmentedTablePotential) {
			variables = ((AugmentedTablePotential) node.getPotentials().get(0)).getAugmentedTable().getVariables();
		}
		//CMF
		// 26/11/2014
		// Added node.hasPolicy() to the condition of the if clause when allowing to reorder variables
		// when imposing a policy in a decision node
		if (potential.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY || node.hasPolicy()) {
			variables.remove(0);
		}
		Object[][] data = new Object[variables.size()][1];
		for (int i = 0; i < variables.size(); i++) {
			//data[variables.size () - i - 1][0] = variables.get (i).getName ();
			data[i][0] = variables.get(i).getName();
		}
		return data;
	}

	protected void defineTableLookAndFeel() {
		// center the data in all columns
		DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
		tcr.setHorizontalAlignment(SwingConstants.LEFT);
		DefaultTableCellRenderer statesRender = new DefaultTableCellRenderer();
		statesRender.setHorizontalAlignment(SwingConstants.LEFT);
		int maxColumn = valuesTable.getColumnModel().getColumnCount();
		for (int i = 1; i < maxColumn; i++) {
			TableColumn aColumn = valuesTable.getColumnModel().getColumn(i);
			aColumn.setCellRenderer(tcr);
			valuesTable.getTableHeader().getColumnModel().getColumn(i).setCellRenderer(tcr);
		}
	}

	@Override protected void actionPerformedUpValue() {
		int selectedRow = valuesTable.getSelectedRow();
		Object swap = data[selectedRow][0];
		data[selectedRow][0] = data[selectedRow - 1][0];
		data[selectedRow - 1][0] = swap;
		setData(data);
		valuesTable.getSelectionModel().setSelectionInterval(selectedRow - 1, selectedRow - 1);
	}

	@Override protected void actionPerformedDownValue() {
		int selectedRow = valuesTable.getSelectedRow();
		Object swap = data[selectedRow][0];
		data[selectedRow][0] = data[selectedRow + 1][0];
		data[selectedRow + 1][0] = swap;
		setData(data);
		valuesTable.getSelectionModel().setSelectionInterval(selectedRow + 1, selectedRow + 1);
	}

	public List<Variable> getVariables() {
		Potential potential = node.getPotentials().get(0);
		List<Variable> potentialVariables = potential.getVariables();
		List<Variable> newVariables = new ArrayList<Variable>();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < potentialVariables.size(); j++) {
				if (((String) data[i][0]).equals(potentialVariables.get(j).getName())) {
					newVariables.add(potentialVariables.get(j));
				}
			}
		}
		//Collections.reverse(newVariables);
		// 26/11/2014
		// Added node.hasPolicy() to the condition of the if clause when allowing to reorder variables
		// when imposing a policy in a decision node
		if (potential.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY || node.hasPolicy()) {
			newVariables.add(0, potential.getVariables().get(0));
		}
		return newVariables;
	}

	/**
	 * @return The lisf of edits
	 */
	public List<PNEdit> getEdits() {
		return edits;
	}

	@Override public void undoableEditHappened(UndoableEditEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override public void undoableEditWillHappen(UndoableEditEvent event)
			throws ConstraintViolationException, NonProjectablePotentialException,
			WrongCriterionException {
		// TODO Auto-generated method stub
	}

	@Override public void undoEditHappened(UndoableEditEvent event) {
		// TODO Auto-generated method stub
	}

	@Override public void tableChanged(TableModelEvent arg0) {
		// TODO Auto-generated method stub
	}
}
