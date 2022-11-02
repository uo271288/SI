/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.constraint;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.constraint.NoRevelationArc;

public class AlwaysObservedPropertyValidator {
	/*****
	 * Checks if a node can have the alwaysObserved property.
	 * @param node
	 * @return <code>true</code> if the node can have the alwaysObserved property.
	 */
	public static boolean validate(Node node) {
		if (!node.getProbNet().hasConstraint(NoRevelationArc.class)) {
			if (node.getNodeType() == NodeType.CHANCE) {
				return true;
			}
		}
		return false;
	}

}
