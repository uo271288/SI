/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.localize;

import org.openmarkov.gui.loader.element.IconLoader;

import javax.swing.*;

@SuppressWarnings("serial") public class LocalizedMenuItem extends JMenuItem {
	/**
	 * Icon loader.
	 */
	private static IconLoader iconLoader = new IconLoader();

	public LocalizedMenuItem(String name) {
		this.setName(name);
		this.setText(MenuLocalizer.getLabel(name));
		this.setMnemonic(MenuLocalizer.getMnemonic(name).charAt(0));
	}

	public LocalizedMenuItem(String name, String actionCommand) {
		this(name);
		this.setActionCommand(actionCommand);
	}

	public LocalizedMenuItem(String name, String actionCommand, String iconName) {
		this(name, actionCommand);
		this.setIcon(iconLoader.load(iconName));
	}

	public LocalizedMenuItem(String name, String actionCommand, String iconName, KeyStroke keyStroke) {
		this(name, actionCommand, iconName);
		this.setAccelerator(keyStroke);
	}

}
