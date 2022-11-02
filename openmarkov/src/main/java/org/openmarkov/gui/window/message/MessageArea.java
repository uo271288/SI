/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.message;

/**
 * This interface must be implemented by the classes that represent a message
 * area.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo - suppressing the public modifier of the methods (not
 * rquired in an interface)
 */
interface MessageArea {

	/**
	 * Writes an information message in the text area.
	 *
	 * @param message text to write.
	 */
	void writeInformationMessage(String message);

	/**
	 * Writes an error message in the text area.
	 *
	 * @param message text to write.
	 */
	void writeErrorMessage(String message);
}
