/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.configuration;

public enum OperatingSystem {

	WINDOWS(0, "Windows"), LINUX(1, "Linux"), OTHER(2, "Other");

	private int value;

	private String name;

	OperatingSystem(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public int value() {
		return value;
	}

	public String toString() {
		return name;
	}

}
