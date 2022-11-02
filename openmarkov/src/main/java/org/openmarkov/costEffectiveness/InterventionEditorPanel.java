/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.costEffectiveness;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.dialog.treeadd.TreeADDCellRenderer;
import org.openmarkov.gui.dialog.treeadd.TreeADDEditorPanel;

import java.awt.event.ActionListener;

@SuppressWarnings("serial") public class InterventionEditorPanel extends TreeADDEditorPanel implements ActionListener {

	public InterventionEditorPanel(TreeADDCellRenderer cellRenderer, Node node, boolean readOnly) {
		super(cellRenderer, node, readOnly);
	}

	public InterventionEditorPanel(TreeADDCellRenderer cellRenderer, Node node) {
		super(cellRenderer, node, true);
	}

}
