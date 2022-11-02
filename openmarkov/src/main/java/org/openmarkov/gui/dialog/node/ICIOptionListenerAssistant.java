
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.gui.dialog.common.CPTablePanel;
import org.openmarkov.gui.dialog.common.ICIPotentialsTablePanel;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

/**
 * This class is an event manager to detect selected buttons in ICIOptionsPanel
 * to change from ICIPotentialsTablePanel ICIOptionsPanel starts with Canonical
 * JRadiobutton selected (it is shown in ICIPotentialsTablePanel), then when it
 * is selected CPT JRadiobutton generates a CPTablePanel to get the JScrollPane
 * that is responsible to display the complete parameters table values.
 *
 * @author myebra
 */
public class ICIOptionListenerAssistant implements ItemListener {
	private static int CANONICAL = 0;
	private static int TPC = 1;
	/**
	 * Identifies the radio button affected by the event.
	 */
	private int previousModel = -1;
	private ICIOptionsPanel iciOptionPanel;
	private Container parentPanel;
	/**
	 * original node of the ICIOptionPanel
	 */
	private CPTablePanel cpTablePanel;
	private JScrollPane iciValuesTablePanel = null;

	public ICIOptionListenerAssistant(ICIOptionsPanel iciOptionPanel) {
		this.iciOptionPanel = iciOptionPanel;
	}

	public Node getNodeParentPanel() {
		return ((ICIPotentialsTablePanel) parentPanel).getNode();
	}

	public void itemStateChanged(ItemEvent e) {
		// to identify what is the panel container it could be CPTTablePanel or
		// ICIPotentialsTablePanel
		this.parentPanel = iciOptionPanel.getParent();
		if (e.getItem().equals(iciOptionPanel.getJRadioButtonTPC())) {
			itemStateChangedTPC(e);
		}
		if (e.getItem().equals(iciOptionPanel.getJRadioButtonCanonical())) {
			itemStateChangedCanonical(e);
		}
	}

	private void itemStateChangedCanonical(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			// has been deselected canonical
			previousModel = CANONICAL;
		} else if (e.getStateChange() == ItemEvent.SELECTED) {
		}
		if (previousModel == CANONICAL) {
			// do nothing
		} else if (previousModel == TPC) { // tpc --&gt; Canonical
			for (Component component : parentPanel.getComponents()) {
				if (component instanceof ICIOptionsPanel) {
					continue;
				}
				component.setVisible(false);
			}
			parentPanel.validate();
			// cpTablePanel.setVisible(false);
			parentPanel.repaint();
			// parentPanel.add(iciValuesTablePanel, BorderLayout.CENTER);
			iciValuesTablePanel.setVisible(true);
			parentPanel.repaint();
		}
	}

	private void itemStateChangedTPC(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			// has been deselected tpc
			previousModel = TPC;
		} else if (e.getStateChange() == ItemEvent.SELECTED) {
		}
		if (previousModel == CANONICAL) { // Canonical --&gt; tpc
			// show TPC do not allow edit
			// Copy of the parents panel node
			Node iciNode = new Node(((ICIPotentialsTablePanel) parentPanel).getNode());
			ICIPotential iciPotential = (ICIPotential) iciNode.getPotentials().get(0);
			TablePotential tablePotential;
			try {
				tablePotential = (TablePotential) iciPotential.getCPT();
				ArrayList<Potential> potentials = new ArrayList<Potential>();
				potentials.add(tablePotential);
				iciNode.setPotentials(potentials);
			} catch (NonProjectablePotentialException | WrongCriterionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, StringDatabase.getUniqueInstance().getString(e1.getMessage()),
						StringDatabase.getUniqueInstance().getString(e1.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
			this.cpTablePanel = new CPTablePanel(iciNode);
			JScrollPane cptValuesTablePanel = cpTablePanel.getValuesTableScrollPane();
			ICIPotentialsTablePanel iciPotentialTablePanel = (ICIPotentialsTablePanel) parentPanel;
			this.iciValuesTablePanel = iciPotentialTablePanel.getValuesTableScrollPane();
			for (Component component : parentPanel.getComponents()) {
				if (component instanceof ICIOptionsPanel) {
					continue;
				}
				component.setVisible(false);
			}
			parentPanel.repaint();
			parentPanel.validate();
			parentPanel.add(cptValuesTablePanel, BorderLayout.CENTER);
			parentPanel.repaint();
			parentPanel.validate();
		} else if (previousModel == TPC) {
			// do nothing
		}
	}
}