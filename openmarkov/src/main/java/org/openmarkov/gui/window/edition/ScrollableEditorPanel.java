/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition;

import javax.swing.*;

@SuppressWarnings("serial") public class ScrollableEditorPanel extends JScrollPane {
	public ScrollableEditorPanel(EditorPanel editorPanel) {
		setViewportView(editorPanel);
		getVerticalScrollBar().setUnitIncrement(25);
	}
}
