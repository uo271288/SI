/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmarkov.core.action.PotentialChangeEdit;
import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.potential.AugmentedTable;
import org.openmarkov.core.model.network.potential.AugmentedTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;
import org.openmarkov.gui.localize.LocalizedException;

import java.util.List;

/**
 * @author carmenyago
 * @version 1.1 22/05/2017 Changed into AugmentedPotentialValueEdit: now is the edit for all the AugmentedPotentials
 */
@SuppressWarnings("serial") public class AugmentedPotentialValueEdit extends SimplePNEdit {
	/**
	 * The column of the table where is the potential
	 */
	private int col;
	/** 
	 * The row of the table where is the potential
	 */
	private int row;
	/**
	 * A list that store the edition order
	 */
	private List<Integer> priorityList;

	/**
	 * Index of the value selected
	 */
	private int indexSelected;
	/**
	 * The new potential
	 */
	private Potential oldPotential;

	/**
	 * The new potential
	 */
	private Potential newPotential;

	/**
	 * The AugmentedTable potential
	 */
	private AugmentedTablePotential newAugmentedTablePotential;
	/**
	 * Old table potential
	 */
	private AugmentedTablePotential oldAugmentedTablePotential;

	/**
	 * The UnivariateDistr Potential
	 */
	private UnivariateDistrPotential newUnivariateDistrPotential;
	/**
	 * Old table potential
	 */
	private UnivariateDistrPotential oldUnivariateDistrPotential;

	/**
	 * Pseudo-util class with common operations used  in potential tables
	 */
	private PotentialsTablePanelOperations tablePotentialsPanelOperations;

	/**
	 * the table potential
	 */
	private AugmentedTable newAugmentedTable;
	private String[] newAugmentedValues;
	
	/**
	 * Logger
	 */
	private Logger logger;
	
	// Constructor

