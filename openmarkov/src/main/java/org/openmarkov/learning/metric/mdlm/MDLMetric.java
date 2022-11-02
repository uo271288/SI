/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.mdlm;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.scoreAndSearch.metric.annotation.MetricType;
import org.openmarkov.learning.metric.entropy.EntropyMetric;

import java.util.HashMap;

/**
 * This class implements the MDL metric.
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@MetricType(name = "MDLM") public class MDLMetric extends EntropyMetric {

	//Membern
	//Members
	/**
	 * HashMap with the dimension of each node. (We do not want to
	 * recalculate the dimension of all the nodes of the net every time
	 * we make an edition)
	 */
	protected HashMap<String, Double> nodesDimensions;

	//Constructor

	public MDLMetric() {
		super();
		nodesDimensions = new HashMap<String, Double>();
	}

	@Override public void init(ProbNet probNet, CaseDatabase caseDatabase) {
		super.init(probNet, caseDatabase);
		calculateDimension();
	}

	/**
	 * Scores the associated network.
	 *
	 * @return <code>double</code> score of the net
	 */
	public double score() {
		return super.getScore() - (calculateDimension() / 2) * Math.log(caseDatabase.getCases().length);
	}

	/**
	 * Scores the associated network with the link given in the received
	 * edition added. We only have to recalculate the entropy and dimension
	 * of the destination node. If an undoable edit happened (that is, if
	 * parameter change is true) we update the entropy and dimension of the
	 * destination node and the net.
	 *
	 * @param edit   <code>AddLinkEdit</code>
	 * @param change <code>boolean</code> indicates whether the edition is
	 *               definitive (UndoableEditHappend called this method) or not.
	 * @return <code>double</code> score of the net with the given edition
	 */
	protected double score(AddLinkEdit edit, boolean change) {

		Node destinationNode = probNet.getNode(edit.getVariable2());
		double coefficient = Math.log(caseDatabase.getCases().length) / 2;
		/* Dimension of the node without adding the link */
		double lastNodeDimension = nodesDimensions.get(((Node) destinationNode).getName());
		/* To calculate the new dimension, we subtract the last dimension
		 * of the destination node and sum the new dimension of this node*/
		double newNodeDimension = (edit.getVariable1().getNumStates() * lastNodeDimension);

		/*If change is true it's because we have to update the probNet values
		 * and store the node dimension and entropy to avoid repeating the
		 * calculations */
		if (change) {
			nodesDimensions.put(destinationNode.getName(), new Double(newNodeDimension));
		}
		return super.score(edit, change) - (newNodeDimension - lastNodeDimension) * coefficient;
	}

	/**
	 * Scores the associated network with the link given in the received
	 * edition removed. We only have to recalculate the entropy and dimension
	 * of the destination node. If an undoable edit happened (that is, if
	 * parameter change is true) we update the entropy and dimension of the
	 * destination node and the net.
	 *
	 * @param edit   <code>AddLinkEdit</code>
	 * @param change <code>boolean</code> indicates whether the edition is
	 *               definitive (UndoableEditHappend called this method) or not.
	 * @return <code>double</code> score of the net with the given edition
	 */
	protected double score(RemoveLinkEdit edit, boolean change) {

		Node destinationNode = probNet.getNode(edit.getVariable2());
		double coefficient = Math.log(caseDatabase.getCases().length) / 2;

		/* dimension of the node without adding the link */
		double lastNodeDimension = nodesDimensions.get(destinationNode.getName());
		/* To calculate the dimension, we subtract the last dimension
		 * of the destination node and sum the new dimension of this node*/
		double newNodeDimension = lastNodeDimension / (edit.getVariable1().getNumStates());
		/*If change is true it's because we have to update the probNet values*/
		if (change) {
			nodesDimensions.put(destinationNode.getName(), new Double(newNodeDimension));
		}

		return super.score(edit, change) - (newNodeDimension - lastNodeDimension) * coefficient;
	}

	/**
	 * Scores the associated network with the link given in the received
	 * edition inverted. We have to recalculate the entropies and dimensions
	 * of the destinations nodes before and after the inversion. If an undoable
	 * edit happened (that is, if parameter change is true) we update the
	 * entropies and dimensions of the destinations node and the net.
	 *
	 * @param edit   <code>InvertLinkEdit</code>
	 * @param change <code>boolean</code> indicates wheter the edition is
	 *               definitive (UndoableEditHappend called this method) or not.
	 * @return <code>double</code> score of the net with the given edition
	 */
	@Override protected double score(InvertLinkEdit edit, boolean change) {

		Node initialDestinationNode = probNet.getNode(edit.getVariable2());
		Node initialOriginNode = probNet.getNode(edit.getVariable1());
		double coefficient = Math.log(caseDatabase.getCases().length) / 2;

		/* dimension of the node without adding the link */
		double lastNodeDimension = nodesDimensions.get(initialDestinationNode.getName());
		double newNodeDimension = lastNodeDimension / edit.getVariable1().getNumStates();

		/*If change is true it's because we have to update the probNet values*/
		if (change) {
			nodesDimensions.put(initialDestinationNode.getName(), new Double(newNodeDimension));
		}

		double result = super.score(edit, change) - (newNodeDimension - lastNodeDimension) * coefficient;

		/* We do the calculations for the final destination node*/
		lastNodeDimension = nodesDimensions.get(((Node) initialOriginNode).getName());
		newNodeDimension = ((edit.getVariable1().getNumStates()) * lastNodeDimension);

		/*If change is true it's because we have to update the probNet values
		 * and store the node dimension and entropy to avoid repeating the
		 * calculations */
		if (change) {
			nodesDimensions.put(initialOriginNode.getName(), new Double(newNodeDimension));
		}

		result -= ((newNodeDimension - lastNodeDimension) * coefficient);
		return result;
	}

	/**
	 * Calculates the dimension of the net as the sum of the dimensions
	 * of each node. It is only used the first time we score the net, so we make
	 * all the calculations.
	 *
	 * @return double dimension of the net
	 */
	protected double calculateDimension() {
		double newDimension = 0;

		for (Node node : probNet.getNodes()) {
			newDimension += nodeDimension(node, true);
		}
		return newDimension;
	}

	/**
	 * Calculates the dimension of the given node as the product of
	 * its number of states and the number of possible configurations
	 * of its parents.
	 *
	 * @param node   <code>Node</code> whose dimension we want to calculate.
	 * @param change <code>boolean</code> indicates wheter the edition is
	 *               definitive (UndoableEditHappend called this method) or not.
	 * @return double dimension of this node
	 */
	protected double nodeDimension(Node node, boolean change) {
		int numStates = node.getVariable().getNumStates();
		int parentsConfigurations = 1;
		double nodeDimension = 0;

		for (Node parent : node.getParents()) {
			parentsConfigurations *= parent.getVariable().getNumStates();
		}
		nodeDimension = (numStates - 1) * parentsConfigurations;
		/* Store the dimension of the node to avoid repeating the calculations */
		if (change) {
			nodesDimensions.put(node.getName(), new Double(nodeDimension));
		}
		return nodeDimension;
	}
}