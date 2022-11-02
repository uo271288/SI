/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.apache.logging.log4j.Logger;
import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.localize.LocalizedException;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>MoveNodeEdi</code> is a simple edit that allows to modify the position
 * of a group of nodes
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 */
public class MoveNodeEdit extends SimplePNEdit {
	/**
	 *
	 */
	private static final long serialVersionUID = 7578733825996342882L;
	/**
	 * The nodes last positions before the action
	 */
	private List<Point2D.Double> lastPositions = new ArrayList<Point2D.Double>();
	/**
	 * The new positions of the nodes to move
	 */
	private List<Point2D.Double> newPositions = new ArrayList<Point2D.Double>();
	/**
	 * The node's name to move
	 */
	private List<String> namesNode = new ArrayList<String>();
	
	/**
	 * Logger
	 */
	protected Logger logger;

	/**
	 * Creates a new <code>MoveNodeEdit</code> with the nodes, and new X, Y
	 * coordinates.
	 *
	 * @param movedNodes the nodes that will be edited, with their new
	 *                   positions.
	 */
	public MoveNodeEdit(List<VisualNode> movedNodes) {
		super(movedNodes.get(0).getNode().getProbNet());
		for (VisualNode visualNode : movedNodes) {
			lastPositions.add((Point2D.Double) visualNode.getPosition().clone());
			newPositions.add((Point2D.Double) visualNode.getTemporalPosition().clone());
			namesNode.add(visualNode.getNode().getName());
		}
	}

	@Override public void doEdit() {
		Node node = null;
		int i = 0;
		for (String name : namesNode) {
			try {
				node = probNet.getNode(name);
				node.setCoordinateX(newPositions.get(i).getX());
				node.setCoordinateY(newPositions.get(i).getY());
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
				logger.warn(e.getMessage());
				LocalizedException exception = new LocalizedException(e);
				exception.showException();				
				JOptionPane.showMessageDialog(null, StringDatabase.getUniqueInstance().getString(e.getMessage()),
						StringDatabase.getUniqueInstance().getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
			i++;
		}
	}

	public void undo() {
		super.undo();
		int i = 0;
		Node node = null;
		for (String name : namesNode) {
			try {
				node = probNet.getNode(name);
				node.setCoordinateX(lastPositions.get(i).getX());
				node.setCoordinateY(lastPositions.get(i).getY());
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
				logger.warn(e.getMessage());
				LocalizedException exception = new LocalizedException(e); 
				exception.showException();
				JOptionPane.showMessageDialog(null, StringDatabase.getUniqueInstance().getString(e.getMessage()),
						StringDatabase.getUniqueInstance().getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
			i++;
		}
	}
}
