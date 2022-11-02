/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.CycleLengthEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.CycleLength.Unit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.dialog.common.OkCancelHorizontalDialog;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;

public class NetworkTemporalOptionsDialog extends OkCancelHorizontalDialog {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 5179498063445726852L;

	private ProbNet probNet;

	private JComboBox<String> temporalUnits;

	private JTextField unitScale;

	private Unit probNetUnit;

	private double probNetScale;

	private JPanel componentsPanel;

	public NetworkTemporalOptionsDialog(Window owner, ProbNet probNet) {
		super(owner);
		this.probNet = probNet;
		initialize();

		CycleLength temporalUnit;
		if (probNet.getCycleLength() != null) {
			temporalUnit = probNet.getCycleLength();
		} else {
			temporalUnit = new CycleLength();
			probNet.setCycleLength(temporalUnit);
		}

		probNetUnit = temporalUnit.getUnit();
		probNetScale = temporalUnit.getValue();

		temporalUnits.setSelectedItem(
				stringDatabase.getString("NetworkAdvancedPanel.TemporalOptions.Unit." + probNetUnit.toString()));
		unitScale.setText(String.valueOf(probNetScale));

		setName("TemporalOptionsDialog");
		setLocationRelativeTo(owner);
		pack();
	}

	/**
	 * This method configures the dialog box.
	 */
	private void initialize() {

		setTitle(StringDatabase.getUniqueInstance().getString("NetworkAdvancedPanel.TemporalOptions.Title"));
		getComponentsPanel();
		pack();
	}

	/**
	 * This method initialises componentsPanel.
	 *
	 * @return a new components panel.
	 */
	protected JPanel getComponentsPanel() {

		if (componentsPanel == null) {
			componentsPanel = new JPanel();
			JLabel label = new JLabel(
					StringDatabase.getUniqueInstance().getString("NetworkAdvancedPanel.TemporalOptions.Label"));
			componentsPanel.add(label);
			componentsPanel.add(getUnitScale());
			componentsPanel.add(getTemporalUnits());
		}

		return componentsPanel;

	}

	private Component getTemporalUnits() {
		if (temporalUnits == null) {
			temporalUnits = new JComboBox<String>();
			for (Unit unit : Unit.values()) {
				String newUnit = StringDatabase.getUniqueInstance()
						.getString("NetworkAdvancedPanel.TemporalOptions.Unit." + unit.toString());
				temporalUnits.addItem(newUnit);
			}

		}
		return temporalUnits;
	}

	private Component getUnitScale() {
		if (unitScale == null) {
			unitScale = new JTextField();

		}
		return unitScale;
	}

	/**
	 * This method carries out the actions when the user press the Ok button
	 * before hide the dialog.
	 *
	 * @return true if the dialog box can be closed.
	 */
	protected boolean doOkClickBeforeHide() {
		double newScale;
		try {
			newScale = Double.parseDouble(unitScale.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, stringDatabase.getString("NumberFormatException.Text.Label"),
					stringDatabase.getString("NumberFormatException.Title.Label"), JOptionPane.ERROR_MESSAGE);
			return false;
		}

		String selectedUnitString = temporalUnits.getSelectedItem().toString();
		String unitDataBaseString = StringDatabase.getUniqueInstance()
				.getString("NetworkAdvancedPanel.TemporalOptions.Unit." + probNetUnit.toString());

		if (newScale != probNetScale || !unitDataBaseString.equals(selectedUnitString)) {

			for (Unit unit : Unit.values()) {
				if (StringDatabase.getUniqueInstance()
						.getString("NetworkAdvancedPanel.TemporalOptions.Unit." + unit.toString())
						.equals(selectedUnitString)) {
					probNetUnit = unit;
					break;
				}
			}
			CycleLength temporalUnit = new CycleLength(probNetUnit, newScale);
			CycleLengthEdit edit = new CycleLengthEdit(probNet, temporalUnit);
			try {
				probNet.getPNESupport().doEdit(edit);
			} catch (DoEditException | NonProjectablePotentialException | WrongCriterionException e) {
				e.printStackTrace();
			}
			return super.doOkClickBeforeHide();
		}

		return true;
	}

	/**
	 * This method carries out the actions when the user press the Cancel button
	 * before hide the dialog.
	 */
	@Override protected void doCancelClickBeforeHide() {
		super.doCancelClickBeforeHide();
	}
}
