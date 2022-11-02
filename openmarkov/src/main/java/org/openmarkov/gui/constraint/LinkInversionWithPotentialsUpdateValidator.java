/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.constraint;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.AugmentedTable;
import org.openmarkov.core.model.network.potential.AugmentedTablePotential;
import org.openmarkov.core.model.network.potential.BinomialPotential;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;

import java.util.ArrayList;
import java.util.List;

/******
 * This class validates if a link can be inverted arc-reversal style
 *
 * @author iagoparís - summer
 *
 */
public class LinkInversionWithPotentialsUpdateValidator {

	/******
	 * Links can be inverted if each one of its nodes has a table potential or one convertible to a table.
	 *
	 * @return <code>true</code> if it is so.
	 */
	public static boolean validate(Link<Node> link) {

		boolean validPotentials;

		try {
			Potential potential1 = link.getNode1().getPotentials().get(0);
			Potential potential2 = link.getNode2().getPotentials().get(0);
			validPotentials = validatePotential(potential1) && validatePotential(potential2);
		} catch (IndexOutOfBoundsException ex) {
			// This exception is thrown when one of the involved nodes is decision.
			return false; // Arc reversal is only applicable if both nodes are chance.
		}

		// 1. The link must be directed
		// 2. The potential must be convertible to TablePotential
		return (link.isDirected() && validPotentials);
	}

	private static boolean validatePotential(Potential potential) {
		// TablePotential is OK, and only the next potentials can't be converted to it.
		return (!(potential instanceof AugmentedTable ||
				potential instanceof AugmentedTablePotential ||
				potential instanceof BinomialPotential ||
				potential instanceof FunctionPotential ||
				potential instanceof SameAsPrevious ||
				potential instanceof UnivariateDistrPotential));
	}
}
