/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.localize;

/**
 * Wrapper class for GUI localization
 *
 * @author Iñigo
 */
public class MenuLocalizer {

	/**
	 * Suffix that has label string resources.
	 */
	private final static String LABEL_SUFFIX = ".Label";
	/**
	 * Suffix that has mnemonic string resources.
	 */
	private final static String MNEMONIC_SUFFIX = ".Mnemonic";
	/**
	 * String resource.
	 */
	private static StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

	public static String getString(String stringId) {
		return stringDatabase.getString(stringId);
	}

	public static String getLabel(String stringId) {
		return stringDatabase.getString(stringId + LABEL_SUFFIX);
	}

	public static String getMnemonic(String stringId) {
		return stringDatabase.getString(stringId + MNEMONIC_SUFFIX);
	}

}
