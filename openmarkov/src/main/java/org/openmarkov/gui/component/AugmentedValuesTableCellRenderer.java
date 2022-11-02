/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

/**
 * This class is used for painting and coloring the table and the headers
 *
 * @author carmenyago
 * @version 1.0 Apr/2017
 */
public class AugmentedValuesTableCellRenderer extends ValuesTableCellRenderer {
	/**
	 * default serial ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * constructor for the renderer
	 *
	 * @param firstEditableRow value of the first editable row
	 */

	public AugmentedValuesTableCellRenderer(int firstEditableRow) {
		super(firstEditableRow, null);
	}

}
