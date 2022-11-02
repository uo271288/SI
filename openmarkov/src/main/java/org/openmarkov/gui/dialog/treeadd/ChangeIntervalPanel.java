/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

import javax.swing.*;

/**
 * @author myebra
 */
@SuppressWarnings("serial") public class ChangeIntervalPanel extends JPanel {
	final String[] intervalLowerSymbols = new String[] { "[", "(" };
	final String[] intervalUpperSymbols = new String[] { "]", ")" };
	private JTextField minText;
	private JTextField maxText;
	private Threshold min;
	private Threshold max;
	private JComboBox<String> jComboBoxUpperSymbol;
	private JComboBox<String> jComboBoxLowerSymbol;

	public ChangeIntervalPanel(TreeADDBranch treeBranch) {
		min = treeBranch.getLowerBound();
		max = treeBranch.getUpperBound();

		Float minDomainLimit = (float) treeBranch.getRootVariable().getPartitionedInterval().getMin();
		Float maxDomainLimit = (float) treeBranch.getRootVariable().getPartitionedInterval().getMax();

		boolean isLeftClosed = treeBranch.getRootVariable().getPartitionedInterval().isLeftClosed(); // true -> [) 
		boolean isRightClosed = treeBranch.getRootVariable().getPartitionedInterval().isRightClosed();
		boolean minBelongsToLeftDomain = !isLeftClosed;
		boolean maxBelongsToLeftDomain = isRightClosed;

		minText = new JTextField(String.valueOf(min.getLimit()));
		minText.setBounds(68, 64, 86, 20);
		minText.setColumns(10);

		maxText = new JTextField(String.valueOf(max.getLimit()));
		maxText.setBounds(189, 64, 86, 20);
		maxText.setColumns(10);

		jComboBoxLowerSymbol = new JComboBox<String>(intervalLowerSymbols);
		if (min.belongsToLeft()) {// "(" intervalLowerSymbols [1]
			jComboBoxLowerSymbol.setSelectedItem(intervalLowerSymbols[1]);
		} else if (!min.belongsToLeft()) {// "[" intervalLowerSymbols [0]
			jComboBoxLowerSymbol.setSelectedItem(intervalLowerSymbols[0]);
		}
		jComboBoxLowerSymbol.setBounds(10, 64, 48, 20);

		jComboBoxUpperSymbol = new JComboBox<String>(intervalUpperSymbols);
		if (max.belongsToLeft()) {// "]" intervalLowerSymbols [0]
			jComboBoxUpperSymbol.setSelectedItem(intervalUpperSymbols[0]);
		} else if (!max.belongsToLeft()) {// ")" intervalLowerSymbols [1]
			jComboBoxUpperSymbol.setSelectedItem(intervalUpperSymbols[1]);
		}
		jComboBoxUpperSymbol.setBounds(285, 64, 48, 20);
		setLayout(null);

		if (min.getLimit() == minDomainLimit && min.belongsToLeft() == minBelongsToLeftDomain) {
			minText.setEditable(false);
			jComboBoxLowerSymbol.setEnabled(false);
		}
		if (max.getLimit() == maxDomainLimit && max.belongsToLeft() == maxBelongsToLeftDomain) {
			maxText.setEditable(false);
			jComboBoxUpperSymbol.setEnabled(false);
		}

		JLabel lblNewLabel = new JLabel("   ,   ");
		lblNewLabel.setBounds(160, 64, 22, 20);
		//add(lblNewLabel);
		add(jComboBoxLowerSymbol);
		add(minText);
		add(lblNewLabel);
		add(maxText);
		add(jComboBoxUpperSymbol);
	}

	public JTextField getMin() {
		return minText;
	}

	public JTextField getMax() {
		return maxText;
	}

	public JComboBox<String> minBelongsToLeft() {
		return jComboBoxLowerSymbol;
	}

	public JComboBox<String> maxBelongsToLeft() {
		return jComboBoxUpperSymbol;
	}
}
