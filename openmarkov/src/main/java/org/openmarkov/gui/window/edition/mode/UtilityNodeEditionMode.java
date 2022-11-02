/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.mode;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.window.edition.EditorPanel;

@EditionState(name = "Edit.Mode.Utility", icon = "utility.gif", cursor = "utility.gif") public class UtilityNodeEditionMode
		extends NodeEditionMode {

	public UtilityNodeEditionMode(EditorPanel editorPanel, ProbNet probNet) {
		super(editorPanel, probNet, NodeType.UTILITY);
	}
}
