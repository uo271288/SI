/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition;

/**
 * This interface is used to communicate the changes of size of a network panel.
 * Normally, a JScrollPane will implement it to scroll when the panel extends
 * itself.
 *
 * @author jmendoza
 * @version 1.0
 */
interface EditorPanelSizeListener {

	/**
	 * Notifies the increases of size of the panel.
	 *
	 * @param incrLeft   increase for the left side.
	 * @param incrTop    increase overhead.
	 * @param incrRight  increase for the right side.
	 * @param incrBottom increase for below.
	 */
	void sizeChanged(double incrLeft, double incrTop, double incrRight, double incrBottom);
}
