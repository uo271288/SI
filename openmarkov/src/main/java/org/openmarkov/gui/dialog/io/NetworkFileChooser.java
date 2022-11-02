/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.gui.configuration.OpenMarkovPreferences;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This class implements a file chooser dialog file to select OpenMarkov files.
 *
 * @author ibermejo
 */
@SuppressWarnings("serial") public class NetworkFileChooser extends FileChooser {
	/**
	 * Creates a new file chooser that starts in the current directory,
	 * filtering the files with the file filters.
	 *
	 * @param isOpening Indicates if the file chooser is for opening a file (isOpening=true) or for saving (isOpening=false)
	 */
	public NetworkFileChooser(boolean acceptAllfile, boolean isOpening) {
		super(acceptAllfile);
		FormatManager formatManager = FormatManager.getInstance();
		HashMap<String, String> parsersListForFilters = isOpening ?
				formatManager.getReaders() :
				formatManager.getWriters();
		//CMI List of extensions for opening a file
		List<String> extensionList = new ArrayList<String>();
		// for (String item : parsersListForFilters.keySet ())
		List<String> descriptions = new ArrayList<>();
		descriptions.addAll(parsersListForFilters.keySet());
		Collections.sort(descriptions);
		for (String item : descriptions)
		//CMF
		{
			//CMI
        	/*
        	addChoosableFileFilter (new FileFilterAll (parsersListForFilters.get (item), item));
        	*/
			String itemExtension = parsersListForFilters.get(item);

			addChoosableFileFilter(new FileFilterAll(itemExtension, item));

			//CMF
		}
		File currentDirectory = new File(OpenMarkovPreferences
				.get(OpenMarkovPreferences.LAST_OPEN_DIRECTORY, OpenMarkovPreferences.OPENMARKOV_DIRECTORIES, "."));
		setCurrentDirectory(currentDirectory);
		//CMI
        /*
        setFileFilter (OpenMarkovPreferences.get (OpenMarkovPreferences.LAST_OPENED_FORMAT,
                                                  OpenMarkovPreferences.OPENMARKOV_FORMATS, "pgmx"));
        */
		//CMI UNCLEAR Where is set pgmx? By default LAST_OPENED_FORMAT=pgmx

		if (isOpening) {
			setFileFilter("OpenMarkov");
		} else {
			setFileFilter(OpenMarkovPreferences
					.get(OpenMarkovPreferences.LAST_OPENED_FORMAT, OpenMarkovPreferences.OPENMARKOV_FORMATS,
							FileChooser.DEFAULT_FILE_FORMAT));
		}
		//CMF
	}

	public NetworkFileChooser() {
		this(false, true);
	}

	@Override public int showOpenDialog(Component parent) throws HeadlessException {
		int result = super.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPEN_DIRECTORY, getSelectedFile().getAbsolutePath(),
					OpenMarkovPreferences.OPENMARKOV_DIRECTORIES);
			// CMI
            /*
            OpenMarkovPreferences.set (OpenMarkovPreferences.LAST_OPENED_FORMAT,
                                       ((FileFilterBasic) getFileFilter ()).getFilterExtension (),
                                       OpenMarkovPreferences.OPENMARKOV_FORMATS);
            */
			try {
				OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPENED_FORMAT, getPgmxFileFormat(),
						OpenMarkovPreferences.OPENMARKOV_FORMATS);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			//CMF
		}
		return result;
	}

	@Override public int showSaveDialog(Component parent) throws HeadlessException {
		int result = super.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPEN_DIRECTORY, getSelectedFile().getAbsolutePath(),
					OpenMarkovPreferences.OPENMARKOV_DIRECTORIES);
			//CMI
            /*
            OpenMarkovPreferences.set (OpenMarkovPreferences.LAST_OPENED_FORMAT,
                                       ((FileFilterBasic) getFileFilter ()).getFilterExtension (),
                                       OpenMarkovPreferences.OPENMARKOV_FORMATS);
            */

			OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPENED_FORMAT,
					((FileFilterAll) getFileFilter()).getFileDescription(), OpenMarkovPreferences.OPENMARKOV_FORMATS);

			//CMF

		}
		return result;
	}

	@Override public void approveSelection() {
		if (getDialogType() == SAVE_DIALOG) {
			File selectedFile = getSelectedFile();
			if ((selectedFile != null) && selectedFile.exists()) {
				int response = JOptionPane.showConfirmDialog(this, "The file " + selectedFile.getName()
								+ " already exists. Do you want to replace the existing file?", "Ovewrite file",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (response != JOptionPane.YES_OPTION)
					return;
			}
		}
		super.approveSelection();
	}
}
