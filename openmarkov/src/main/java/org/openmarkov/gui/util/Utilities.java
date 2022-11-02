/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This class implements various methods that are used by the rest of classes of
 * the application.
 *
 * @author jmendoza
 * @version 1.2 jlgozalo - 10/05/10 - set private constructor, remove functions
 * and fix warnings
 */
public class Utilities {

	/**
	 * private constructor for a class with only static methods
	 */
	private Utilities() {

	}

	/**
	 * Returns the window that owns the component.
	 *
	 * @param component component whose top level window will be returned.
	 * @return the top level ancestor of the component, if it exists and it is a
	 * Window instance, of null if it isn't a window instance.
	 */
	public static Window getOwner(JComponent component) {

		Container ancestor = component.getTopLevelAncestor();

		if (ancestor == null) {

			return null;
		} else if (ancestor instanceof Window) {
			return (Window) ancestor;
		} else {
			return null;
		}

	}

	/**
	 * Checks if the mouse event hasn't key modifiers.
	 *
	 * @param e mouse event information.
	 * @return true if the mouse event hasn't modifiers; otherwise, false.
	 */
	public static boolean noMouseModifiers(MouseEvent e) {

		return ((e.getModifiers() & 0xF) == 0);

	}

}
