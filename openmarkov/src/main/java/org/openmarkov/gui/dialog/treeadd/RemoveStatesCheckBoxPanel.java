/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author myebra
 */
@SuppressWarnings("serial") public class RemoveStatesCheckBoxPanel extends JPanel {
	private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	private TreeADDBranch branch;
	private TreeADDPotential treeADD;

	public RemoveStatesCheckBoxPanel(TreeADDBranch branch, TreeADDPotential treeADD) {
		this.branch = branch;
		this.treeADD = treeADD;
		initialize();
		repaint();
	}

	public void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		List<State> states = branch.getBranchStates();

		for (State state : states) {

			JCheckBox checkBox = new JCheckBox(state.getName());
			checkBoxes.add(checkBox);
			//checkBox.setAlignmentX((float) 0.5);
			add(checkBox, BorderLayout.CENTER);

		}
	}

	public TreeADDBranch getBranch() {
		return this.branch;
	}

	public TreeADDPotential getTreeADDPotential() {
		return this.treeADD;
	}

	public List<JCheckBox> getCheckBoxes() {
		return this.checkBoxes;
	}
}
