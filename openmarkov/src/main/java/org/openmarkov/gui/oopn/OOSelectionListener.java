/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.oopn;

import org.openmarkov.gui.graphic.SelectionListener;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNode;

import java.util.List;

/**
 * This interface is implemented by classes interested in objects selected in
 * a VisualOONetwork
 *
 * @author ibermejo
 * @version 1.0
 */
public interface OOSelectionListener extends SelectionListener {

	/**
	 * This method indicates the selected elements
	 *
	 * @param selectedNodes     array of nodes that are currently selected
	 * @param selectedLinks     array of links that are currently selected
	 * @param selectedInstances array of instances that are currently selected
	 */
	void objectsSelected(List<VisualNode> selectedNodes, List<VisualLink> selectedLinks,
			List<VisualInstance> selectedInstances, List<VisualReferenceLink> selectedReferenceLinks);

}


