/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.PotentialChangeEdit;
import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.potential.AugmentedTable;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;

/**
 * <code>UnivariateDistrPotentialEdit</code> is a simple edit that allows to modify the
 * node's <code>Potential</code> values when its potential is an UnivariateDistrPotential.
 *
 * @author carmenyago
 * @version 1.0  03/04/2017
 */
@SuppressWarnings("serial") public class UnivariateDistrPotentialValueEdit extends SimplePNEdit {
	/**
	 * The column of the table where is the potential
	 */
	private int col;
	/**
	 * The row of the table where is the potential
	 */
	private int row;
	/**
	 * Index of the value selected
	 */
	private int potentialSelected;

	/**
	 * The new class of the distribution
	 */
	private Class<? extends ProbDensFunction> newDistributionClass;

	/**
	 * The UnivariateDistrPotential to be changed
	 */
	private UnivariateDistrPotential oldPotential;

	/**
	 * The new UnivariateDistrPotential
	 */

	private UnivariateDistrPotential newPotential;

	/**
	 * The new distributionTable
	 */
	private AugmentedTable newDistributionTable;

	/**
	 * Pseudo-util class with common operations used  in potential tables
	 */
	private PotentialsTablePanelOperations tablePotentialsPanelOperations;

	/**
	 * The node which potential is changed
	 */
	private Node node;

	// Constructor

	/**
	 * Creates a new <code>UnivariateDistrPotentialEdit</code> in which one value of the <code>distributionTable</code> is changed.
	 * Specifying the node to be edited, the new value to be inserted in (row,col) and the notEditablePositions of the displayed table.
	 *
	 * @param node                 - the node to be changed
	 * @param newValue             - the new value to be inserted in the table
	 * @param row                  - the row where the new value is inserted
	 * @param col                  - the column where the new value is inserted
	 * @param notEditablePositions - the notEditablePositions of the displayed table
	 * @see org.openmarkov.core.model.network.potential.UnivariateDistrPotential
	 */
	public UnivariateDistrPotentialValueEdit(Node node, String newValue, int row, int col,
			Object[][] notEditablePositions) {
		super(node.getProbNet());
		this.node = node;
		try {
			this.oldPotential = (UnivariateDistrPotential) node.getPotentials().get(0);
		} catch (Exception e) {
			e.printStackTrace();
/* TODO
			JOptionPane.showMessageDialog(this,
					stringDatabase.getValuesInAString(e.getMessage()),
					stringDatabase.getValuesInAString(e.getMessage()),
					JOptionPane.ERROR_MESSAGE);
			return;		
*/
		}
		this.row = row;
		this.col = col;
		this.tablePotentialsPanelOperations = new PotentialsTablePanelOperations();

		this.newPotential = new UnivariateDistrPotential(oldPotential);
		this.newDistributionTable = newPotential.getDistributionTable();
		this.potentialSelected = tablePotentialsPanelOperations.getPotentialIndex(row, col, newDistributionTable);
		newDistributionTable.getFunctionValues()[potentialSelected] = newValue;
	}

	/**
	 * Creates a new <code>UnivariateDistrPotentialEdit</code> specifying the node to be and the new probability distribution.
	 * This is used when the distribution of <code>UnivariateDistrPotential</code> is changed.
	 *
	 * @param node             - the node to be edited
	 * @param distributionName - the name of the distribution to be created. Represents the attribute name in ProbDensFunctionType which represents the distribution class
	 * @see org.openmarkov.core.model.network.potential.UnivariateDistrPotential
	 */
	public UnivariateDistrPotentialValueEdit(Node node, String distributionName) {
		super(node.getProbNet());
		this.node = node;
		UnivariateDistrPotential potential = null;
		try {
			//The old univariateDistrPotential
			potential = (UnivariateDistrPotential) node.getPotentials().get(0);
			this.oldPotential = potential;
		} catch (Exception e) {
			e.printStackTrace();
			return;
/* TODO
			JOptionPane.showMessageDialog(this,
					stringDatabase.getValuesInAString(e.getMessage()),
					stringDatabase.getValuesInAString(e.getMessage()),
					JOptionPane.ERROR_MESSAGE);
			return;		
*/
		}

		if (distributionName.equals(potential.getProbDensFunctionName())) {
			this.newPotential = new UnivariateDistrPotential(potential);
		} else {
			this.newDistributionClass = ProbDensFunctionManager.getUniqueInstance()
					.getProbDensFunctionClass(distributionName);
			this.newPotential = new UnivariateDistrPotential(potential.getVariables(), newDistributionClass,
					potential.getPotentialRole());
		}
	}

	/**
	 * This method changes the old UnivariateDistrPotential in node for the updated potential
	 * and updates the probNet
	 */
	@Override public void doEdit() throws DoEditException {
		PotentialChangeEdit changePotentialEdit = null;
		changePotentialEdit = new PotentialChangeEdit(probNet, oldPotential, newPotential);
		try {
			probNet.doEdit(changePotentialEdit);
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
			e.printStackTrace();
			throw new DoEditException(e);
		}
	}

	/**
	 * Gets the row position associated to value edited if priorityList exists
	 *
	 * @param position position of the value in the array of values
	 * @return the position in the table
	 */
	public int getRowPosition(int position) {
		int lastRow = tablePotentialsPanelOperations.calculateLastEditableRow(node);
		return lastRow - position % newDistributionTable.getDimensions()[0];
	}

	/**
	 * Gets the row position associated to value edited if priorityList no
	 * exists
	 *
	 * @return the position in the table
	 */
	public int getRowPosition() {
		return row;
	}

	/**
	 * Gets the column where the value is edited
	 *
	 * @return the column edited
	 */
	public int getColumnPosition() {
		return col;
	}

}
