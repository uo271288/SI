/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.dt;

import org.openmarkov.core.dt.DecisionTreeNode;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.dialog.treeadd.IconFactory;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial") public class DecisionTreeNodePanel extends DecisionTreeElementPanel {
	private static Map<String, Icon> chanceNodeIconPool = new HashMap<>();
	private static Map<String, Icon> decisionNodeIconPool = new HashMap<>();
	private static Map<String, Icon> utilityNodeIconPool = new HashMap<>();
	private DecisionTreeNode treeNode;

	/**
	 * Constructor for DecisionTreeNodePanel.
	 *
	 * @param treeNode
	 */
	public DecisionTreeNodePanel(DecisionTreeNode treeNode) {
		this.treeNode = treeNode;
		leftLabel.setIcon(createNodeIcon(treeNode.getVariable(), treeNode.getNodeType()));
	}
	
	public DecisionTreeNode getTreeNode() {
		return treeNode;
	}


	/**
	 * Create a new icon for a node of the ADD/Tree
	 * @return the new node icon created
	 */
	protected Icon createNodeIcon(Variable variable, NodeType nodeType) {
		Font textIconFont = new Font("Helvetica", Font.BOLD, 15);
		Icon icon = null;
		switch (nodeType) {
		case CHANCE: {
			icon = chanceNodeIconPool.get(variable.getName());
			if (icon == null) {
				icon = IconFactory.createChanceIcon(variable.getName(), textIconFont);
				chanceNodeIconPool.put(variable.getName(), icon);
			}
			break;
		}
		case DECISION: {
			if (icon == null) {
				icon = IconFactory.createDecisionIcon(variable.getName(), textIconFont);
				decisionNodeIconPool.put(variable.getName(), icon);
			}
			break;
		}
		case UTILITY: {
			if (icon == null) {
				icon = IconFactory.createUtilityIcon(variable.getName(), textIconFont);
				utilityNodeIconPool.put(variable.getName(), icon);
			}
			break;
		}
		}
		return icon;
	}

	@Override public void update(boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (treeNode.getNodeType() == NodeType.UTILITY) {
			rightLabel.setText(treeNode.formatUtility(df,false));
		}
	}

	public NodeType getNodeType() {
		return treeNode.getNodeType();
	}
}
