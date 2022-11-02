/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.openmarkov.gui.configuration.OpenMarkovPreferences;
import org.openmarkov.gui.localize.StringDatabase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * This class implements a file chooser dialog file to select OpenMarkov files.
 *
 * @author jmendoza
 * @author m.arias
 * @version 1.1 jlgozalo - set appropriate variables names and redo For loop to
 * use enhanced loop syntax
 */
public abstract class FileChooser extends JFileChooser {

	/**
	 * Static field representing the default file format
	 */
	public static final String DEFAULT_FILE_FORMAT = "OpenMarkov.0.2";
	/**
	 * Static field for serializable class.
	 */
	private static final long serialVersionUID = 9076351651764305920L;

	//CMI
	/**
	 * Directory where the dialog box searchs the files.
	 */
	private static String directoryPath = System.getProperty("user.home");
	//CMF

	/**
	 * Creates a new file chooser that starts in the current directory,
	 * filtering the files with the file filters.
	 */
	public FileChooser(boolean acceptAllfile) {
		setTextsInLocale();
		setAcceptAllFileFilterUsed(acceptAllfile);
		setCurrentDirectory(new File(directoryPath));
		rescanCurrentDirectory();
	}

	/**
	 * to fix the bug in JFileChooser to display text in different languages the
	 * text of the components must be set explicitly
	 */
	private void setTextsInLocale() {

		StringDatabase stringDb = StringDatabase.getUniqueInstance();

		UIManager.put("FileChooser.cancelButtonText", stringDb.getString("FileChooser.cancelButtonText"));
		UIManager.put("FileChooser.cancelButtonToolTipText", stringDb.getString("FileChooser.cancelButtonToolTipText"));
		UIManager.put("FileChooser.detailsViewActionLabelText",
				stringDb.getString("FileChooser.detailsViewActionLabelText"));
		UIManager.put("FileChooser.detailsViewButtonToolTipText",
				stringDb.getString("FileChooser.detailsViewButtonToolTipText"));
		UIManager.put("FileChooser.fileNameLabelText", stringDb.getString("FileChooser.fileNameLabelText"));
		UIManager.put("FileChooser.filesOfTypeLabelText", stringDb.getString("FileChooser.filesOfTypeLabelText"));
		UIManager.put("FileChooser.helpButtonText", stringDb.getString("FileChooser.helpButtonText"));
		UIManager.put("FileChooser.helpButtonToolTipText", stringDb.getString("FileChooser.helpButtonToolTipText"));
		UIManager.put("FileChooser.homeFolderToolTipText", stringDb.getString("FileChooser.homeFolderToolTipText"));
		UIManager.put("FileChooser.listViewActionLabelText", stringDb.getString("FileChooser.listViewActionLabelText"));
		UIManager.put("FileChooser.listViewButtonToolTipTextlist",
				stringDb.getString("FileChooser.newFolderToolTipText"));
		UIManager.put("FileChooser.lookInLabelText", stringDb.getString("FileChooser.lookInLabelText"));
		UIManager.put("FileChooser.newFolderActionLabelText",
				stringDb.getString("FileChooser.newFolderActionLabelText"));
		UIManager.put("FileChooser.newFolderToolTipText", stringDb.getString("FileChooser.newFolderToolTipText"));
		UIManager.put("FileChooser.openButtonTextOpen", stringDb.getString("FileChooser.openButtonTextOpen"));
		UIManager.put("FileChooser.openButtonToolTipText", stringDb.getString("FileChooser.openButtonToolTipText"));
		UIManager.put("FileChooser.refreshActionLabelText", stringDb.getString("FileChooser.refreshActionLabelText"));
		UIManager.put("FileChooser.saveButtonTextSave", stringDb.getString("FileChooser.saveButtonTextSave"));
		UIManager.put("FileChooser.saveButtonToolTipText", stringDb.getString("FileChooser.saveButtonToolTipText"));
		UIManager.put("FileChooser.upFolderToolTipText", stringDb.getString("FileChooser.upFolderToolTipText"));
		UIManager.put("FileChooser.updateButtonText", stringDb.getString("FileChooser.updateButtonText"));
		UIManager.put("FileChooser.updateButtonToolTipText", stringDb.getString("FileChooser.updateButtonToolTipText"));
		UIManager.put("FileChooser.viewMenuLabelText", stringDb.getString("FileChooser.viewMenuLabelText"));

	}

	//CMI
	//
	//	public void setFileFilter(String extension) {
	//		for(FileFilter filter : getChoosableFileFilters())
	//		{
	//			if(filter instanceof FileFilterBasic &&
	//					((FileFilterBasic)filter).getFilterExtension().equalsIgnoreCase(extension))
	//			{
	//				setFileFilter(filter);
	//			}
	//		}
	//	}

	/**
	 * Sets the file given by description
	 *
	 * @param description - description of the filter: "Elvira" or "OpenMarkov_version"
	 */
	public void setFileFilter(String description) {
		boolean isSet = false;
		for (FileFilter filter : getChoosableFileFilters()) {
			if (filter instanceof FileFilterAll && ((FileFilterAll) filter).getFileDescription().equals(description)) {
				setFileFilter(filter);
				isSet = true;
				break;
			}
		}
		// In case there is an outdated value in the register
		if (!isSet) {
			OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPENED_FORMAT, FileChooser.DEFAULT_FILE_FORMAT,
					OpenMarkovPreferences.OPENMARKOV_FORMATS);
			description = FileChooser.DEFAULT_FILE_FORMAT;
			for (FileFilter filter : getChoosableFileFilters()) {
				if (filter instanceof FileFilterAll && ((FileFilterAll) filter).getFileDescription()
						.equals(description)) {
					setFileFilter(filter);
					break;
				}
			}
		}
	}
	//CMF

	//CMI -- New method

	/**
	 * Extracts the version of a pgmx file and concatenate it to the String "OpenMarkov" for having the description of the file
	 *
	 * @return the format OpenMarkov.version of a pgmx file
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */

	public String getPgmxFileFormat() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(getSelectedFile());
		String version = doc.getDocumentElement().getAttribute("formatVersion");
		//Removing the last digit of the version
		version = version.substring(0, version.lastIndexOf('.'));
		return "OpenMarkov.".concat(version);

	}
	//CMF
}
