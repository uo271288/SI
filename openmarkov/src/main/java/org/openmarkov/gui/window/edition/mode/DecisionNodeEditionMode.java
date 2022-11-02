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

@EditionState(name = "Edit.Mode.Decision", icon = "decision.gif", cursor = "decision.gif") public class DecisionNodeEditionMode
		extends NodeEditionMode {

	public DecisionNodeEditionMode(EditorPanel editorPanel, ProbNet probNet) {
		super(editorPanel, probNet, NodeType.DECISION);
	}
}
