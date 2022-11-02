/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.plugin;

import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolPluginManager {
	private static ToolPluginManager instance = null;
	private PluginLoaderIF pluginsLoader;
	private Map<String, Class<?>> plugins;

	/**
	 * Constructor for ToolsMenuManager.
	 */
	private ToolPluginManager() {
		super();
		this.pluginsLoader = new PluginLoader();
		this.plugins = new HashMap<String, Class<?>>();
		for (Class<?> plugin : findAllToolPlugins()) {
			this.plugins.put(plugin.getAnnotation(ToolPlugin.class).command(), plugin);
		}
	}

	public static ToolPluginManager getInstance() {
		if (instance == null) {
			instance = new ToolPluginManager();
		}
		return instance;
	}

	/**
	 * Finds all learning tools menu items.
	 *
	 * @return a list of tools menu items.
	 */
	private final List<Class<?>> findAllToolPlugins() {
		try {
			FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy(ToolPlugin.class);
			return pluginsLoader.loadAllPlugins(filter);
		} catch (Exception e) {
		}
		return null;
	}

	public List<JMenuItem> getMenuItems() {
		List<JMenuItem> menuItems = new ArrayList<>();
		try {
			for (Class<?> plugin : plugins.values()) {
				ToolPlugin lAnnotation = plugin.getAnnotation(ToolPlugin.class);
				JMenuItem menuItem = new LocalizedMenuItem(lAnnotation.name(), lAnnotation.command());
				menuItems.add(menuItem);
			}
		} catch (Exception e) {
		}

		return menuItems;
	}

	public void processCommand(String command, JFrame parent) {
		try {
			Class<?> plugin = plugins.get(command);
			if (plugin == null) {
				JOptionPane.showMessageDialog(null,
						StringDatabase.getUniqueInstance().getString("Tools.Plugin.NotAvailable"),
						StringDatabase.getUniqueInstance().getString("ErrorWindow.Title.Label") + " - " + command,
						JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					plugin.getConstructor().newInstance();
				} catch (NoSuchMethodException e2) {
					plugin.getConstructor(JFrame.class).newInstance(parent);
				}
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null,
					StringDatabase.getUniqueInstance().getString("Tools.Plugin.Error") + command,
					StringDatabase.getUniqueInstance().getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}
}
