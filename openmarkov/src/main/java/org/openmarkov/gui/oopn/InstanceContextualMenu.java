/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.oopn;

import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.localize.MenuLocalizer;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuItemNames;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenu;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * This class implements a contextual menu that is displays when the user right-clicks on
 * an instance.
 *
 * @author ibermejo
 * @version 1.0
 */
@SuppressWarnings("serial") public class InstanceContextualMenu extends ContextualMenu {

	/**
	 * Object that represents the item 'Remove'.
	 */
	private JMenuItem removeMenuItem = null;

	/**
	 * Object that represents the item 'Mark as Input'.
	 */
	private JMenuItem inputMenuItem = null;

	/**
	 * Object that represents the item 'Edit Class'.
	 */
	private JMenuItem editClassMenuItem = null;

	/**
	 * Object that represents the item 'Edit Instance Name'.
	 */
	private JMenuItem editInstanceNameItem = null;

	/**
	 * Object that represents the item 'Arity'
	 */
	private JMenu arityMenuItem = null;

	/**
	 * Object used to make autoexclusive the menu item to select the arity.
	 */
	private ButtonGroup arityGroup = new ButtonGroup();

	/**
	 * This constructor creates a new instance.
	 *
	 * @param newListener object that listens to the menu events.
	 */
	public InstanceContextualMenu(ActionListener newListener) {

		super(newListener);

		initialize();

	}

	/**
	 * This method initialises this instance.
	 */
	private void initialize() {

		add(getEditInstanceNameMenuItem());
		add(getEditClassMenuItem());
		add(getRemoveMenuItem());
		// addSeparator();
		// add(getPropertiesMenuItem());
		add(getInputMenuItem());
		addSeparator();
		add(getArityMenuItem());
	}

	/**
	 * This method initialises removeMenuItem.
	 *
	 * @return a new 'Remove' menu item.
	 */
	private JMenuItem getRemoveMenuItem() {

		if (removeMenuItem == null) {
			removeMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_REMOVE_MENUITEM, ActionCommands.OBJECT_REMOVAL);
			removeMenuItem.addActionListener(listener);
		}

		return removeMenuItem;

	}

	/**
	 * This method initialises inputMenuItem.
	 *
	 * @return a new 'Input' menu item.
	 */
	private JMenuItem getInputMenuItem() {

		if (inputMenuItem == null) {
			inputMenuItem = new JCheckBoxMenuItem(MenuLocalizer.getLabel(MenuItemNames.EDIT_MARKASINPUT_MENUITEM));
			inputMenuItem.setActionCommand(ActionCommands.MARK_AS_INPUT);
			inputMenuItem.addActionListener(listener);
		}

		return inputMenuItem;
	}

	/**
	 * This method initialises inputMenuItem.
	 *
	 * @return a new 'Edit Class' menu item.
	 */
	private JMenuItem getEditClassMenuItem() {

		if (editClassMenuItem == null) {
			editClassMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_CLASS_MENUITEM, ActionCommands.EDIT_CLASS);
			editClassMenuItem.addActionListener(listener);
		}

		return editClassMenuItem;
	}

	/**
	 * This method initialises inputMenuItem.
	 *
	 * @return a new 'Edit Instance name' menu item.
	 */
	private JMenuItem getEditInstanceNameMenuItem() {

		if (editInstanceNameItem == null) {
			editInstanceNameItem = new LocalizedMenuItem(MenuItemNames.EDIT_INSTANCE_NAME_MENUITEM,
					ActionCommands.EDIT_INSTANCE_NAME);
			editInstanceNameItem.addActionListener(listener);
		}

		return editInstanceNameItem;
	}

	/**
	 * This method initialises arityMenuItem.
	 *
	 * @return a new 'Arity' menu item.
	 */
	private JMenu getArityMenuItem() {
		if (arityMenuItem == null) {
			arityMenuItem = new JMenu(MenuLocalizer.getLabel(MenuItemNames.ARITY_MENUITEM));
			arityMenuItem.setActionCommand(ActionCommands.SET_ARITY);
			arityMenuItem.addActionListener(listener);

			JCheckBoxMenuItem setArityOneMenuItem = new JCheckBoxMenuItem("1");
			setArityOneMenuItem.setActionCommand(ActionCommands.SET_ARITY_ONE);
			setArityOneMenuItem.addActionListener(listener);
			arityMenuItem.add(setArityOneMenuItem);
			arityGroup.add(setArityOneMenuItem);

			JCheckBoxMenuItem setArityManyMenuItem = new JCheckBoxMenuItem("*");
			setArityManyMenuItem.setActionCommand(ActionCommands.SET_ARITY_MANY);
			setArityManyMenuItem.addActionListener(listener);
			arityMenuItem.add(setArityManyMenuItem);
			arityGroup.add(setArityManyMenuItem);

			setArityOneMenuItem.setSelected(true);

		}

		return arityMenuItem;
	}

	/**
	 * Returns the component that corresponds to an action command.
	 *
	 * @param actionCommand action command that identifies the component.
	 * @return a components identified by the action command.
	 */
	@Override protected JComponent getJComponentActionCommand(String actionCommand) {

		JComponent component = null;

		if (actionCommand.equals(ActionCommands.OBJECT_REMOVAL)) {
			component = removeMenuItem;
		} else if (actionCommand.equals(ActionCommands.MARK_AS_INPUT)) {
			component = inputMenuItem;
		} else if (actionCommand.equals(ActionCommands.EDIT_CLASS)) {
			component = editClassMenuItem;
		} else if (actionCommand.equals(ActionCommands.EDIT_INSTANCE_NAME)) {
			component = editInstanceNameItem;
		}

		return component;

	}

}
