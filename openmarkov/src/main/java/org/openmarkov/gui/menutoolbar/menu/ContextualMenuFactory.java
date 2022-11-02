/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.gui.graphic.VisualElement;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.menutoolbar.common.MenuAssistant;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasic;
import org.openmarkov.gui.oopn.InstanceContextualMenu;
import org.openmarkov.gui.oopn.VisualInstance;
import org.openmarkov.gui.window.edition.EditorPanel;

import java.awt.event.ActionListener;

/**
 * This class has a reference to all the contextual menus of the application.
 *
 * @author jmendoza
 */
public class ContextualMenuFactory implements MenuToolBarBasic {

	/**
	 * Constant that indentifies the network contextual menu.
	 */
	public static final int NETWORK = 0;

	/**
	 * Constant that indentifies the node contextual menu.
	 */
	public static final int NODE = 1;

	/**
	 * Constant that indentifies the link contextual menu.
	 */
	public static final int LINK = 2;

	/**
	 * Constant that indentifies the instance contextual menu.
	 */
	public static final int INSTANCE = 3;

	/**
	* Constant that identifies the tree contextual menu.
	*/
	public static final int TREE = 4;

	/**
	 * Contextual menu that has the options of a whole network.
	 */
	private ContextualMenu networkContextualMenu = null;

	/**
	 * Contextual menu that has the options of a node.
	 */
	private ContextualMenu nodeContextualMenu = null;

	/**
	 * Contextual menu that has the options of a link.
	 */
	private ContextualMenu linkContextualMenu = null;

	/**
	 * Contextual menu that has the options of an instance.
	 */
	private ContextualMenu instanceContextualMenu = null;

	/**
	 * Contextual menu that has the options of a node in a decision tree.
	 */
	private ContextualMenu treeContextualMenu = null;


	/**
	 * Assistant that manages all the contextual menus.
	 */
	private MenuAssistant menuAssistant = null;

	/**
	 * Listener for all the contextual menus.
	 */
	private ActionListener listener = null;

	/**
	 * Creates a new instance.
	 *
	 * @param newListener listener of the user's actions.
	 */
	public ContextualMenuFactory(ActionListener newListener) {

		listener = newListener;
		initialize();
	}

	/**
	 * This method initialises the instance.
	 */
	private void initialize() {

		menuAssistant = new MenuAssistant();
	}

	/**
	 * This method initialises networkContextualMenu.
	 *
	 * @return the network panel contextual menu.
	 */
	public ContextualMenu getNetworkContextualMenu(boolean canBeExpanded) {

		networkContextualMenu = new NetworkContextualMenu(listener, canBeExpanded);
		networkContextualMenu.setName("networkContextualMenu");
		menuAssistant.addMenu(networkContextualMenu);

		return networkContextualMenu;
	}

	/**
	 * This method initialises nodeContextualMenu.
	 *
	 * @param panel
	 * @param selectedNode
	 * @return the node contextual menu.
	 */
	private ContextualMenu getNodeContextualMenu(VisualNode selectedNode, EditorPanel panel) {

		menuAssistant.removeMenu(nodeContextualMenu);
		nodeContextualMenu = new NodeContextualMenu(listener, selectedNode, panel);
		nodeContextualMenu.setName("nodeContextualMenu");
		menuAssistant.addMenu(nodeContextualMenu);
		return nodeContextualMenu;
	}

	/**
	 * This method initialises linkContextualMenu.
	 *
	 * @param panel
	 * @param selectedLink
	 * @return the link contextual menu.
	 */
	private ContextualMenu getLinkContextualMenu(VisualLink selectedLink, EditorPanel panel) {

		menuAssistant.removeMenu(linkContextualMenu);
		linkContextualMenu = new LinkContextualMenu(listener, selectedLink, panel);
		linkContextualMenu.setName("linkContextualMenu");
		menuAssistant.addMenu(linkContextualMenu);
		return linkContextualMenu;
	}

	/**
	 * This method initialises instanceContextualMenu.
	 *
	 * @param panel
	 * @param selectedInstance
	 * @return the instance contextual menu .
	 */
	// TODO OOPN start
	private ContextualMenu getInstanceContextualMenu(VisualInstance selectedInstance, EditorPanel panel) {

		if (instanceContextualMenu == null) {
			instanceContextualMenu = new InstanceContextualMenu(listener);
			instanceContextualMenu.setName("instanceContextualMenu");
			menuAssistant.addMenu(instanceContextualMenu);
		}
		return instanceContextualMenu;
	}

	/**
	 * This method initialises treeContextualMenu. A menu for the nodes of a decision tree.
	 * @param enableShowCEP 
	 *
	 * @return the tree contextual menu.
	 */
	private ContextualMenu getStandardTreeContextualMenu(boolean enableShowCEP) {

		menuAssistant.removeMenu(treeContextualMenu);
		treeContextualMenu = new TreeContextualMenu(listener,enableShowCEP);
		treeContextualMenu.setName("treeContextualMenu");
		menuAssistant.addMenu(treeContextualMenu);
		return treeContextualMenu;
	}

	// TODO OOPN end

	/**
	 * Enables or disabled an option identified by an action command.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to enable the option, false to disable.
	 */
	public void setOptionEnabled(String actionCommand, boolean b) {

		menuAssistant.setOptionEnabled(actionCommand, b);
	}

	/**
	 * Selects or unselects an option identified by an action command. Only
	 * selects or unselects the components that are AbstractButton.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to select the option, false to unselect.
	 */
	public void setOptionSelected(String actionCommand, boolean b) {

		menuAssistant.setOptionSelected(actionCommand, b);
	}

	/**
	 * Adds a text to the label of an option identified by an action command.
	 * Only adds a text to the components that are AbstractButton.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param text          text to add to the label of the options. If null, nothing is
	 *                      added.
	 */
	public void addOptionText(String actionCommand, String text) {

		menuAssistant.addOptionText(actionCommand, text);
	}

	/**
	 * Changes the text of menu item
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param text          text to set to the Item.
	 */
	public void setText(String actionCommand, String text) {

		menuAssistant.setText(actionCommand, text);
	}

	/**
	 * Returns an instance of a pop up menu given the class and some additional
	 * info
	 * @return The contextual menu
	 */
	public ContextualMenu getContextualMenu(VisualElement selectedElement, EditorPanel panel) {
		ContextualMenu contextualMenu = null;
		if (VisualNode.class.isAssignableFrom(selectedElement.getClass())) {
			contextualMenu = getNodeContextualMenu((VisualNode) selectedElement, panel);
		} else if (VisualLink.class.isAssignableFrom(selectedElement.getClass())) {
			contextualMenu = getLinkContextualMenu((VisualLink) selectedElement, panel);
			// TODO OOPN start
		} else if (VisualInstance.class.isAssignableFrom(selectedElement.getClass())) {
			contextualMenu = getInstanceContextualMenu((VisualInstance) selectedElement, panel);
			// TODO OOPN end
		}
		return contextualMenu;
	}

	/**
	 * Returns the correspondent tree pop-up menu. As for now there is only one possible menu
	 * so discrimination by inputs (as is @link #getContextualMenu(VisualElement, EditorPanel))
	 * is not necessary.
	 * @param enableShowCEP 
	 *
	 * @return the correspondent tree pop-up menu
	 */
	public ContextualMenu getTreeContextualMenu(boolean enableShowCEP) {
		return getStandardTreeContextualMenu(enableShowCEP);
	}


}
