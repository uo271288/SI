/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.common;

import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.gui.component.ValuesTableCellRenderer;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;

/**
 * Renderer for the Multi criteria table
 *
 * @author Jorge
 */
public class MultiCriteriaComboBoxRenderer extends ValuesTableCellRenderer {

	public static final int USE_RENDERER = 0;
	public static final int DISCOUNT_UNIT_RENDERER = 1;
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 7294499626818840525L;
	private int rendererType;

	public MultiCriteriaComboBoxRenderer(int rendererType) {
		super(1);
		this.rendererType = rendererType;
	}

	@SuppressWarnings("unchecked") @Override public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (rendererType == USE_RENDERER) {
			if (row == 0) {
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			} else {
				JComboBox<String> comboBox = new JComboBox<String>();
				//				comboBox.addItem(Criterion.CECriterion.Null.toString());
				comboBox.addItem(Criterion.CECriterion.Cost.toString());
				comboBox.addItem(Criterion.CECriterion.Effectiveness.toString());

				if (value.getClass().equals(JComboBox.class)) {
					comboBox.setSelectedItem(((JComboBox<String>) value).getSelectedItem());
				} else if (value.getClass().equals(String.class)) {
					comboBox.setSelectedItem((String) value);
				}

				return comboBox;
			}
		} else if (rendererType == DISCOUNT_UNIT_RENDERER) {
			if (row == 0) {
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			} else {
				JComboBox<String> comboBox = new JComboBox<String>();
				for (CycleLength.DiscountUnit unit : CycleLength.DiscountUnit.values()) {
					String newUnit = StringDatabase.getUniqueInstance()
							.getString("NetworkAdvancedPanel.TemporalOptions.DiscountUnit." + unit.toString());
					comboBox.addItem(newUnit);
				}

				if (value.getClass().equals(JComboBox.class)) {
					String selectedItem = ((JComboBox<String>) value).getSelectedItem().toString();
					comboBox.setSelectedItem(selectedItem);
				} else if (value.getClass().equals(String.class)) {
					comboBox.setSelectedItem((String) value);
				}

				return comboBox;
			}
		} else {
			return null;
		}

	}

}
