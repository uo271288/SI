/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition.mode;

import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.DefaultStates;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.util.GUIDefaultStates;
import org.openmarkov.gui.util.Utilities;
import org.openmarkov.gui.window.edition.EditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;

public abstract class NodeEditionMode extends EditionMode {
	private NodeType nodeType;

	public NodeEditionMode(EditorPanel editorPanel, ProbNet probNet, NodeType nodeType) {
		super(editorPanel, probNet);
		this.nodeType = nodeType;
	}

	@Override public void mousePressed(MouseEvent e, Point2D.Double position, Graphics2D g) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (Utilities.noMouseModifiers(e)) {
				if (visualNetwork.getElementInPosition(position, g) == null) {
					probNet.getPNESupport().setWithUndo(true);
					HashSet<String> existingNames = new HashSet<String>();
					for (Node node : probNet.getNodes()) {
						String name = node.getName();
						if (name.contains("[")) {
							existingNames.add(name.substring(0, name.indexOf(" [")));
						} else {
							existingNames.add(node.getName());
						}
					}
					String nodeName = Util.getNextNodeName(nodeType, existingNames);
					State states[] = DefaultStates.getStatesNodeType(nodeType, probNet.getDefaultStates());
					for (int i = 0; i < states.length; i++) {
						states[i] = new State(GUIDefaultStates.getString(states[i].getName()));
					}
					Variable variable = new Variable(nodeName, states);
					if (probNet.onlyTemporal()) {
						// default value
						variable.setBaseName(nodeName);
						variable.setTimeSlice(0);
					}
					List<Criterion> decisionCriteria = probNet.getDecisionCriteria();
					if (nodeType == NodeType.UTILITY && decisionCriteria != null) {
						variable.setDecisionCriterion(decisionCriteria.get(0));
					}
					AddNodeEdit addNodeEdit = new AddNodeEdit(probNet, variable, nodeType, position);
					try {
						probNet.doEdit(addNodeEdit);
					} catch (Exception e1) {
						System.err.println(e1.toString());
						e1.printStackTrace();
						JOptionPane.showMessageDialog(this.editorPanel, e1.toString(), "Error creating node",
								JOptionPane.ERROR_MESSAGE);
					}
					editorPanel.adjustPanelDimension();
					editorPanel.repaint();
				}
			}
		}
	}

	@Override public void mouseReleased(MouseEvent e, Point2D.Double cursorPosition, Graphics2D g) {
		// TODO Auto-generated method stub
	}

	@Override public void mouseDragged(MouseEvent e, Point2D.Double position, double diffX, double diffY,
			Graphics2D g) {
		// TODO Auto-generated method stub
	}
}
