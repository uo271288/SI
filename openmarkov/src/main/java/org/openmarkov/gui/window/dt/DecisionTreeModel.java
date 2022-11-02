/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.dt;

import org.openmarkov.core.dt.DecisionTreeBranch;
import org.openmarkov.core.dt.DecisionTreeElement;
import org.openmarkov.core.dt.DecisionTreeNode;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.Set;

public class DecisionTreeModel implements TreeModel {
	private Set<TreeModelListener> listeners;
	private DecisionTreeElementPanel root;

	/**
	 * Constructor for DecisionTreeModel.
	 */
	public DecisionTreeModel(DecisionTreeElement root) {
		super();
		this.listeners = new HashSet<>();
		this.root = buildPanelTree(root);
	}

	private DecisionTreeElementPanel buildPanelTree(DecisionTreeElement treeElement) {
		DecisionTreeElementPanel treeElementPanel = null;
		if (treeElement instanceof DecisionTreeNode) {
			treeElementPanel = new DecisionTreeNodePanel((DecisionTreeNode) treeElement);
		} else if (treeElement instanceof DecisionTreeBranch) {
			treeElementPanel = new DecisionTreeBranchPanel((DecisionTreeBranch) treeElement);
		}

		for (DecisionTreeElement child : treeElement.getChildren()) {
			treeElementPanel.addChild(buildPanelTree(child));
		}

		return treeElementPanel;
	}

	@Override public void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
	}

	@Override public Object getChild(Object parent, int index) {
		return ((DecisionTreeElementPanel) parent).getChildren().get(index);
	}

	@Override public int getChildCount(Object parent) {
		return ((DecisionTreeElementPanel) parent).getChildren().size();
	}

	@Override public int getIndexOfChild(Object parent, Object child) {
		return ((DecisionTreeElementPanel) parent).getChildren().indexOf(child);
	}

	@Override public Object getRoot() {
		return root;
	}

	@Override public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}

	@Override public void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}

	@Override public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub

	}
}
