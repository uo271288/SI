/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.dt;

import javax.swing.plaf.basic.BasicTreeUI;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DecisionTreeUI extends BasicTreeUI {

	protected java.awt.event.MouseListener createMouseListener() {
		return new ZoomMouseListener(super.createMouseListener());
	}

	public class ZoomMouseListener extends MouseAdapter {

		MouseListener parentMouseListener;

		public ZoomMouseListener(MouseListener ml) {
			parentMouseListener = ml;
		}

		@Override public void mousePressed(MouseEvent e) {
			Double zoom = ((DecisionTree) tree).getZoom();
			int newX = (int) (e.getX() / zoom);
			int newY = (int) (e.getY() / zoom);
			e.translatePoint(newX - e.getX(), newY - e.getY());
			parentMouseListener.mousePressed(e);
		}
	}
}
