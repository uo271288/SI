/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.dt;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.window.mdi.FrameContentPanel;

import java.awt.*;

@SuppressWarnings("serial") public class DecisionTreeWindow extends FrameContentPanel {
	private String title = null;
	private DecisionTreePanel decisionTreePanel = null;

	public DecisionTreeWindow(ProbNet probNet) {
		setLayout(new BorderLayout());
		title = probNet.getName() + "- decision tree";
		try {
			decisionTreePanel = new DecisionTreePanel(probNet);
		} catch (NotEvaluableNetworkException e) {
			e.printStackTrace();
		}
		add(decisionTreePanel, BorderLayout.CENTER);
		setBackground(Color.blue);
	}

	@Override public String getTitle() {
		return title;
	}

	@Override public void close() {
		// TODO Auto-generated method stub
	}

	@Override public double getZoom() {
		return decisionTreePanel.getZoom();
	}

	@Override public void setZoom(double zoom) {
		decisionTreePanel.setZoom(zoom);
	}

}
