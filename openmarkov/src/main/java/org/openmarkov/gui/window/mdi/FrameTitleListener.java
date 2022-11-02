/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.mdi;

import javax.swing.*;

/**
 * This interface enables the listener to know the when the title of the frame
 * has changed.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo - remove public identifier to methods (not required in
 * an interface definition)
 */
interface FrameTitleListener {

	/**
	 * This method executes when the title of a frame has changed.
	 *
	 * @param frame frame whose title has been changed.
	 */
	void titleChanged(JInternalFrame frame, String oldTitle, String newTitle);
}
