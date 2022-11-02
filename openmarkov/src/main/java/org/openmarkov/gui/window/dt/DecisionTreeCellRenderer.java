/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.dt;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class DecisionTreeCellRenderer implements TreeCellRenderer {

	@Override public Component getTreeCellRendererComponent(JTree tree, Object object, boolean selected,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (object instanceof DecisionTreeElementPanel) {
			((DecisionTreeElementPanel) object).update(selected, expanded, leaf, row, hasFocus);
		}
		return (Component) object;
	}

}
