/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.window.edition.SelectedContent;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial") public class PasteEdit extends CompoundEdit implements PNEdit {
	private SelectedContent clipboardContent;
	private SelectedContent pastedContent;
	private VisualNetwork visualNetwork;

	public PasteEdit(VisualNetwork visualNetwork, SelectedContent clipboardContent) {
		this.clipboardContent = clipboardContent;
		this.visualNetwork = visualNetwork;
		this.pastedContent = null;
	}

	// Methods

	/**
	 * Generate edits and does them
	 * carmenyago only adapted the method to the change in utility potentials
	 *
	 * @throws DoEditException
	 * @throws WrongCriterionException
	 * @throws NonProjectablePotentialException
	 * @author carmenyago
	 */
	public void doEdit() throws DoEditException, NonProjectablePotentialException, WrongCriterionException {
		HashMap<String, String> newVariables = new HashMap<String, String>();
		ProbNet probNet = visualNetwork.getNetwork();
		// Gather new node creation edits
		for (Node node : clipboardContent.getNodes()) {
			String oldName = node.getName();
			String newName = oldName;
			while (probNet.containsVariable(newName)) {
				newName += "'";
			}
			Variable variable = new Variable(node.getVariable());
			variable.setName(newName);
			newVariables.put(oldName, newName);

			Point2D.Double position = new Point2D.Double(node.getCoordinateX() + 3.0, node.getCoordinateY());
			edits.add(new AddNodeEdit(probNet, variable, node.getNodeType(), position));

		}

		// Apply node generation edits
		ArrayList<Node> pastedNodes = new ArrayList<Node>();
		for (UndoableEdit edit : edits) {
			try {
				probNet.doEdit(((PNEdit) edit));
				pastedNodes.add(((AddNodeEdit) edit).getNode());
			} catch (ConstraintViolationException e) {
				e.printStackTrace();
			}
		}

		//Gather link creation edits
		for (Link<Node> link : clipboardContent.getLinks()) {
			try {
				String originalSourceNodeName = link.getNode1().getName();
				String originalDestinationNodeName = link.getNode2().getName();

				edits.add(new AddLinkEdit(probNet, probNet.getVariable(newVariables.get(originalSourceNodeName)),
						probNet.getVariable(newVariables.get(originalDestinationNodeName)), link.isDirected()));
			} catch (NodeNotFoundException e) {/* Can not possibly happen */
			}
		}

		//Apply link creation edits
		List<Link<Node>> pastedLinks = new ArrayList<>();
		for (UndoableEdit edit : edits) {
			if (edit instanceof AddLinkEdit) {
				AddLinkEdit linkEdit = ((AddLinkEdit) edit);
				try {
					probNet.doEdit(linkEdit);
					pastedLinks.add(linkEdit.getLink());
				} catch (ConstraintViolationException e) {
					e.printStackTrace();
				}
			}
		}
		super.end();
		pastedContent = new SelectedContent(pastedNodes, pastedLinks);

		//Replace potentials to already created nodes with copies of copied nodes
		for (Node originalNode : clipboardContent.getNodes()) {
			ArrayList<Potential> newPotentials = new ArrayList<Potential>();
			try {
				Node newNode = probNet.getNode(newVariables.get(originalNode.getName()));
				for (Potential originalPotential : originalNode.getPotentials()) {
					Potential potential = originalPotential.copy();
					for (int i = 0; i < potential.getNumVariables(); ++i) {
						String variableName = potential.getVariable(i).getName();
						if (newVariables.containsKey(variableName)) {
							Variable variable = probNet.getVariable(newVariables.get(variableName));
							potential.replaceVariable(i, variable);
						}
					}
					//carmenyago Commented to adapt the code to the new potentials. Now there isn't utilityVariable
					// If the potential is ExactDistrPotential Iset the new childVariable
                    /*
                    if(potential.isUtility())
                    {
                    	Variable utilityVariable = potential.getUtilityVariable();
                    	if(newVariables.containsKey (utilityVariable.getName()))
                    	{
                    		potential.replaceVariable (utilityVariable, probNet.getVariable (newVariables.get (utilityVariable.getName())));
                    	}
                    }
                    */

					if (potential instanceof ExactDistrPotential) {
						Variable child = ((ExactDistrPotential) potential).getChildVariable();
						if (newVariables.containsKey(child.getName())) {
							((ExactDistrPotential) potential)
									.setChildVariable(probNet.getVariable(newVariables.get(child.getName())));
						}

					}

					//
					newPotentials.add(potential);
				}
				newNode.setPotentials(newPotentials);
				// Copy comment too!
				newNode.setComment(originalNode.getComment());
				newNode.setRelevance(originalNode.getRelevance());
				newNode.setPurpose(originalNode.getPurpose());
				newNode.additionalProperties = new HashMap<String, String>(originalNode.additionalProperties);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	//@Override
	public void setSignificant(boolean significant) {
		// Do nothing
	}

	/**
	 * Returns the pasted content.
	 *
	 * @return the pastedContent.
	 */
	public SelectedContent getPastedContent() {
		return pastedContent;
	}

	// @Override
	public ProbNet getProbNet() {
		return visualNetwork.getNetwork();
	}

}
