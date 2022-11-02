/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial") public class ValuesTableWithLinkRestrictionCellRenderer extends ValuesTableCellRenderer {

	private static Color INCOMPATIBILITY_COLOR = new Color(255, 122, 122);

	public ValuesTableWithLinkRestrictionCellRenderer(int firstEditableRow, boolean[] uncertaintyInColumns) {
		super(firstEditableRow, uncertaintyInColumns);
	}

	@Override protected void setCellColors(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		super.setCellColors(table, value, isSelected, hasFocus, row, column);
		if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) && firstEditableRow >= 0 && (row >= firstEditableRow)) {
			try {
				if (!table.isCellEditable(row, column)) {
					setBackground(INCOMPATIBILITY_COLOR);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
			}
		}
	}

}
