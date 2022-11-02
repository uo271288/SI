/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial") public class ValuesTableOptimalPolicyCellRenderer extends ValuesTableCellRenderer {

	private Color cellColor;

	public ValuesTableOptimalPolicyCellRenderer(int firstEditableRow, boolean[] uncertaintyInColumns,
			boolean colorGreen) {
		super(firstEditableRow, uncertaintyInColumns);
		if (!colorGreen) {
			cellColor = new java.awt.Color(255, 72, 72);
		} else {
			cellColor = new java.awt.Color(80, 220, 95);
		}
	}

	@Override protected void setCellColors(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		super.setCellColors(table, value, isSelected, hasFocus, row, column);
		Color color = cellColor; //new java.awt.Color (255, 72, 72);
		if (column >= ValuesTable.FIRST_EDITABLE_COLUMN && ValuesTable.FIRST_EDITABLE_COLUMN >= 0
				&& row >= firstEditableRow && value instanceof Double) {
			boolean isMax = true;
			double doubleValue = (double) value;
			// Change color if this cell contains optimal policy, i.e. max value
			for (int i = firstEditableRow; i < table.getRowCount(); i++) {
				if (i != row) {
					isMax &= doubleValue > (double) table.getValueAt(i, column);
				}
			}
			if (isMax) {
				setBackground(color);
			}
		}
	}
}
