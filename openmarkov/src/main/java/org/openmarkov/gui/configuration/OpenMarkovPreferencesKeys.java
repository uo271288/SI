/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.configuration;

/**
 * Keys for the OpenMarkov Preferences
 *
 * @author jlgozalo
 * @version 1.0
 * 30 Oct 2009
 */
public interface OpenMarkovPreferencesKeys {

	// access
    String LAST_CONNECTION = "last connection";
	String LAST_USER_CONNECTED = "last user connected";

	// directories
    String INITIALIZED = "initialized";
	String LAST_OPEN_DIRECTORY = "last open directory";
	String LAST_OPEN_FILE = "last open file ";
	String LAST_OPEN_FILE_1 = "last open file 1";
	String LAST_OPEN_FILE_2 = "last open file 2";
	String LAST_OPEN_FILE_3 = "last open file 3";
	String LAST_OPEN_FILE_4 = "last open file 4";
	String LAST_OPEN_FILE_5 = "last open file 5";
	String STRING_LANGUAGES_PATH = "languages directory path";
	String LAST_OPEN_DB_DIRECTORY = "last open db directory";

	// positions
    String X_OPENMARKOV_MAIN_FRAME = "x openmarkov main frame";
	String Y_OPENMARKOV_MAIN_FRAME = "y openmarkov main frame";
	String X_OPEMARKOV_HELP_DIMENSION = "xDimension Camen HelpViewer";
	String Y_OPENMARKOV_HELP_DIMENSION = "yDimension OpenMarkov HelpViewer";

	// colors
    String ARROW_BACKGROUND_COLOR = "arrow background";
	String ARROW_FOREGROUND_COLOR = "arrow foreground";
	String NODECHANCE_BACKGROUND_COLOR = "node chance background";
	String NODECHANCE_FOREGROUND_COLOR = "node chance foreground";
	String NODECHANCE_TEXT_COLOR = "node chance text";
	String NODEDECISION_BACKGROUND_COLOR = "node decision background";
	String NODEDECISION_FOREGROUND_COLOR = "node decision foreground";
	String NODEDECISION_TEXT_COLOR = "node decision text";
	String NODEUTILITY_BACKGROUND_COLOR = "node utility background";
	String NODEUTILITY_FOREGROUND_COLOR = "node utility foreground";
	String NODEUTILITY_TEXT_COLOR = "node utility text";
	String TABLE_HEADER_TEXT_COLOR_1 = "tableheader first row";
	String TABLE_HEADER_TEXT_COLOR_2 = "tableheader second row";
	String TABLE_HEADER_TEXT_COLOR_3 = "tableheader third row";
	String TABLE_HEADER_TEXT_BACKGROUND_COLOR_1 = "tableheader background 1";
	String TABLE_HEADER_TEXT_BACKGROUND_COLOR_2 = "tableheader background 2";
	String TABLE_FIRST_COLUMN_FOREGROUND_COLOR = "table first column foreground";
	String TABLE_FIRST_COLUMN_BACKGROUND_COLOR = "table first column background";
	String TABLE_CELLS_FOREGROUND_COLOR = "table cells foreground";
	String TABLE_CELLS_BACKGROUND_COLOR = "table cells background";
	String ALWAYS_OBSERVED_VARIABLE = "always observed variable border color";
	String REVELATION_ARC_VARIABLE = "revelation arc color";

	// languages
    String PREFERENCE_LANGUAGE = "user prefered language";

	// parsers & writers
    String LAST_OPENED_FORMAT = "last opened format";
	String LAST_SAVED_FORMAT = "last saved format";
	String LAST_LOADED_EVIDENCE_FORMAT = "last loaded evidence format";

}
