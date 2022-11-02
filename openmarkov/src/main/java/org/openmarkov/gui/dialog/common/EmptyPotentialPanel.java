/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.model.network.Node;

import java.awt.*;

@SuppressWarnings("serial") @PotentialPanelPlugin(potentialType = "Uniform") public class EmptyPotentialPanel
		extends PotentialPanel {
	public EmptyPotentialPanel(Node node) {
		setLayout(new BorderLayout());
	}

	@Override public void setData(Node node) {
		// TODO Auto-generated method stub

	}

	@Override public void close() {
		// TODO Auto-generated method stub

	}
}
