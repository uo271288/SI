/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.openmarkov.core.model.network.Node;

public interface TableMethods {

	int getPotentialIndex(int row, int column, Node node);

	int calculateFirstEditableRow(Node properties);

	int calculateLastEditableRow(Node properties);

}
