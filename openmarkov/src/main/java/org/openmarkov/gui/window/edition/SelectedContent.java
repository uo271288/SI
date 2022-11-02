/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ibermejo
 */
public class SelectedContent {
	/**
	 * Copied nodes
	 */
	private List<Node> nodes;

	/**
	 * Copied links
	 */
	private List<Link<Node>> links;

	/**
	 * Constructor for ClipboardContent.
	 *
	 * @param nodes
	 * @param links
	 */

	public SelectedContent(List<Node> nodes, List<Link<Node>> links) {
		this.nodes = nodes;
		this.links = links;
	}

	/**
	 * Copy constructor for ClipboardContent.
	 *
	 * @param content selected nodes and links
	 */
	public SelectedContent(SelectedContent content) {
		this.nodes = new ArrayList<>(content.getNodes());
		this.links = new ArrayList<>(content.getLinks());
	}

	/**
	 * Returns nodes in the clipboard
	 *
	 * @return nodes in the clipboard
	 */
	public List<Node> getNodes() {
		return this.nodes;
	}

	/**
	 * Returns links in the clipboard
	 *
	 * @return links in the clipboard
	 */
	public List<Link<Node>> getLinks() {
		return this.links;
	}

	/**
	 * returns whether the object is empty
	 * @return true iff the selected content is empty
	 */
	public boolean isEmpty() {
		return this.nodes.size() == 0 && this.links.size() == 0;
	}

}