/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.gui.configuration.OpenMarkovPreferences;

import javax.swing.*;
import java.awt.*;
import java.io.File;

@SuppressWarnings("serial") public class DBFileChooser extends FileChooser {
	protected static CaseDatabaseManager caseDbManager = new CaseDatabaseManager();

	public DBFileChooser(boolean acceptAllFiles) {
		super(acceptAllFiles);
		File currentDirectory = new File(OpenMarkovPreferences
				.get(OpenMarkovPreferences.LAST_OPEN_DB_DIRECTORY, OpenMarkovPreferences.OPENMARKOV_DIRECTORIES, "."));
		setCurrentDirectory(currentDirectory);
	}

	@Override public int showOpenDialog(Component parent) throws HeadlessException {
		int result = super.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPEN_DB_DIRECTORY, getSelectedFile().getAbsolutePath(),
					OpenMarkovPreferences.OPENMARKOV_DIRECTORIES);
		}
		return result;
	}

	@Override public int showSaveDialog(Component parent) throws HeadlessException {
		int result = super.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPEN_DB_DIRECTORY, getSelectedFile().getAbsolutePath(),
					OpenMarkovPreferences.OPENMARKOV_DIRECTORIES);
		}
		return result;
	}

}
