/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

/**
 * OpenMarkov - PotentialsTablePanelOperations.java
 */
package org.openmarkov.gui.component;

//import java.util.ArrayList;

import org.openmarkov.core.exception.NullListPotentialsException;
import org.openmarkov.core.exception.NullPotentialException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import javax.swing.*;
import java.util.List;


/**
 * Auxiliary methods for PotentialsTablePanel class
 *
 * @author jlgozalo
 * @author marias
 * @author carmenyago
 * @version 2.0 27/05/2016 by carmenyago
 */
public class PotentialsTablePanelOperations implements TableMethods {

	/**
	 * calculate the first editable Row of the table, based upon the number of parents for the node.
	 * The first editable row equals the number of parents of the node
	 *
	 * @param node - node with contains the potentials
	 *             carmenyago removed the dependency from the NodeType
	 * @author carmenyago
	 */
	@Override public int calculateFirstEditableRow(Node node) {
		try {
			checkIfNoPotential(node.getPotentials());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		int row = 0;
		row = node.getPotentials().get(0).getNumVariables() - 1;
		return row;
	}

	/**
	 * This method calculates the last editable row of the table.
	 * The last editable row is (number_of_parents of the node + number_of_states of the variable node)
	 * <p>
	 * carmenyago removed the dependence with NodeType
	 *
	 * @param node node who "owns" the table
	 * @author carmenyago
	 */
	@Override public int calculateLastEditableRow(Node node) {
		try {
			checkIfNoPotential(node.getPotentials());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "There is not a valid potential");
			return 0;
		}

		int row = 0;
		Potential potential = node.getPotentials().get(0);
		if (getIsExactDistrPotential(potential))
			row = potential.getNumVariables() - 1;
		else
			// Number of parents + Number of variable states -1
			row = node.getPotentials().get(0).getNumVariables() - 1 + node.getVariable().getStates().length - 1;

		return row;
	}

	/**
	 * This method determines if a list of potentials is empty or not
	 * @param listPotentials - the list of potentials to check
	 * @throws NullListPotentialsException if listPotentials is null
	 * @throws NullPotentialException      if listPotentials is empty
	 * carmenyago simplified the method
	 * @author carmenyago
	 */
	public void checkIfNoPotential(List<Potential> listPotentials)
			throws NullListPotentialsException, NullPotentialException {

		if (listPotentials == null)
			throw new NullListPotentialsException("");
		if (listPotentials.isEmpty())
			throw new NullPotentialException("");
	}

	/**
	 * True if the class of the potential is ExactDistrPotential
	 *
	 * @param potential - The potential to check
	 * @return true if the class of the potential is ExactDistrPotential; false otherwise
	 */
	public boolean getIsExactDistrPotential(Potential potential) {
		return (potential instanceof ExactDistrPotential);
		// potential.getClass().getName().equals("org.openmarkov.core.model.network.potential.ExactDistrPotential");
	}

	@Override public int getPotentialIndex(int row, int column, Node node) {
		try {
			checkIfNoPotential(node.getPotentials());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "There is not a valid potential");
			return 0;
		}
		// First of all we get the start index of the column
		int potentialIndex = getPotentialStartIndexOfColumn(column, node);

		// We get the last editable row in the JTable
		int lastRow = calculateLastEditableRow(node);

		// Then we move a number of positions equals to the row (without the headers)
		potentialIndex += (lastRow - row);
		return potentialIndex;
	}

	//CMI

	/**
	 * Created for univariateDistributions
	 */

	public int getPotentialIndex(int row, int column, TablePotential tableDistribution) {
		// First of all we get the start index of the column
		int potentialIndex = getPotentialStartIndexOfColumn(column, tableDistribution);

		// We get the last editable row in the JTable
		int lastRow = calculateLastEditableRow(tableDistribution);

		// Then we move a number of positions equals to the row (without the headers)
		potentialIndex += (lastRow - row);
		return potentialIndex;
	}

	public int getPotentialStartIndexOfColumn(int column, TablePotential tablePotential) {

		// Index in tablePotential of the beginning of the column
		int position = 0;
		// Making the column 1 as the first (column 0)
		int temp = column - 1;

		// In this code we get the coordinates (states index) of the variable and
		// we calculate the position in the list of potentials. The position
		// The position is the product of each state index and the respective offset
		// s[0]*offset[0] + s[1]*offset[1] + ..... + s[n]*offset[n]

		// Dimensions--&gt; list with the states of each variable of the potential
		//
		// Now there is no difference between CHANCE and UTILITY
		int[] dimensions = tablePotential.getDimensions();
		int numberOfDimensions = 0;
		if (dimensions == null) {
			return 0;
		} else
			numberOfDimensions = dimensions.length - 1;

		int lowerBound = 0;
		//if (getIsExactDistrPotential(potential)) lowerBound = -1;
		for (int i = numberOfDimensions; i > lowerBound; i--) {
			int dimension = dimensions[i];
			position += (temp % dimension) * tablePotential.getOffsets()[i];
			temp = temp / dimension;
		}
		return position;
	}

	public int calculateFirstEditableRow(TablePotential potential) {
		int row = 0;
		row = potential.getNumVariables() - 1;
		return row;
	}

	public int calculateLastEditableRow(TablePotential potential) {

		int row = 0;
		//	if (getIsExactDistrPotential(potential))
		//		row =potential.getNumVariables()-1;

		// Number of parents + Number of variable states -1
		row = potential.getNumVariables() - 1 + potential.getVariable(0).getStates().length - 1;
		return row;
	}

	//CMF

	/**
	 * Given the number of column of a JTable,
	 * this method calculates the index in the table of a first potential of a node
	 * corresponding to the first cell in the column.
	 * If there is no potential or the potential has no states, the method returns 0 (keeping the previous behaviour)
	 *
	 * @param column - the index of a column
	 * @param node   - the node with the potential
	 * @return index of the potential.
	 * @author carmenyago
	 */
	public int getPotentialStartIndexOfColumn(int column, Node node) {
		/*
		 * This code is here and in getPotentialIndex because this method is used not only in  getPotentialIndex
		 * but in org.openmarkov.gui.action.TablePotentialValueEdit
		 */
		try {
			checkIfNoPotential(node.getPotentials());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "There is not a valid potential");
			return 0;
		}

		Potential potential = node.getPotentials().get(0);
		TablePotential tablePotential = null;

		if (getIsExactDistrPotential(potential))
			tablePotential = ((ExactDistrPotential) potential).getTablePotential();
		else
			tablePotential = (TablePotential) potential;

		// Index in tablePotential of the beginning of the column
		int position = 0;
		// Making the column 1 as the first (column 0)
		int temp = column - 1;

		// Supposing Dimensions >=1 UNCLEAR

		// In this code we get the coordinates (states index) of the variable and
		// we calculate the position in the list of potentials. The position
		// The position is the product of each state index and the respective offset
		// s[0]*offset[0] + s[1]*offset[1] + ..... + s[n]*offset[n]

		// Dimensions--&gt; list with the states of each variable of the potential
		//
		// Now there is no difference between CHANCE and UTILITY
		int[] dimensions = tablePotential.getDimensions();
		int numberOfDimensions = 0;
		if (dimensions == null) {
			return 0;
		} else
			numberOfDimensions = dimensions.length - 1;

		int lowerBound = 0;
		if (getIsExactDistrPotential(potential))
			lowerBound = -1;
		for (int i = numberOfDimensions; i > lowerBound; i--) {
			int dimension = dimensions[i];
			position += (temp % dimension) * tablePotential.getOffsets()[i];
			temp = temp / dimension;
		}
		return position;
	}

}

