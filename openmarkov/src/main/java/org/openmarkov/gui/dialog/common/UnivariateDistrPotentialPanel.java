/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AugmentedTable;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.gui.component.AugmentedValuesTable;
import org.openmarkov.gui.component.AugmentedValuesTableModel;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;
import org.openmarkov.gui.component.ValuesTable;
import org.openmarkov.gui.component.ValuesTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 *
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialType = "UnivariateDistr") public class UnivariateDistrPotentialPanel
		extends TablePotentialPanel {

	protected List<Variable> pseudoVariablesDistribution = null;

	/**
	 * Variables of the AugmentedTablePotential.
	 * The attribute <code>variables</code> contains the variables of the AugmentedTable
	 */
	protected List<Variable> potentialVariables;

	/**
	 *
	 */
	protected String previouslySelectedDistribution = "";

	/**
	 * Constructor used by CPTablePanel
	 * This method creates, initialises, and displays a ValuesTable object for the first potential of the node
	 * <p>
	 * When there is no potential NullListPotentialException is showed--&gt;UNCLEAR stop???
	 *
	 * @param node : node whose first potential is a TablePotential or a TableDeltaPotential
	 * @author carmenyago : adaptation to TableDeltaPotential
	 */
	public UnivariateDistrPotentialPanel(Node node) {
		super();

		this.tablePotentialsPanelOperations = new PotentialsTablePanelOperations();

		// If there is no potential
		try {
			tablePotentialsPanelOperations.checkIfNoPotential(node.getPotentials());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.node = node;
		potential = node.getPotentials().get(0);
		//The list of variables of the UnivariateDistrPotential
		potentialVariables = potential.getVariables();
		tablePotential = ((UnivariateDistrPotential) potential).getDistributionTable();

		//The list of variables of the AugmentedTable of TablePotential
		variables = tablePotential.getVariables();

		pseudoVariablesDistribution = ((UnivariateDistrPotential) potential).getDistributionTable().getVariables();
		previouslySelectedDistribution = ((UnivariateDistrPotential) potential).getProbDensFunctionName();

		// Creating the table; class ValuesTable

		valuesTable = new AugmentedValuesTable(node, getTableModel(), (AugmentedTable) tablePotential, modifiable);
		valuesTable.setName("PotentialUnivariatePanel.valuesTable");
		valuesTable.setVisible(true);
		modifiable = true;

		// Previous--&gt;Ok
		setTableSpecificListeners();

		setData();

		setLayout(new BorderLayout());

		// If the ScrollPane is not created, initialise it and set the Viewport.
		// Then add the element to the Layout.
		add(getValuesTableScrollPane(), BorderLayout.CENTER);

		repaint();
	}

	/**
	 * This method returns the tableModel of valuesTable. If valuesTable has not a tableModel, this method creates one.
	 *
	 * @return the tableModel of valuesTable.
	 * @see ValuesTable
	 * revised--&gt;minor changes
	 */
	@Override protected ValuesTableModel getTableModel() {
		AugmentedValuesTableModel tableModel = null;
		if ((valuesTable == null) || (valuesTable.getTableModel() == null))
			tableModel = new AugmentedValuesTableModel(data, columns, firstEditableRow);
		else
			tableModel = (AugmentedValuesTableModel) valuesTable.getModel();

		return tableModel;
	}

	@Override public void setData() {

		// true
		hasLinkRestriction = LinkRestrictionPotentialOperations.hasLinkRestriction(node);
		// Sets the probNet in the table

		valuesTable.setData(node);

		Object[][] tableData = null;
		uncertaintyInColumns = null;
		String[] newColumns = null;

		// tableData contains the table to be displayed in ValuesTable
		// No override. Overriding some internal methos
		tableData = convertListPotentialsToTableFormat();

		// Sets the column names in Excel style: A, B, C,....AA,AB...
		// These column names aren't displayed
		newColumns = ValuesTable.getColumnsIdsSpreadSheetStyle(tableData[0].length);

		//Sets the table model in valuesTable
		//No override
		setDataInValuesTable(tableData, newColumns);

		// set the Cell Renders according to NodeType (a different renderer for some DECISON nodes) and the uncertainty
		setCellRenderers(uncertaintyInColumns);

		//this.getTableModel().setNotEditablePositions(getNotEditablePositions());

		// Establish the column width
		valuesTable.fitColumnsWidthToContent();
	}

	/**
	 * Sets a new table model with new data and new columns in valuesTable
	 *
	 * @param newData    new data for the table
	 * @param newColumns new columns for the table
	 * @author carmenyago
	 * Previously named setData; I find this name confusing because coincides with setData()
	 */
	@Override public void setDataInValuesTable(Object[][] newData, String[] newColumns) {

		super.setDataInValuesTable(newData, newColumns);
		// Sets the valuesTable tableModel with columns, data
		valuesTable.setModel(new AugmentedValuesTableModel(data, columns, firstEditableRow));

	}

	/**
	 * Creates and empty array of empty objects with the [number_of_rows][number_of_columns] of the valuesTable
	 * Considers the potential is not null
	 * Minor override only necessary for calculateFirstEditableRow,also removed tableDeltaPotential lines
	 *
	 * @author carmenyago
	 * <p>
	 * Continuous variables have only one state
	 * tableSize is always greater than 0
	 */
	@Override protected Object[][] createEmptyTable() {

		int numRows = 0;
		int numColumns = 1; // Variables column

		// First editable row coincides with the number of parents
		//CHANGE (minor node by tablePotential
		firstEditableRow = tablePotentialsPanelOperations.calculateFirstEditableRow(tablePotential);

		// The baseIndexForCoordinates is the first editable row--&gt;What for--&gt;UNCLEAR
		// The property baseIndexForCoordinates is not Visible. baseIndexForCoordinates= row
		setBaseIndexForCoordinates(firstEditableRow);

		// Number of data elements of tablePotential
		int tableSize = tablePotential
				.getTableSize();//--&gt;UNCLEAR What happens when there is no parent (f.e. when Tree/ADD )

		// Number of states of the variable of the node; if isTableDeltaPotential numDimensions=1
		int numDimensions = tablePotential.getDimensions()[0];
		// Parent variables + states of node variable
		numRows = firstEditableRow + numDimensions;
		lastEditableRow = numRows - 1;

		/*if (!isTableDeltaPotential) numRows++;*/ //--&gt; UNCLEAR Last row with the name of the variable and the state with '1' is REMOVED
		numColumns = numColumns + tableSize / numDimensions;

		// create the array of arrays
		return new Object[numRows][numColumns];

	}

	/**
	 * Sets the data table from potential in oldValues
	 *
	 * @param oldValues
	 * @return an array filled with the date table from tablePotential or tableDeltaPotential filled with the data values
	 * from tablePotential or tableDeltaPotential in the correct positions to be displayed by ValuesTable
	 */
	@Override protected Object[][] setPotentialDataInCentreArea(Object[][] oldValues) {
		Object[][] values = oldValues;

		int numColumns = values[0].length;

		// rounding initial values
		String[] initialValues = ((AugmentedTable) tablePotential).getFunctionValues();
		for (int j = 1; j <= numColumns - 1; j++) {

			// put the values on the table
			for (int i = getLastEditableRow(); i >= getFirstEditableRow(); i--) {
				int potentialIndex = tablePotentialsPanelOperations.getPotentialIndex(i, j, tablePotential);
				String value = initialValues[potentialIndex];
				values[i][j] = value;
			}
		}
		return values;
	}

	/**
	 * This method calculates the number of data cells and stores it in the attribute positions.
	 * The number of data cell is the product of the number of states of all variables
	 * <p>
	 * Override because we need the variables in tablePotential no in potential
	 */
	@Override protected int setNumberOfPostions() {
		int numPositions = 1;
		try {
			for (Variable variable : tablePotential.getVariables()) {
				numPositions = numPositions * variable.getNumStates();
			}
		} catch (NullPointerException exception) {
			numPositions = 0;
			logger.error("not enough memory");
		}
		setPosition(numPositions);
		return numPositions;
	}

	/**
	 * This method generates the evidenceCase based on the column selected on
	 * the <code>valuesTable</code> object.
	 * The evidence case has a finding for every parent of the node and its state in column
	 * <p>
	 * UNCLEAR When is the parents list reordered???
	 *
	 * @param col The column selected. Never is 0 , because the column 0 is the
	 *            states column
	 * @return An evidence case object
	 * @throws InvalidStateException
	 * @throws IncompatibleEvidenceException
	 * @author carmenyago
	 */
	@Override protected EvidenceCase getConfiguration(int col)
			throws InvalidStateException, IncompatibleEvidenceException {

		List<Variable> parents = variables.subList(1, potential.getNumVariables());

		EvidenceCase evidence = new EvidenceCase();

		int[] parentsConfiguration = new int[parents.size()];

		/*
		 * If there is no potential, an exception is shown (caught) and startPosition=0
		 */
		int startPosition = tablePotentialsPanelOperations.getPotentialStartIndexOfColumn(col, node);

		// gets the configuration of startPosition--&gt; the data position in tablePotential corresponding to
		// the beginning of the column
		// I suppose configuration=[Node Variable, parent_1,----,parent_n]
		int[] configuration = tablePotential.getConfiguration(startPosition);

		// Extracts the configuration of the parents from configuration
		// It is the same for every cell of the selected column

		for (int i = configuration.length - 1; i > 0; i--) {
			parentsConfiguration[i - 1] = configuration[i];
		}

		// Gets the evidence
		int j = 0;
		// Adds to evidence a finding containing the parent and its configuration
		Finding finding;
		for (Variable var : parents) {
			finding = new Finding(var, parentsConfiguration[j]);
			evidence.addFinding(finding);
			j++;
		}
		return evidence;
	}

	/**
	 * This method gets the Evidence Case from the selected column
	 *
	 * @return Evidence case
	 */
	public EvidenceCase getEvidenceCaseFromSelectedColumn() {
		EvidenceCase evi = null;
		try {
			evi = getConfiguration(selectedColumn);
		} catch (InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}
		return evi;
	}

	/**
	 * This method initialises valuesTable and defines that first two columns cannot be selected
	 *
	 * @return a new values table.
	 * revised--&gt;not changed
	 */
	public ValuesTable getValuesTable() {
		if (valuesTable == null) {
			valuesTable = new AugmentedValuesTable(node, getTableModel(), modifiable);
			valuesTable.setName("PotentialsTablePanel.valuesTable");
		}
		return valuesTable;
	}

	@Override protected void setTableSpecificListeners() {
		//	        valuesTable.addMouseListener(new java.awt.event.MouseAdapter() {
		//	            public void mouseClicked(java.awt.event.MouseEvent e) {
		//	                int row = valuesTable.rowAtPoint(e.getPoint());
		//	                int col = valuesTable.columnAtPoint(e.getPoint());
		//	                selectedColumn = col;
		//	                if ((SwingUtilities.isLeftMouseButton(e))&& (e.getClickCount()==1)) {
		//	                    valuesTable.editCellAt(
		//	                            valuesTable.rowAtPoint(e.getPoint()),
		//	                            valuesTable.columnAtPoint(e.getPoint()), e);
		//	                }
		//	                if ((SwingUtilities.isLeftMouseButton(e))&& (e.getClickCount()==2)) {
		//	                    System.err.println( "Entro" );
		//
		//                    }
		//	            }
		//
		//	        });
		valuesTable.addMouseListener(new MouseClickedListener());
	}

	/**
	 * Handles the double click in a cell
	 *
	 * @param e
	 */
	@Override protected void doubleClickEvent(MouseEvent e) {
		String function = null;
		
		List<Variable> parameterVariables = ((UnivariateDistrPotential) potential).getParameterVariables();
		int row = valuesTable.rowAtPoint(e.getPoint());
		int column = valuesTable.columnAtPoint(e.getPoint());
		function = (String) valuesTable.getValueAt(row, column);
		ArithmeticExpressionDialog expressionDialog = new ArithmeticExpressionDialog(null, parameterVariables,
				function);
		expressionDialog.setVisible(true);
		if (expressionDialog.getSelectedButton() == OkCancelHorizontalDialog.OK_BUTTON) {
			function = expressionDialog.getExpression();
			//int row = valuesTable.rowAtPoint(e.getPoint());
			//int column = valuesTable.columnAtPoint(e.getPoint());
			valuesTable.setValueAt(function, row, column);
		}
	}

	/**
	 * This class overrides the double click listener calling the
	 * @see org.openmarkov.gui.dialog.common.TablePotentialPanel.DoubleClickListener
	 * revised--&gt;not changed
	 */
	private class MouseClickedListener extends MouseAdapter {

		@Override public void mousePressed(MouseEvent e) {

			if (e.getClickCount() == 2) {
				doubleClickEvent(e);
			}
		}
	}

}
