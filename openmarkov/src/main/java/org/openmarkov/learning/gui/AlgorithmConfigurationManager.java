/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.gui;

import org.openmarkov.learning.core.algorithm.LearningAlgorithmType;
import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;

import javax.swing.*;
import java.lang.annotation.AnnotationFormatError;
import java.util.HashMap;
import java.util.List;

public class AlgorithmConfigurationManager {
	private PluginLoaderIF pluginsLoader;
	private HashMap<String, Class<? extends AlgorithmParametersDialog>> algorithmConfigurations;
	private JFrame parent = null;

	/**
	 * Constructor for AlgorithmConfigurationManager.
	 */
	@SuppressWarnings("unchecked") public AlgorithmConfigurationManager(JFrame parent) {
		super();
		this.parent = parent;
		this.pluginsLoader = new PluginLoader();
		algorithmConfigurations = new HashMap<String, Class<? extends AlgorithmParametersDialog>>();

		for (Class<?> plugin : findAllAlgorithmsConfigurations()) {
			AlgorithmConfiguration lAnnotation = plugin.getAnnotation(AlgorithmConfiguration.class);
			if (AlgorithmParametersDialog.class.isAssignableFrom(plugin)) {
				LearningAlgorithmType learningAlgorithmType = lAnnotation.algorithm()
						.getAnnotation(LearningAlgorithmType.class);
				algorithmConfigurations
						.put(learningAlgorithmType.name(), (Class<? extends AlgorithmParametersDialog>) plugin);
			} else {
				throw new AnnotationFormatError(
						"AlgorithmConfiguration annotation must be in a class that extends AlgorithmParametersDialog");
			}
		}
	}

	/**
	 * Returns a learning algorithm by name.
	 *
	 * @param algorithmName the algorithm name.
	 * @return a learning algorithm.
	 */
	public final AlgorithmParametersDialog getByName(String algorithmName) {
		AlgorithmParametersDialog instance = null;
		try {
			Class<? extends AlgorithmParametersDialog> algorithmOptionsGUIClass = algorithmConfigurations
					.get(algorithmName);
			if (algorithmOptionsGUIClass != null) {
				instance = algorithmOptionsGUIClass.getConstructor(JFrame.class, boolean.class)
						.newInstance(parent, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	/**
	 * Finds all learning algorithms.
	 *
	 * @return a list of learning algorithms.
	 */
	private final List<Class<?>> findAllAlgorithmsConfigurations() {
		try {
			FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy(AlgorithmConfiguration.class);
			return pluginsLoader.loadAllPlugins(filter);
		} catch (Exception e) {
		}
		return null;
	}

}
