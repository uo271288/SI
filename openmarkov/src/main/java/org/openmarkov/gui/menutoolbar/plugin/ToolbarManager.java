/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.plugin;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.gui.localize.LocalizedException;
import org.openmarkov.gui.menutoolbar.toolbar.ToolBarBasic;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ToolbarManager {
	private MainPanel mainPanel;
	private Map<String, Class<?>> toolbarClasses;
	private List<String> activeToolbars = new ArrayList<>();

	public ToolbarManager(MainPanel mainPanel) {
		toolbarClasses = new HashMap<String, Class<?>>();
		this.mainPanel = mainPanel;

		for (Class<?> toolbarClass : findAllToolbars()) {
			Toolbar toolbar = toolbarClass.getAnnotation(Toolbar.class);
			toolbarClasses.put(toolbar.name(), toolbarClass);
		}
	}

	public Set<String> getToolbarNames() {
		return toolbarClasses.keySet();
	}

	public void addToolbar(String name) {
		ToolBarBasic instance = null;

		if (!activeToolbars.contains(name)) {
			if (toolbarClasses.containsKey(name)) {
				LocalizedException localizedException = null;
				try {
					Constructor<?> constructor = toolbarClasses.get(name).getConstructor(ActionListener.class);
					instance = (ToolBarBasic) constructor.newInstance(mainPanel.getMainPanelListenerAssistant());
				} catch (NoSuchMethodException e) {
					localizedException = new LocalizedException(new OpenMarkovException("NoSuchMethod", name), null);
				} catch (SecurityException e) {
					localizedException = new LocalizedException(new OpenMarkovException("Security"), null);
				} catch (InstantiationException e) {
					localizedException = new LocalizedException(new OpenMarkovException("Instantiation"), null);
				} catch (IllegalAccessException e) {
					localizedException = new LocalizedException(new OpenMarkovException("IllegalAccess"), null);
				} catch ( IllegalArgumentException e) {
					localizedException = new LocalizedException(new OpenMarkovException("IllegalArgument"), null);
				} catch (InvocationTargetException e) {
					localizedException = new LocalizedException(new OpenMarkovException("InvocationTarget"), null);
				} finally {
					if (localizedException != null) localizedException.showException();
				}
			}
			mainPanel.getToolBarPanel().add(instance);
		}
		activeToolbars.add(name);
	}

	/**
	 * This method gets all the plugins with Toolbar annotations
	 *
	 * @return a list with the plugins detected with Toolbar annotations.
	 */
	private final List<Class<?>> findAllToolbars() {
		PluginLoader pluginsLoader = new PluginLoader();
		try {
			FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy(Toolbar.class);
			return pluginsLoader.loadAllPlugins(filter);
		} catch (Exception e) {
			int i = 1;
		}
		return null;
	}
}