	/**
	 * Creates a new <code>NodePotentialEdit</code> specifying the node to be
	 * edited, the new value of the potential, the row and column where is the
	 * value to be modified and a priority list for potentials updating.
	 *
	 * @param node                 the node to be edited
	 * @param newValue             the new value
	 * @param col                  the column in the edited table
	 * @param row                  the row in the edited table
	 * @param priorityList         the priority lists for potentials update.
	 * @param notEditablePositions two dimensional array with the information about editable
	 *                             positions.
	 */
	public AugmentedPotentialValueEdit(Node node, String newValue, int row, int col, List<Integer> priorityList,
			Object[][] notEditablePositions) {
		super(node.getProbNet());
		logger = LogManager.getLogger(AugmentedPotentialValueEdit.class.getName());
		boolean isAugmentedTablePotential = false;
		try {
			oldPotential = node.getPotentials().get(0);
			if (oldPotential instanceof AugmentedTablePotential) {
				oldAugmentedTablePotential = (AugmentedTablePotential) oldPotential;
				newAugmentedTablePotential = new AugmentedTablePotential(oldAugmentedTablePotential);
				newPotential = newAugmentedTablePotential;
				newAugmentedTable = newAugmentedTablePotential.getAugmentedTable();
				newAugmentedValues = newAugmentedTable.getFunctionValues();
				isAugmentedTablePotential = true;

			} else {
				oldUnivariateDistrPotential = (UnivariateDistrPotential) oldPotential;
				newUnivariateDistrPotential = new UnivariateDistrPotential(oldUnivariateDistrPotential);
				newPotential = newUnivariateDistrPotential;
				newAugmentedTable = newUnivariateDistrPotential.getAugmentedTable();
				newAugmentedValues = newAugmentedTable.getFunctionValues();

			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
			LocalizedException exception = new LocalizedException(e);
			exception.showException();

			
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
		this.setIndexSelected(tablePotentialsPanelOperations.calculateLastEditableRow(newAugmentedTable) - row);
		//Set the entire column to "Complement"
		if (isAugmentedTablePotential) {
			int firstEditableRow = tablePotentialsPanelOperations.calculateFirstEditableRow(newAugmentedTable);
			int lastEditableRow = tablePotentialsPanelOperations.calculateLastEditableRow(newAugmentedTable);

			for (int i = firstEditableRow; i <= lastEditableRow; i++) {
				int index = tablePotentialsPanelOperations.getPotentialIndex(i, col, newAugmentedTable);
				newAugmentedValues[index] = new String("Complement");
			}
		}
		this.indexSelected = tablePotentialsPanelOperations.getPotentialIndex(row, col, newAugmentedTable);
		newAugmentedValues[indexSelected] = new String(newValue);
	}

	/**
	 * Creates a new <code>UnivariateDistrPotentialEdit</code> specifying the node to be and the new probability distribution.
	 * This is used when the distribution of <code>UnivariateDistrPotential</code> is changed.
	 *
	 * @param node             - the node to be edited
	 * @param distributionName - the name of the distribution to be created. Represents the attribute name in ProbDensFunctionType which represents the distribution class
	 * @see org.openmarkov.core.model.network.potential.UnivariateDistrPotential
	 */
	public AugmentedPotentialValueEdit(Node node, String distributionName) {
		super(node.getProbNet());
		try {
			//The old univariateDistrPotential
			oldPotential = node.getPotentials().get(0);
			oldUnivariateDistrPotential = (UnivariateDistrPotential) oldPotential;
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
			LocalizedException exception = new LocalizedException(e);
			exception.showException();
			return;
/* TODO
            JOptionPane.showMessageDialog(this,
                    stringDatabase.getValuesInAString(e.getMessage()),
                    stringDatabase.getValuesInAString(e.getMessage()),
                    JOptionPane.ERROR_MESSAGE);
            return;     
*/
		}

		if (distributionName.equals(oldUnivariateDistrPotential.getProbDensFunctionName())) {
			newUnivariateDistrPotential = new UnivariateDistrPotential(oldUnivariateDistrPotential);
		} else {
			Class<? extends ProbDensFunction> newDistributionClass = ProbDensFunctionManager.getUniqueInstance()
					.getProbDensFunctionClass(distributionName);
			newUnivariateDistrPotential = new UnivariateDistrPotential(oldPotential.getVariables(),
					newDistributionClass, oldPotential.getPotentialRole());
		}
		newPotential = newUnivariateDistrPotential;
		this.indexSelected = -1;
	}

	/*
	 *
	 */
	@Override public void doEdit() throws DoEditException {
		PotentialChangeEdit changePotentialEdit = null;
		changePotentialEdit = new PotentialChangeEdit(probNet, oldPotential, newPotential);
		try {
			probNet.doEdit(changePotentialEdit);
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
			LocalizedException exception = new LocalizedException(e);
			exception.showException();
			throw new DoEditException(e);
		}
	}

	/**
	 * Gets the table-potential of the node
	 *
	 * @return variable1 <code>Variable</code>
	 */
	public Potential getPotential() {
		return newPotential;
	}

	/**
	 * Gets the priority list
	 *
	 * @return the priority list
	 */
	public List<Integer> getPriorityList() {
		return priorityList;
	}

	/**
	 * Gets the row position associated to value edited if priorityList exists
	 *
	 * @param position position of the value in the array of values
	 * @return the position in the table
	 */
	public int getRowPosition(int position) {
		int lastRow = tablePotentialsPanelOperations.calculateLastEditableRow(newAugmentedTable);
		return lastRow - position % newAugmentedTable.getDimensions()[0];
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

	/**
	 * @return the indexSelected
	 */
	public int getIndexSelected() {
		return indexSelected;
	}

	/**
	 * @param indexSelected the indexSelected to set
	 */
	public void setIndexSelected(int indexSelected) {
		this.indexSelected = indexSelected;
	}

}
