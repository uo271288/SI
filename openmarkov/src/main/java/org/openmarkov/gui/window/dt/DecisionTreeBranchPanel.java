/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.dt;

import org.openmarkov.core.dt.DecisionTreeBranch;
import org.openmarkov.core.dt.DecisionTreeNode;
import org.openmarkov.core.model.network.NodeType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@SuppressWarnings("serial") public class DecisionTreeBranchPanel extends DecisionTreeElementPanel {
	private DecisionTreeBranch treeBranch;

	public DecisionTreeBranchPanel(DecisionTreeBranch treeBranch) {
		super();
		this.treeBranch = treeBranch;
	}
	
	public DecisionTreeBranch getTreeBranch() {
		return treeBranch;
	}

	/**
	 * Builds the text to be shown in the branch
	 */
	public String getBranchDescriptiontHTML() {
		StringBuilder txtLeft = new StringBuilder("<html><table border=1>");
		DecisionTreeNode parent = treeBranch.getParent();
		if (parent != null && parent.getNodeType() == NodeType.DECISION) {
			if (parent.isBestDecision(treeBranch)) {
				txtLeft.append("<td width=10px bgcolor=red border=0></td>");
			} else {
				txtLeft.append("<td width=10px border=0></td>");
			}
		}
		txtLeft.append("<td align=center border=0>");
		if (treeBranch.getBranchVariable() != null) {
			txtLeft.append(treeBranch.getBranchVariable().getName() + "=");
			txtLeft.append(treeBranch.getBranchState().getName());
		}
		if (parent != null && parent.getNodeType() == NodeType.CHANCE) {
			txtLeft.append(" / ");
			txtLeft.append(" P=" + df.format(treeBranch.getBranchProbability()));
		}
		
		txtLeft.append(treeBranch.getChild().formatUtility(df,parent != null));
		txtLeft.append("</td>");
		txtLeft.append("</table></html>");
		return txtLeft.toString();
	}

	

	@Override public void update(boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		leftLabel.setText(getBranchDescriptiontHTML());
	}
}
