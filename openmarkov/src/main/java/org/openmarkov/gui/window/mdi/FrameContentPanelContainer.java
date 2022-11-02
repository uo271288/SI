/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.mdi;

/**
 * This interface must be implemented by the classes that are containers of a
 * panel that can't know in which type of class are contained. They only know
 * that their container has a title.
 *
 * @author jmendoza
 * @version 1.0
 */
public interface FrameContentPanelContainer {

	/**
	 * Returns the title of the container.
	 *
	 * @return a string containing the container's title.
	 */
	String getTitle();

	/**
	 * Sets the container's title.
	 *
	 * @param title new title (may have a null value).
	 */
	void setTitle(String title);
}
