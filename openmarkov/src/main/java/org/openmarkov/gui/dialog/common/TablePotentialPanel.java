/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.apache.logging.log4j.Logger;
import org.openmarkov.core.action.UncertainValuesEdit;
import org.openmarkov.core.action.UncertainValuesRemoveEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PolicyType;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;
import org.openmarkov.gui.component.ValuesTable;
import org.openmarkov.gui.component.ValuesTableCellRenderer;
import org.openmarkov.gui.component.ValuesTableModel;
import org.openmarkov.gui.component.ValuesTableOptimalPolicyCellRenderer;
import org.openmarkov.gui.component.ValuesTableWithLinkRestrictionCellRenderer;
import org.openmarkov.gui.dialog.node.UncertainValuesDialog;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.menu.UncertaintyContextualMenu;
import org.openmarkov.gui.util.Utilities;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

/**
 * This class implements a Table potential table with the following features:
 * <ul>
 * <li>Its elements, except the first column, are modifiable.
	s* <li>New
 * elements can be added, creating a new key row with empty data.
	s* <li>The
 * key data (first column) consist of a key string following of the index of the
 * row and it is used for internal purposes only.
	s* <li>The key data is
 * hidden.
	s* <li>The information of a row (except the first column) can not
 * be taken up or down.
	s* <li>The rows can not be removed.
	s* <li>The first
 * editable row is the one that has the values of the potentials.
	s* <li>The
 * rows between 0 and the first editable row are ocuppied by the values of the
 * states of the parents of the variable.
	s* <li>The header of columns is
 * hidden.</li>
 * </ul>
 * <p>
 * carmenyago: Changes: 1. adaptation to the new definition of utility node, 2. removing deterministic features
 * 3. when the potential doesn't exit an exception is raised
 *
 * @author jlgozalo
 * @author myebra
 * @author carmenyago 19/06/2016
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialType = "Table") public class TablePotentialPanel
		extends ProbabilityTablePanel {
	protected Logger logger;
	/**
	 * JTable where show the values.
	 */
	protected ValuesTable valuesTable = null;
	/**
	 * Indicates if the data of the table is modifiable.
	 */
	protected boolean modifiable;
	/**
	 * Panel to scroll the table.
	 */
	protected JScrollPane valuesTableScrollPane = null;
	protected Node node;

	/**
	 * First potential of node;  its class  should be  org.openmarkov.core.model.network.potential.TablePotential or
	 * org.openmarkov.core.model.network.potential.TableDeltaPotential
	 *
	 * @author carmenyago
	 */
	protected Potential potential = null;

	/**
	 * When potential is an instance of TablePotential, tablePotential is potential casted as TablePotential
	 * When potential is an instance of TableDeltaPotential, tablePotential=(TablePotential)potential.getTablePotential()
	 *
	 * @author carmenyago
	 */

	protected TablePotential tablePotential = null;

	/**
	 * True if class of zeroPotential is org.openmarkov.core.model.network.potential.TableDeltaPotential
	 *
	 * @author carmenyago
	 */
	protected boolean isExactDistrPotential = false;

	/**
	 * True if some parent has a link restriction to the node
	 *
	 * @author carmenyago
	 */
	protected boolean hasLinkRestriction;

	/**
	 * UNCLEAR--&gt;We calculate the uncertainty. This is calculated several times; i have to check if calculations are repeated unnecessarily
	 */
	protected boolean[] uncertaintyInColumns;

	/**
	 * Pseudo-util class with common operations used in potential tables
	 */
	protected PotentialsTablePanelOperations tablePotentialsPanelOperations;

	/**
	 * ContextualMenu to assign/remove uncertainty.
	 * <p>
	 * This method creates the evidenceCase object when the user do right click on the table.
	 */

	protected UncertaintyContextualMenu uncertaintyContextualMenu;

	public TablePotentialPanel() {
		super();
	}

	/**
	 * Constructor used by CPTablePanel
	 * This method creates, initialises, and displays a ValuesTable object for the first potential of the node
	 * <p>
	 * When there is no potential NullListPotentialException is showed--&gt;UNCLEAR stop???
	 * <p>
	 * <p>
	 * <p>
	 * If it is not TableDeltaPotential or TablePotential it cast to TablePotential
	 *
	 * @param node : node whose first potential is a TablePotential or a TableDeltaPotential
	 * @author carmenyago : adaptation to TableDeltaPotential
	 */
	public TablePotentialPanel(Node node) {
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
		// This panel displays the first potential of the node
		potential = node.getPotentials().get(0);
		if (potential instanceof ExactDistrPotential) {
			isExactDistrPotential = true;
			tablePotential = ((ExactDistrPotential) potential).getTablePotential();
		} else
			tablePotential = (TablePotential) potential;

		// The list of variables of potential
		variables = potential.getVariables();

		// Creating the table; class ValuesTable
		valuesTable = new ValuesTable(node, getTableModel(), modifiable);
		valuesTable.setName("PotentialsTablePanel.valuesTable");
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
	 * Sets a new table model with new data and new columns in valuesTable
	 *
	 * @param newData    new data for the table
	 * @param newColumns new columns for the table
	 * @author carmenyago
	 * revised--&gt; minor changes
	 * Previously named setData; I find this name confusing because coincides with setData()
	 */
	public void setDataInValuesTable(Object[][] newData, String[] newColumns) {

		// Table data
		data = newData.clone();
		// Table columns
		columns = newColumns.clone();

		// resets the tableModel
		valuesTable.resetModel();

		// Sets the valuesTable tableModel with columns, data
		valuesTable.setModel(new ValuesTableModel(data, columns, firstEditableRow));

		// Initialises a false an array which tells which data are modified
		valuesTable.initializeDataModified(false);

		valuesTable.setLastEditableRow(lastEditableRow);

		//show/hide rows based on the showingAllParameters attribute using a RowFilter mechanism.
		valuesTable.setShowingAllParameters(true);

		valuesTable.setNodeType(node.getNodeType());
	}

	/**
	 * It is necessary to implement setData(Node node)
	 * Here I deal with potential = null or potential =0;
	 * <p>
	 * UNCLEAR--&gt; Called in PotentialEditDialog.showFields(Node)
	 *
	 * @author carmenyago
	 */
	public void setData(Node node) {
		this.node = node;

		try {
			tablePotentialsPanelOperations.checkIfNoPotential(node.getPotentials());

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			return;
		}
		setData();
	}

	/**
	 * Sets a new table model with new data and new columns based on three
	 * items:
	 * <ul>
	 *     <li>list of Potentials of the variable
	 *     <li>states of the variable
	 *     <li>parents of the variable
	 * </ul>
	 * This method obtains if the node has link restrictions and store it in hasLinkrestriction,
	 * stores the probNet in ValuesTable
	 * fills the tableData (tableData consists of headers + data),
	 * sets the columns name in a Excel mode (A,B,C,...AA,AB...),
	 * sets the tableModel in valuesTable( tableData + column names),
	 * sets uncertaintyInColumns with the columns with uncertainty,
	 * sets the cell renders according to the type of node, and
	 * in the tableMoel, sets the not editable cells due to links restrictions and uncertainty in columns.
	 * Finally, this method adjust the size of the cells in valuesTable
	 *
	 * @author carmenyago
	 */
	// Using node sets in variable node
	// What to do with the exception
	public void setData() {

		// true 
		hasLinkRestriction = LinkRestrictionPotentialOperations.hasLinkRestriction(node);
		// Sets the probNet in the table

		valuesTable.setData(node);

		Object[][] tableData = null;
		uncertaintyInColumns = null;
		String[] newColumns = null;

		// tableData contains the table to be displayed in ValuesTable
		tableData = convertListPotentialsToTableFormat();

		// Sets the column names in Excel style: A, B, C,....AA,AB...
		// These column names aren't displayed
		newColumns = ValuesTable.getColumnsIdsSpreadSheetStyle(tableData[0].length);

		//Calculated in convertListPotentialsToTableFormat--&gt;createEmptyTable()
		//setFirstEditableRow(tablePotentialsPanelOperations.calculateFirstEditableRow(node));
		//setLastEditableRow(tablePotentialsPanelOperations.calculateLastEditableRow(node));

		//Sets the table model in valuesTable
		setDataInValuesTable(tableData, newColumns);

		//uncertaintyInColums indicates the data columns which have uncertainty
		uncertaintyInColumns = getUncertaintyInColumns();

		// set the Cell Renders according to NodeType (a different renderer for some DECISON nodes) and the uncertainty
		setCellRenderers(uncertaintyInColumns);

		// getNotEditablePositions checked if there is any link restriction 
		// which make the correspondent cells no editable and returns an array with the size of the table
		// with the not editable cells set to 1 
		this.getTableModel().setNotEditablePositions(getNotEditablePositions());

		// Establish the column width
		valuesTable.fitColumnsWidthToContent();
	}

	/**
	 * Sets the columns that have uncertainty a true in a boolean array
	 * To do that, this method extracts the uncertainty for every column configuration (parents state set)
	 * <p>
	 * UNCLEAR--&gt;When we reach this method potential!=null
	 *
	 * @return Boolean array that represents the columns (true = the column has
	 * an uncertainty, false = the column has not an uncertainty). This array only contains the data columns
	 * @author carmenyago
	 */
	protected boolean[] getUncertaintyInColumns() {

		int size = valuesTable.getColumnCount();

		// Column 0 contains the name of the states
		boolean[] newUncertaintyInColumns = new boolean[size - 1];

		for (int i = 1; i < size; i++) {
			boolean hasUncertainty = false;
			try {
				// Returns an evidence case with one finding for every parent variable and its state in the column
				EvidenceCase configuration = getConfiguration(i);
				// If the column configuration has uncertainty hasUncertainty= true
				hasUncertainty = tablePotential.hasUncertainty(configuration);
			} catch (InvalidStateException | IncompatibleEvidenceException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
						stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
			// Indicates whether this column has uncertainty or not
			newUncertaintyInColumns[i - 1] = hasUncertainty;
		}
		return newUncertaintyInColumns;
	}

	/**
	 * calculate the number of rows of the table based on the parents and  states of the node variable
	 * Last row with the name of the variable when TablePotential REMOVED
	 *
	 * @author carmenyago
	 */
	protected int howManyRows(Node n) {
		return n.getParents().size() + n.getVariable().getStates().length;
	}

	/**
	 * Creates an array[number_of_rows][number_of_columns] with the objects displayed in the cells of valueTable
	 * Considers the potential is not null
	 *
	 * @return the table data to be set
	 * @author carmenyago
	 */
	protected Object[][] convertListPotentialsToTableFormat() {
		Object[][] values = null;

		// Empty array values[number_of_rows][number_of_colums]
		values = createEmptyTable();

		// Sets the number of the parent variables
		values = setParentsNameInUpperLeftCornerArea(values);

		// Set the states of the parents on  the top of the table
		// UNCLEAR--&gt; what happens when the parent variable is continuous????
		values = setParentsStatesInTopArea(values);
		// Set the states of the node variable on the left column  
		values = setNodeStatesInLeftArea(values);

		// Set the TablePotential/TableDeltaPotential Data on values
		values = setPotentialDataInCentreArea(values);

		// The variable position stores the number o data cells
		setNumberOfPostions();
		return values;
	}

	/**
	 * Creates and empty array of empty objects with the [number_of_rows][number_of_columns] of the valuesTable
	 * Considers the potential is not null
	 * UNCLEAR --&gt; setBaseIndexForCoordinates
	 *
	 * @author carmenyago
	 * <p>
	 * Continuous variables have only one state
	 * tableSize is always greater than 0
	 */
	protected Object[][] createEmptyTable() {

		// If there is no potential
		try {
			tablePotentialsPanelOperations.checkIfNoPotential(node.getPotentials());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		int numRows = 0;
		int numColumns = 1; // Variables column

		// First editable row coincides with the number of parents
		firstEditableRow = tablePotentialsPanelOperations.calculateFirstEditableRow(node);

		// The baseIndexForCoordinates is the first editable row--&gt;What for--&gt;UNCLEAR
		// The property baseIndexForCoordinates is not Visible. baseIndexForCoordinates= row
		setBaseIndexForCoordinates(firstEditableRow);

		if (isExactDistrPotential)
			setBaseIndexForCoordinates(firstEditableRow - 1); //UNCLEAR

		// Number of data elements of tablePotential
		int tableSize = tablePotential
				.getTableSize();//--&gt;UNCLEAR What happens when there is no parent (f.e. when Tree/ADD )

		// Number of states of the variable of the node; if isTableDeltaPotential numDimensions=1
		int numDimensions = 1;
		if (!isExactDistrPotential)
			numDimensions = tablePotential.getDimensions()[0];
		// Parent variables + states of node variable
		numRows = firstEditableRow + numDimensions;
		lastEditableRow = numRows - 1;

		/*if (!isTableDeltaPotential) numRows++;*/ //--&gt; UNCLEAR Last row with the name of the variable and the state with '1' is REMOVED
		numColumns = numColumns + tableSize / numDimensions;

		// create the array of arrays
		return new Object[numRows][numColumns];
	}

	/**
	 * This methods fills the Upper Left corner of the table with the name of
	 * the parents of the node
	 *
	 * @param oldValues - the table that is being modified
	 * @author carmenyago
	 */
	protected Object[][] setParentsNameInUpperLeftCornerArea(Object[][] oldValues) {
		Object[][] values = oldValues;
		// Adding the parent
		// The first variable is always the node variable
		for (int i = 1; i < variables.size(); i++) {
			values[i - 1][0] = variables.get(i);
		}
		return values;
	}

	/**
	 * Sets the states of the parents in the top of the table
	 * Potential is not null
	 *
	 * @param oldValues - the table that is being modified.
	 *                     oldValues !=null and oldValues.lenght is always &gt; 0
	 * @author carmenyago
	 */
	protected Object[][] setParentsStatesInTopArea(Object[][] oldValues) {
		Object[][] values = oldValues;

		int numColumns = values[0].length;

		// Initialise the variable with the number of data columns
		int numRepetitions = numColumns - 1;
		int numParentVariables = variables.size() - 1;
		State[] states;

		for (int row = 0; row < numParentVariables; row++) {

			states = variables.get(row + 1).getStates();
			numRepetitions = numRepetitions / states.length;

			for (int column = 1; column < numColumns; column++) {
				// Find the index of the state. We start in zero position of the
				// array of states, and thus we need to substract a unit to
				// column
				// The ratio divides the table in sections and the module
				// get the position relative to the section.
				int stateIndex = ((column - 1) / numRepetitions) % states.length;
				State state = states[stateIndex];
				values[row][column] = state.getName();
			}

		}
		return values;
	}

	/**
	 * this method sets the first row with the values of the states of the node
	 * (if it is a node chance) or the name of the variable of the node (if it
	 * is a utility node)
	 *
	 * @param oldValues - the table that is being modified
	 * @author carmenyago
	 */
	protected Object[][] setNodeStatesInLeftArea(Object[][] oldValues) {
		Object[][] values = oldValues;
		if (isExactDistrPotential)
			values[firstEditableRow][0] = node.getName();
		else {

			// Why not trying lastEditableRow?
			//int length = values.length - 2;
			int length = lastEditableRow;
			for (State state : variables.get(0).getStates()) {
				values[length--][0] = state.getName();
			}
		}
		return values;
	}

	/**
	 * Sets the data table from potential in oldValues
	 *
	 * @param oldValues
	 * @return an array filled with the date table from tablePotential or tableDeltaPotential filled with the data values
	 * from tablePotential or tableDeltaPotential in the correct positions to be displayed by ValuesTable
	 */
	protected Object[][] setPotentialDataInCentreArea(Object[][] oldValues) {
		Object[][] values = oldValues;

		int numColumns = values[0].length;

		// rounding initial values
		double[] initialValues = tablePotential.getValues();
		double[] roundedValues = new double[initialValues.length];
		int maxDecimals = 10;
		double epsilon;
		epsilon = Math.pow(10, -(maxDecimals + 2));
		for (int i = 0; i < initialValues.length; i++) {
			roundedValues[i] = Util.roundAndReduce(initialValues[i], epsilon, maxDecimals);
		}
		// UNCLEAR--&gt;What for??
		//tablePotential.setValues(roundedValues);

		for (int j = 1; j <= numColumns - 1; j++) {

			// put the values on the table
			for (int i = getLastEditableRow(); i >= getFirstEditableRow(); i--) {
				int potentialIndex = tablePotentialsPanelOperations.getPotentialIndex(i, j, node);
				double value = roundedValues[potentialIndex];
				values[i][j] = value;
			}
		}
		return values;
	}

	/**
	 * This method calculates the number of data cells and stores it in the attribute positions.
	 * The number of data cell is the product of the number of states of all variables
	 *
	 * @author carmenyago
	 * minor changes
	 */
	protected int setNumberOfPostions() {
		int numPositions = 1;
		try {
			for (Variable variable : potential.getVariables()) {
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
	 * Calculates the position on valuesTable for a state combination
	 *
	 * @param stateIndices - indexes of the states
	 * @return an array containing the row at the first position and the column
	 * at the second position.
	 * revised--&gt; only changed the code between CMI, CMF
	 */
	protected int[] getRowAndColumnForStateCombination(int[] stateIndices, TablePotential potential) {
		int numStates = node.getVariable().getNumStates();
		int position = potential.getPosition(stateIndices);
		int tempMultiplier = tablePotential.getTableSize() / numStates;
		int tempColumnPosition = 0;

		// We start at index 1 because the state of the node is irrelevant for
		// obtain the column (only is relevant for the row)
		// We multiply the number of columns above each state and the index of
		// this variable (in wich state is)
		for (int i = 1; i < potential.getVariables().size(); i++) {
			Variable var = potential.getVariables().get(i);
			tempMultiplier = tempMultiplier / var.getNumStates();
			tempColumnPosition += stateIndices[i] * tempMultiplier;
		}

		// The column will be the column in the data structure plus one row at
		// the beginning
		int column = tempColumnPosition + 1;
		// The row will be the last row in the table minus the relative position
		// in the node state
		int row = getLastEditableRow() - (position % numStates);
		return new int[] { row, column };
	}

	/****
	 * Calculates the positions of the table which are not editable due to a
	 * link restriction or uncertainty in the columns. 
	 * If the position is not editable the position in the return array is set to 1, otherwise it contains a null value.
	 *
	 * @return a two dimensional array with the size of the table containing the
	 *         information about the editable positions.
	 *
	 * UNCLEAR--&gt; Can a utility Node have nodes with restriction and what to do?
	 * @author carmenyago
	 *
	 */
	protected Object[][] getNotEditablePositions() {
		Object[][] notEditablePositions = createEmptyTable();
		//CMI Bug #162 Applying restriction to utility Nodes
		//if (!isTableDeltaPotential && hasLinkRestriction){
		if (hasLinkRestriction) {
			//CMF
			List<int[]> statesWithRestriction = LinkRestrictionPotentialOperations
					.getStateCombinationsWithLinkRestriction(node);

			for (int[] state : statesWithRestriction) {
				int[] position = getRowAndColumnForStateCombination(state, tablePotential);
				int row = position[0];
				int column = position[1];
				notEditablePositions[row][column] = 1;
			}
		}
		// I suppose it is calculated previously
		//	uncertaintyInColumns = getUncertaintyInColumns();
		for (int row = firstEditableRow; row < notEditablePositions.length; ++row) {
			for (int column = 1; column < notEditablePositions[0].length; ++column) {
				if (uncertaintyInColumns[column - 1]) {
					notEditablePositions[row][column] = 1;
				}
			}
		}
		return notEditablePositions;
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
	protected EvidenceCase getConfiguration(int col) throws InvalidStateException, IncompatibleEvidenceException {

		List<Variable> parents = variables.subList(1, potential.getNumVariables());

		EvidenceCase evidence = new EvidenceCase();
		
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
		int initialIndex = (node.getNodeType() == NodeType.UTILITY) ? 0 : 1;
		int[] parentsConfiguration = null;
		
		int parentsSize = parents.size();
		if (parentsSize > 0) {
			parentsConfiguration = Arrays.copyOfRange(configuration, initialIndex, configuration.length);
		}

		// Adds to evidence a finding containing the parent and its configuration
		for (int j = 0; j < parentsSize; j++) {
			Finding finding = new Finding(parents.get(j), parentsConfiguration[j]);
			evidence.addFinding(finding);
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
	 * Creates and shows the UncertainValuesDialog object
	 *
	 * @throws WrongCriterionException revised--&gt;minor changes
	 */
	public void showUncertaintyDialog() throws WrongCriterionException {
		// Generates the evidenceCase based on the column
		// selected on the JTable object
		evidenceCase = getEvidenceCaseFromSelectedColumn();
		UncertainValuesDialog uncertDialog;
		if (isExactDistrPotential) {
			uncertDialog = new UncertainValuesDialog(Utilities.getOwner(this), evidenceCase,
					(ExactDistrPotential) potential);
		} else {
			uncertDialog = new UncertainValuesDialog(Utilities.getOwner(this), evidenceCase, tablePotential);
		}
		int button = uncertDialog.requestUncertainValues();
		if (button == UncertainValuesDialog.OK_BUTTON) {
			UncertainValuesEdit uncertEdit = null;
			try {
				uncertEdit = new UncertainValuesEdit(node, uncertDialog.getUncertainColumn(),
						uncertDialog.getValuesColumn(), uncertDialog.getPosBase(), selectedColumn,
						uncertDialog.isChanceVariable());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				node.getProbNet().doEdit(uncertEdit);
				if (selectedColumn > 0) {
					(
							(ValuesTableCellRenderer) getValuesTable().getDefaultRenderer(Double.class)
					).setMark(selectedColumn - 1);
					getValuesTable().repaint();
					this.getTableModel().setNotEditablePositions(getNotEditablePositions());
				}
			} catch (ConstraintViolationException | NonProjectablePotentialException | DoEditException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
						stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * This method initialises valuesTable and defines that first two columns cannot be selected
	 *
	 * @return a new values table.
	 * revised--&gt;not changed
	 */
	public ValuesTable getValuesTable() {
		if (valuesTable == null) {
			valuesTable = new ValuesTable(node, getTableModel(), modifiable);
			valuesTable.setName("PotentialsTablePanel.valuesTable");
		}
		return valuesTable;
	}

	/**
	 * This method initialises valuesTableScrollPane.
	 *
	 * @return a new values table scroll pane.
	 * revised--&gt;not changed
	 */
	protected JScrollPane getValuesTableScrollPane() {
		if (valuesTableScrollPane == null) {
			valuesTableScrollPane = new JScrollPane();
			valuesTableScrollPane.setName("TablePotentialPanel.valuesTableScrollPane");
			valuesTableScrollPane.setViewportView(getValuesTable());
		}
		return valuesTableScrollPane;
	}

	/**
	 * special method to show/hide the values table
	 * revised--&gt;not changed
	 */
	public void showValuesTable(final boolean visible) {
		getValuesTable().setVisible(visible);
	}

	/**
	 * This method returns the tableModel of valuesTable. If valuesTable has not a tableModel, this method creates one.
	 *
	 * @return the tableModel of valuesTable.
	 * @see ValuesTable
	 * revised--&gt;minor changes
	 */
	protected ValuesTableModel getTableModel() {
		ValuesTableModel tableModel = null;
		if ((valuesTable == null) || (valuesTable.getTableModel() == null))
			tableModel = new ValuesTableModel(data, columns, firstEditableRow);
		else
			tableModel = (ValuesTableModel) valuesTable.getModel();

		return tableModel;
	}

	/**
	 * Show/Hide all the parameters
	 *
	 * @param showAllParameters the showAllParameters to set
	 */
	public void setShowAllParameters(boolean showAllParameters) {
		this.showAllParameters = showAllParameters;
		valuesTable.setShowingAllParameters(showAllParameters);
	}

	/**
	 * Handles an action performed
	 * revised--&gt;not changed
	 */
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals(ActionCommands.UNCERTAINTY_ASSIGN) || actionCommand
				.equals(ActionCommands.UNCERTAINTY_EDIT)) {
			try {
				showUncertaintyDialog();
			} catch (WrongCriterionException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this, stringDatabase.getString(e1.getMessage()),
						stringDatabase.getString(e1.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
		} else if (actionCommand.equals(ActionCommands.UNCERTAINTY_REMOVE)) {
			try {
				removeUncertainty();
			} catch (WrongCriterionException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this, stringDatabase.getString(e1.getMessage()),
						stringDatabase.getString(e1.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Method for removing the uncertain values for a certain configuration
	 *
	 * @throws WrongCriterionException revised--&gt;minor changes; only changed the call to getNotEditablePositions
	 */
	public void removeUncertainty() throws WrongCriterionException {
		evidenceCase = getEvidenceCaseFromSelectedColumn();
		UncertainValuesRemoveEdit uncertEdit = new UncertainValuesRemoveEdit(node, evidenceCase);
		try {
			node.getProbNet().doEdit(uncertEdit);
			if (selectedColumn > 0) {
				(
						(ValuesTableCellRenderer) getValuesTable().getDefaultRenderer(Double.class)
				).unMark(selectedColumn - 1);
				getValuesTable().repaint();
				this.getTableModel().setNotEditablePositions(getNotEditablePositions());
			}
		} catch (ConstraintViolationException | NonProjectablePotentialException | DoEditException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Method for update the options showed in the contextual menu
	 * revised--&gt;not changed
	 */
	protected void updateContextualMenuOptions() {
		if (node.getPotentials().size() > 0 && node.getPotentials().get(0) instanceof TablePotential) {
			TablePotential tablePotential = (TablePotential) node.getPotentials().get(0);
			boolean hasUncertainty = tablePotential.hasUncertainty(getEvidenceCaseFromSelectedColumn());
			if (hasUncertainty) {
				getUncertaintyContextualMenu().getJComponentActionCommand(ActionCommands.UNCERTAINTY_ASSIGN.toString())
						.setEnabled(false);
				getUncertaintyContextualMenu().getJComponentActionCommand(ActionCommands.UNCERTAINTY_EDIT.toString())
						.setEnabled(true);
				getUncertaintyContextualMenu().getJComponentActionCommand(ActionCommands.UNCERTAINTY_REMOVE.toString())
						.setEnabled(true);
			} else {
				getUncertaintyContextualMenu().getJComponentActionCommand(ActionCommands.UNCERTAINTY_ASSIGN.toString())
						.setEnabled(true);
				getUncertaintyContextualMenu().getJComponentActionCommand(ActionCommands.UNCERTAINTY_EDIT.toString())
						.setEnabled(false);
				getUncertaintyContextualMenu().getJComponentActionCommand(ActionCommands.UNCERTAINTY_REMOVE.toString())
						.setEnabled(false);
			}
		}
	}

	/**
	 * Handles the double click in a cell
	 *
	 * @param evt
	 */
	protected void doubleClickEvent(MouseEvent evt) {
		if (node.getPotentials().size() > 0 && node.getPotentials().get(0) instanceof TablePotential) {
			TablePotential tablePotential = (TablePotential) node.getPotentials().get(0);

			EvidenceCase configuration = null;
			int selectedColumn = valuesTable.columnAtPoint(evt.getPoint());
			try {
				configuration = getConfiguration(selectedColumn);
			} catch (InvalidStateException | IncompatibleEvidenceException e) {
				e.printStackTrace();
			}
			boolean hasUncertainty = tablePotential.hasUncertainty(configuration);
			if (hasUncertainty) {
				try {
					showUncertaintyDialog();
				} catch (WrongCriterionException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this, stringDatabase.getString(e1.getMessage()),
							stringDatabase.getString(e1.getMessage()), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 * This method initialises uncertaintyContextualMenu.
	 *
	 * @return the node contextual menu.
	 * revised--&gt;not changed
	 */
	protected UncertaintyContextualMenu getUncertaintyContextualMenu() {
		if (uncertaintyContextualMenu == null) {
			uncertaintyContextualMenu = new UncertaintyContextualMenu(this);
			uncertaintyContextualMenu.setName("uncertaintyContextualMenu");
		}
		return uncertaintyContextualMenu;
	}

	/**
	 * This method sets renders for the cells in the table. Only has to be called when it sets
	 * data.
	 * It is always used when potential!=null
	 * <p>
	 * In a DECISION node a change is colored in green
	 * <p>
	 * UNCLEAR--&gt; When ReadOnly is se?
	 * <p>
	 * NodeType.DECISION + policyType.OPTIMAL +!potential.isUtility()
	 *
	 * @param uncertaintyInColumns
	 * @author carmenyago
	 */
	protected void setCellRenderers(boolean[] uncertaintyInColumns) {

		TableCellRenderer cellRenderer = null;

		if (node.getNodeType() != NodeType.DECISION) {
			// Creates the TableCellRenderer distinguishing if the node has or not link restrictions
			if (!hasLinkRestriction) {
				cellRenderer = new ValuesTableCellRenderer(firstEditableRow, uncertaintyInColumns);
			} else {
				cellRenderer = new ValuesTableWithLinkRestrictionCellRenderer(firstEditableRow, uncertaintyInColumns);
			}

		} else { // node.getNodeType() == NodeType.DECISION)
			if ((node.getPolicyType() == PolicyType.OPTIMAL) && (
					node.getPotentials().isEmpty() || (
							!node.getPotentials().get(0).isAdditive()
					)
			))

			{
				// UNCLEAR--&gt; When ReadOnly is se?
				// A node has policy if is a decision node with a non uniform potential
				boolean imposingPolicyByUser = node.hasPolicy() && !isReadOnly();
				cellRenderer = new ValuesTableOptimalPolicyCellRenderer(firstEditableRow, uncertaintyInColumns,
						imposingPolicyByUser);
			} else {
				boolean showingOptimalPolicy = node.getPotentials().get(0).isAdditive() && isReadOnly();
				if (!showingOptimalPolicy) {
					cellRenderer = new ValuesTableCellRenderer(firstEditableRow, uncertaintyInColumns);
				} else {
					// When showing the expected utility we want the color of the cells to be green
					cellRenderer = new ValuesTableOptimalPolicyCellRenderer(firstEditableRow, uncertaintyInColumns,
							true);
				}
			}
		}
		valuesTable.setDefaultRenderer(Double.class, cellRenderer);
		valuesTable.setDefaultRenderer(String.class, cellRenderer);
	}

	/**
	 * Method to define the specific listeners in this table (not defined in the
	 * common KeyTable hierarchy. This method creates the evidenceCase object
	 * when the user do right click on the table.
	 */
	protected void setTableSpecificListeners() {
		valuesTable.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				int row = valuesTable.rowAtPoint(e.getPoint());
				int col = valuesTable.columnAtPoint(e.getPoint());
				selectedColumn = col;
				if (SwingUtilities.isLeftMouseButton(e)) {
					valuesTable
							.editCellAt(valuesTable.rowAtPoint(e.getPoint()), valuesTable.columnAtPoint(e.getPoint()),
									e);
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					if ((row > -1) && (col > 0) && !isReadOnly()) {
						if (getUncertaintyContextualMenu() != null) {
							updateContextualMenuOptions();
							getUncertaintyContextualMenu().show(valuesTable, e.getX(), e.getY());
						}
					}
				}
			}

		});
		valuesTable.addMouseListener(new DoubleClickListener());
	}

	/**
	 * Close the table
	 * revised--&gt;not changed
	 */
	@Override public void close() {
		getValuesTable().close();
	}

	/**
	 * This method sets the attributes this.readOnly= readOnly and modifiable = !readOnly to indicate
	 * if the table is read only (readOnly=true) or editable (readOnly = false).
	 * It also changes the cell renderer according to readOnly
	 *
	 * @param readOnly - if true, all the table cells become not editable, if false the data cells become editable
	 *                 revised--&gt;minor changes; only changed the call to getUncertaintyInColumns
	 */
	@Override public void setReadOnly(boolean readOnly) {
		boolean wasReadOnly = super.isReadOnly();
		super.setReadOnly(readOnly);
		/*
		The read only attribute is set after the constructor is invoked and then,
		after the setData(node) method is called. Thus, the cell renderer may need to be changed.
		This is the case if the new read only value is different from the previous one.
		 */
		if (wasReadOnly != readOnly) {
			boolean[] uncertaintyInColumns = null;
			if (node.getPotentials() != null) {
				uncertaintyInColumns = getUncertaintyInColumns();
				setCellRenderers(uncertaintyInColumns);
			} else {
				setCellRenderers(uncertaintyInColumns);
			}
		}
		getValuesTable().setModifiable(!readOnly);
	}

	/**
	 * This class overrides the double click listener calling the
	 *
	 * @see DoubleClickListener
	 * revised--&gt;not changed
	 */
	public class DoubleClickListener extends MouseAdapter {

		@Override public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				doubleClickEvent(e);
			}
		}
	}

}




