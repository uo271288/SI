/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
/**
 *
 */

package org.openmarkov.gui.dialog.configuration;

import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Table model for the Preferences Editor
 *
 * @author jlgozalo
 * @version 1.0 28 Aug 2009
 */
class PreferencesTableModel extends AbstractTableModel {
	/**
	 *
	 */
	private static final long serialVersionUID = 3278473793314149896L;
	/**
	 * String database
	 */
	protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	/**
	 * Preferences to work with
	 */
	Preferences pref;
	/**
	 * Previous preferences to be used in case of CANCEL actions
	 */
	Preferences prefSaved;
	/**
	 * Keys for the preferences
	 */
	String[] keys;

	/**
	 * constructor
	 *
	 * @param pref The preferences for the table model
	 */
	public PreferencesTableModel(Preferences pref) {
		this.pref = pref;
		this.prefSaved = pref;
		try {
			keys = pref.keys();
		} catch (BackingStoreException e) {
			System.out.println("Could not get keys for Preference node: " + pref.name());
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			keys = new String[0];
		}
	}

	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Key";
		case 1:
			return "Value";
		default:
			return "-";
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return false;
		case 1:
			return true;
		default:
			return false;
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		pref.put(keys[rowIndex], aValue.toString());
		this.syncSave();
	}

	public Object getValueAt(int row, int column) {
		String key = keys[row];
		if (column == 0)
			return key;
		Object value = pref.get(key, "(Unknown)");
		return value;
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return keys.length;
	}

	/**
	 * undo action
	 */
	public void undo() {
		pref = prefSaved;
		syncSave();
	}

	/**
	 * sync action
	 */
	public void syncSave() {
		try {
			pref.sync(); // make sure the backing store is synchronized
			// with latest update
		} catch (BackingStoreException e) {
			System.out.println("Error synchronizing backStore with updated values");
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * reset action on the user preferences
	 */
	public void reset() {
		try {
			pref.removeNode(); // removing preferences
		} catch (BackingStoreException e) {
			System.out.println("Error synchronizing backStore with reset value");
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
		}
	}
}
