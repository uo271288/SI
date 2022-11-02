/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.algorithm.pc;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.BaseLinkEdit;
import org.openmarkov.core.action.COrientLinksEdit;
import org.openmarkov.core.action.OrientLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.pc.independencetester.IndependenceTester;
import org.openmarkov.learning.algorithm.pc.util.PCEditMotivation;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmType;
import org.openmarkov.learning.core.util.LearningEditMotivation;
import org.openmarkov.learning.core.util.LearningEditProposal;
import org.openmarkov.learning.core.util.StringEditMotivation;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

@LearningAlgorithmType(name = "PC") public class PCAlgorithm extends IndependenceRelationsAlgorithm
		implements PNUndoableEditListener {

	public static final int ALREADY_DONE = -1;
	/**
	 * These fields indicate the stage of the algorithm.
	 */
	protected static final int INITIAL_PHASE = 0;
	protected static final int HEAD_TO_HEAD_ORIENTATION = 1;
	protected static final int REMAINING_LINKS_ORIENTATION = 2;
	protected static final int ORIENTATION_FINISHED = 3;
	protected static final int LEARNING_FINISHED = 4;
	/**
	 * ProbNet that is being learned.
	 */
	protected ProbNet probNet;
	protected Map<Node, Map<Node, PCEditMotivation>> cache;
	/**
	 * History of last best edits returned.
	 */
	protected Set<PNEdit> lastRemovedEdits;
	protected Set<PNEdit> lastOrientationEdits;
	protected List<COrientLinksEdit> lastCompoundOrientationEdits;
	/**
	 * Case database we are learning upon
	 */
	protected CaseDatabase caseDatabase;
	protected IndependenceTester independenceTester;
	/**
	 * Degree of accuracy of the independence test.
	 */
	protected double significanceLevel;

	public PCAlgorithm(ProbNet probNet, CaseDatabase caseDatabase, Double alpha, IndependenceTester independenceTester,
			Double significanceLevel) {
		super(probNet, caseDatabase, alpha);
		this.probNet = probNet;
		this.caseDatabase = caseDatabase;
		this.independenceTester = independenceTester;
		this.significanceLevel = significanceLevel;
		this.probNet.getPNESupport().addUndoableEditListener(this);
		cache = new HashMap<Node, Map<Node, PCEditMotivation>>();
		for (Node node : probNet.getNodes()) {
			cache.put(node, new HashMap<Node, PCEditMotivation>());
		}
		lastRemovedEdits = new HashSet<PNEdit>();
		lastOrientationEdits = new HashSet<PNEdit>();
		lastCompoundOrientationEdits = new ArrayList<COrientLinksEdit>();
	}

	/**
	 * Method that returns the best edit in each step of the algorithm or null
	 * if there are no more edits to consider (depending on the arguments it
	 * receives).
	 */
	@Override public LearningEditProposal getBestEdit(boolean onlyAllowedEdits, boolean onlyPositiveEdits) {
		resetHistory();
		return getNextEdit(onlyAllowedEdits, onlyPositiveEdits);
	}

	/**
	 * Method that returns the next best edit in each step of the algorithm
	 * or null if there are no more edits to consider (depending on the
	 * arguments it receives).
	 */
	@Override public LearningEditProposal getNextEdit(boolean onlyAllowedEdits, boolean onlyPositiveEdits) {

		LearningEditProposal bestEditProposal = getOptimalEdit(onlyAllowedEdits, onlyPositiveEdits);
		while (bestEditProposal != null && isBlocked(bestEditProposal)) {
			bestEditProposal = getOptimalEdit(onlyAllowedEdits, onlyPositiveEdits);
		}
		//if(bestEditProposal != null)System.out.println(bestEditProposal.toString());
		return bestEditProposal;
	}

	/**
	 * Method that returns the best edit in each step of the algorithm. It
	 * changes the phase of the algorithm in case it is necessary. If there
	 * are no possible edits on any of the phases it returns null.
	 */
	public LearningEditProposal getOptimalEdit(boolean onlyAllowedEdits, boolean onlyPositiveEdits) {
		int adjacencySize = 0;
		LearningEditProposal bestEditProposal = null;

		try {
			while (maxOfAdjacencies() > adjacencySize) {
				for (Node nodeX : probNet.getNodes()) {
					for (Node nodeY : nodeX.getSiblings()) {
						List<Node> adjacencySubset = nodeX.getNeighbors();
						adjacencySubset.remove(nodeY);
						RemoveLinkEdit removeLinkEdit = new RemoveLinkEdit(probNet, nodeX.getVariable(),
								nodeY.getVariable(), false);
						if (!alreadyConsidered(removeLinkEdit, lastRemovedEdits)) {
							PCEditMotivation motivation = cache.get(nodeX).get(nodeY);
							// If not already cached (or the cached sep set has
							// a higher cardinality), search for the sep set with
							// the highest score
							if (motivation == null || (
									motivation.getScore() != ALREADY_DONE
											&& motivation.getSeparationSet().size() > adjacencySize
							)) {
								double bestScore = 0.0;
								List<Node> bestScoreSeparationSet = null;
								for (List<Node> separationSet : subSetsOfSize(adjacencySubset, adjacencySize)) {
									double linkScore = independenceTester
											.test(caseDatabase, nodeX, nodeY, separationSet);

									if (linkScore > bestScore && (
											!onlyPositiveEdits || linkScore > significanceLevel
									)) {
										bestScore = linkScore;
										bestScoreSeparationSet = separationSet;
									}
								}
								// If we found a suitable separation set, cache it
								if (bestScoreSeparationSet != null) {
									cache.get(nodeX)
											.put(nodeY, new PCEditMotivation(bestScore, bestScoreSeparationSet));
								}
							}

						}
					}
				}

				bestEditProposal = getOptimalEditFromCache(onlyAllowedEdits, onlyPositiveEdits);
				if (bestEditProposal != null) {
					return bestEditProposal;
				}
				adjacencySize++;
			}
		} catch (NodeNotFoundException e) {
			LogManager.getLogger(PCAlgorithm.class.getName()).
					log(Level.WARN, e);
		}

		if (bestEditProposal == null) {
			if (lastRemovedEdits.isEmpty()) {
				phase = HEAD_TO_HEAD_ORIENTATION;
				return getOrientationEdit(onlyAllowedEdits);
			}
		}
		phase = INITIAL_PHASE;
		return bestEditProposal;
	}

	public LearningEditProposal getOptimalEditFromCache(boolean onlyAllowedEdits, boolean onlyPositiveEdits)
			throws NodeNotFoundException {
		PCEditMotivation bestMotivation = null;
		LearningEditProposal bestEditProposal = null;

		for (Node nodeX : probNet.getNodes()) {
			for (Node nodeY : nodeX.getSiblings()) {
				PCEditMotivation motivation = cache.get(nodeX).get(nodeY);
				if ((motivation != null) && (motivation.getScore() != ALREADY_DONE) && (
						motivation.compareTo(bestMotivation) > 0
				) && (!onlyPositiveEdits || motivation.getScore() > significanceLevel)) {
					RemoveLinkEdit removeLinkEdit = new RemoveLinkEdit(probNet, nodeX.getVariable(),
							nodeY.getVariable(), false);
					LearningEditProposal learningEditProposal = new LearningEditProposal(removeLinkEdit, motivation);
					LearningEditProposal inverseEditProposal = new LearningEditProposal(inverseEdit(removeLinkEdit),
							motivation);
					if (!isBlocked(learningEditProposal) && !isBlocked(inverseEditProposal) && !alreadyConsidered(
							removeLinkEdit, lastRemovedEdits) && (!onlyAllowedEdits || isAllowed(removeLinkEdit))) {
						bestMotivation = motivation;
						bestEditProposal = learningEditProposal;
					}
				}
			}
		}
		if (bestEditProposal != null) {
			lastRemovedEdits.add(bestEditProposal.getEdit());
		}
		return bestEditProposal;
	}

	/**
	 * Returns the <code>PCEditProposal</code> with the
	 * <code>DirectLinkEdit</code> depending on which stage is the algorithm.
	 * If the "head to head" orientations have not been done, then, the
	 * DirectLinkEdit contains these edits. Else, it contains the remaining
	 * orientations.
	 * @return the orientation edit
	 */
	public LearningEditProposal getOrientationEdit(boolean onlyAllowedEdits) {
		LearningEditProposal bestEdit = null;

		try {
			bestEdit = orientHeadToHeadLinks(onlyAllowedEdits);

			if (bestEdit == null) {
				if (lastCompoundOrientationEdits.isEmpty()) {
					phase = REMAINING_LINKS_ORIENTATION;
					return orientRemainingLinks(onlyAllowedEdits);
				}
			}
		} catch (NodeNotFoundException | NonProjectablePotentialException | WrongCriterionException e) {
			e.printStackTrace();
		}

		return bestEdit;
	}

	/**
	 * This method returns the maximum number of neighbors of a node in
	 * the probNet that is being learned.
	 */
	private int maxOfAdjacencies() {
		int max = 0;
		for (Node node : probNet.getNodes()) {
			int adjacents = node.getNumNeighbors();
			if (adjacents > max)
				max = adjacents;
		}
		return max;
	}

	/**
	 * Returns a list of the subsets of size n of the given set
	 *
	 * @param set         <code>List</code> of
	 *                    <code>Node</code> from which extract the subsets.
	 * @param subSetsSize size of the subsets.
	 * @return <code>List</code> of <code>List</code> of
	 * <code>Node</code>. Each <code>List</code> of <code>Node</code>
	 * is one of the subsets of size n.
	 */
	public List<List<Node>> subSetsOfSize(List<Node> set, int subSetsSize) {

		List<List<Node>> subSets = new ArrayList<List<Node>>();
		List<Node> subSet = new ArrayList<Node>();
		boolean found = true;
		int[] indexSubSet = new int[subSetsSize];

		//Add the empty set
		if (subSetsSize == 0) {
			subSets.add(new ArrayList<Node>());
		}

		if ((subSetsSize > 0) & (subSetsSize <= set.size())) {
			for (int i = 0; i < subSetsSize; i++) {
				indexSubSet[i] = i;
				subSet.add(set.get(i));
			}
			subSets.add(subSet);

			if (subSetsSize < set.size()) {
				while (found) {
					found = false;

					for (int i = subSetsSize - 1; i >= 0; i--) {
						if (indexSubSet[i] < (set.size() + (i - subSetsSize))) {
							indexSubSet[i] = indexSubSet[i] + 1;

							if (i < (subSetsSize - 1)) {
								for (int j = i + 1; j < subSetsSize; j++) {
									indexSubSet[j] = indexSubSet[j - 1] + 1;
								}
							}

							found = true;
							break;
						}
					}

					if (found) {
						subSet = new ArrayList<Node>();
						for (int k = 0; k < subSetsSize; k++) {
							subSet.add(set.get(indexSubSet[k]));
						}

						subSets.add(subSet);
					}
				}
			}
		}

		return subSets;
	}

	/**
	 * Given a RemoveLinkEdit, this method returns the same link with the inverse
	 * direction. For example, if the parameter edit is a RemoveLinkEdit A-&gt;B,
	 * it returns the RemoveLinkEdit B-&gt;A
	 */
	public RemoveLinkEdit inverseEdit(RemoveLinkEdit edit) {
		return new RemoveLinkEdit(probNet, edit.getVariable2(), edit.getVariable1(), false);
	}

	public boolean alreadyConsidered(BaseLinkEdit edit, Set<PNEdit> consideredEdits) {
		BaseLinkEdit inverseEdit = new RemoveLinkEdit(probNet, edit.getVariable2(), edit.getVariable1(),
				edit.isDirected());
		return consideredEdits.contains(edit) || consideredEdits.contains(inverseEdit);
	}

	public boolean alreadyConsidered(OrientLinkEdit edit1, OrientLinkEdit edit2) {
		boolean result = false;

		for (COrientLinksEdit compoundDirectLinkEdit : lastCompoundOrientationEdits) {
			try {
				result |= (
						(edit1.compareTo((OrientLinkEdit) compoundDirectLinkEdit.getEdits().get(0)) == 0) && (
								edit2.compareTo((OrientLinkEdit) compoundDirectLinkEdit.getEdits().get(1)) == 0
						)
				);
				result |= (
						(edit1.compareTo((OrientLinkEdit) compoundDirectLinkEdit.getEdits().get(1)) == 0) && (
								edit2.compareTo((OrientLinkEdit) compoundDirectLinkEdit.getEdits().get(0)) == 0
						)
				);
			} catch (NonProjectablePotentialException | WrongCriterionException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Method to compute the first stage of the orientation. For each
	 * uncoupled meeting X - Y - Z if Y does not pertain to the separation
	 * set of X and Z, we should orient X -&gt; Y &lt;- Z.
	 */
	private LearningEditProposal orientHeadToHeadLinks(boolean onlyAllowedEdits) throws NodeNotFoundException {

		List<Node> neighborhoodX, neighborhoodY;
		COrientLinksEdit compoundDirectLinkEdit = null;
		OrientLinkEdit orientLinkEdit1, orientLinkEdit2;
		StringEditMotivation motivation;

		for (Node nodeX : probNet.getNodes()) {
			neighborhoodX = nodeX.getSiblings();
			for (Node nodeY : neighborhoodX) {
				neighborhoodY = nodeY.getSiblings();
				neighborhoodY.remove(nodeX);

				for (Node nodeZ : neighborhoodY) {
					//Adjacent nodeX and nodeZ?
					if (!nodeX.getNeighbors().contains(nodeZ)) {
						// if Y is not included in the separation set of X and Z
						List<Node> separationXZ = cache.get(nodeX).get(nodeZ).getSeparationSet();
						if (!separationXZ.contains(nodeY)) {
							//Then orient X-&gt;Y&lt;-Z
							orientLinkEdit1 = new OrientLinkEdit(probNet, nodeX.getVariable(), nodeY.getVariable(),
									true);
							orientLinkEdit2 = new OrientLinkEdit(probNet, nodeZ.getVariable(), nodeY.getVariable(),
									true);
							compoundDirectLinkEdit = new COrientLinksEdit(probNet, new Vector<UndoableEdit>());
							compoundDirectLinkEdit.addEdit(orientLinkEdit1);
							compoundDirectLinkEdit.addEdit(orientLinkEdit2);
							motivation = new StringEditMotivation(
									"Sep. set (" + nodeX.getName() + ", " + nodeZ.getName()
											+ ") does not contain variable: " + nodeY.getName());
							if (!alreadyConsidered(orientLinkEdit1, orientLinkEdit2) && !isBlocked(
									new LearningEditProposal(compoundDirectLinkEdit, motivation)) && (
									!onlyAllowedEdits || (
											isOrientationAllowed(orientLinkEdit1) && isOrientationAllowed(
													orientLinkEdit2)
									)
							)) {
								lastCompoundOrientationEdits.add(compoundDirectLinkEdit);
								return new LearningEditProposal(compoundDirectLinkEdit, motivation);
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Method to compute the final stage of the algorithm. The basic idea is
	 * that no new head-to-head links are created and that the DAG condition is
	 * preserved.
	 *
	 * @throws WrongCriterionException
	 * @throws NonProjectablePotentialException
	 */
	private LearningEditProposal orientRemainingLinks(boolean onlyAllowedEdits)
			throws NodeNotFoundException, NonProjectablePotentialException, WrongCriterionException {
		boolean change = true, change2 = true, oriented, skip;
		Node nodeX, nodeZ;
		List<Node> siblingsNodeZ;
		OrientLinkEdit orientLinkEdit = null;
		LearningEditProposal editProposal;

		while (change2) {
			change2 = false;
			while (change) {
				change = false;
				for (Link<Node> link : probNet.getLinks()) {
					nodeX = link.getNode1();
					nodeZ = link.getNode2();
					if (link.isDirected()) {   // X--&gt;Z
						for (Node nodeY : nodeZ.getSiblings()) {
							orientLinkEdit = new OrientLinkEdit(probNet, nodeZ.getVariable(), nodeY.getVariable(),
									true);
							editProposal = new LearningEditProposal(orientLinkEdit,
									new StringEditMotivation("Do not create cycles"));
							if (!nodeY.getNeighbors().contains(nodeX) && !alreadyConsidered(orientLinkEdit,
									lastOrientationEdits) && !isBlocked(editProposal) && (
									!onlyAllowedEdits || isOrientationAllowed(orientLinkEdit)
							)) {
								lastOrientationEdits.add(orientLinkEdit);
								return (editProposal);
							}
						}
					} else { // X -- Z Non-oriented link
						oriented = false;
						orientLinkEdit = new OrientLinkEdit(probNet, nodeX.getVariable(), nodeZ.getVariable(), true);
						editProposal = new LearningEditProposal(orientLinkEdit,
								new StringEditMotivation("Do not create cycles"));
						if (probNet.existsPath(nodeX, nodeZ, true) && !alreadyConsidered(orientLinkEdit,
								lastOrientationEdits) && !isBlocked(editProposal) && (
								!onlyAllowedEdits || isOrientationAllowed(orientLinkEdit)
						)) {
							change = true;
							oriented = true;
							lastOrientationEdits.add(orientLinkEdit);
							return (editProposal);
						}
						orientLinkEdit = new OrientLinkEdit(probNet, nodeZ.getVariable(), nodeX.getVariable(), true);
						editProposal = new LearningEditProposal(orientLinkEdit,
								new StringEditMotivation("Do not create cycles"));
						if ((probNet.existsPath(nodeZ, nodeX, true)) && (!oriented) && !alreadyConsidered(
								orientLinkEdit, lastOrientationEdits) && !isBlocked(editProposal) && (
								!onlyAllowedEdits || isOrientationAllowed(orientLinkEdit)
						)) {
							change = true;
							oriented = true;
							lastOrientationEdits.add(orientLinkEdit);
							return (editProposal);
						}
						if (!oriented) {
							siblingsNodeZ = nodeZ.getSiblings();
							siblingsNodeZ.remove(nodeX);
							for (Node nodeY : siblingsNodeZ) {
								if (!nodeY.getNeighbors().contains(nodeX)) {
									for (Node nodeW : siblingsNodeZ) {
										if (!nodeY.equals(nodeW)) {
											skip = false;
											if (!nodeX.getChildren().contains(nodeW)) {
												skip = true;
											}
											if (probNet.getLink(nodeZ, nodeY, true) != null) {
												skip = true;
											}
											if (!skip) {
												orientLinkEdit = new OrientLinkEdit(probNet, nodeZ.getVariable(),
														nodeW.getVariable(), true);
												editProposal = new LearningEditProposal(orientLinkEdit,
														new StringEditMotivation("Do not create cycles"));
												if (nodeY.getChildren().contains(nodeW) && !alreadyConsidered(
														orientLinkEdit, lastOrientationEdits) && !isBlocked(
														editProposal) && (
														!onlyAllowedEdits || isOrientationAllowed(orientLinkEdit)
												)) {
													change = true;
													skip = true;
													lastOrientationEdits.add(orientLinkEdit);
													return (editProposal);
												}
											}
											if (!skip) {
												orientLinkEdit = new OrientLinkEdit(probNet, nodeZ.getVariable(),
														nodeY.getVariable(), true);
												editProposal = new LearningEditProposal(orientLinkEdit,
														new StringEditMotivation("Do not create cycles"));
												if (nodeW.getChildren().contains(nodeY) && !alreadyConsidered(
														orientLinkEdit, lastOrientationEdits) && !isBlocked(
														editProposal) && (
														!onlyAllowedEdits || isOrientationAllowed(orientLinkEdit)
												)) {
													change = true;
													skip = true;
													lastOrientationEdits.add(orientLinkEdit);
													return (editProposal);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			for (Link<Node> link : probNet.getLinks()) {
				nodeX = link.getNode1();
				nodeZ = link.getNode2();
				if (!link.isDirected()) {   // X--Z
					orientLinkEdit = new OrientLinkEdit(probNet, nodeX.getVariable(), nodeZ.getVariable(), true);
					editProposal = new LearningEditProposal(orientLinkEdit,
							new StringEditMotivation("Do not create cycles"));
					if (!probNet.existsPath(nodeZ, nodeX, true) && !alreadyConsidered(orientLinkEdit,
							lastOrientationEdits) && !isBlocked(editProposal) && (
							!onlyAllowedEdits || isOrientationAllowed(orientLinkEdit)
					)) {
						change2 = true;
						lastOrientationEdits.add(orientLinkEdit);
						return (editProposal);
					} else {
						orientLinkEdit = new OrientLinkEdit(probNet, nodeZ.getVariable(), nodeX.getVariable(), true);
						editProposal = new LearningEditProposal(orientLinkEdit,
								new StringEditMotivation("Do not create cycles"));
						if (!isBlocked(editProposal) && (!onlyAllowedEdits || isOrientationAllowed(orientLinkEdit))
								&& (!alreadyConsidered(orientLinkEdit, lastOrientationEdits))) {
							change2 = true;
							lastOrientationEdits.add(orientLinkEdit);
							return (editProposal);
						}
					}
				}
			}
			orientLinkEdit = null;
		}
		if ((orientLinkEdit == null) && (lastOrientationEdits.isEmpty())) {
			phase = ORIENTATION_FINISHED;
		}
		return null;
	}

	private boolean isOrientationAllowed(OrientLinkEdit orientLinkEdit) {
		Node sourceNode = probNet.getNode(orientLinkEdit.getVariable1());
		Node destinationNode = probNet.getNode(orientLinkEdit.getVariable2());
		return (
				!probNet.existsPath(destinationNode, sourceNode, true) && isAllowed(orientLinkEdit)
		);
	}

	public void undoableEditWillHappen(UndoableEditEvent event)
			throws ConstraintViolationException {
	}

	public void undoEditHappened(UndoableEditEvent event) {
		UndoableEdit edit = event.getEdit();
		Node nodeX, nodeY;
		double linkScore;

		try {
			if (edit instanceof RemoveLinkEdit) {
				phase = INITIAL_PHASE;
				RemoveLinkEdit removeLinkEdit = (RemoveLinkEdit) edit;
				nodeX = probNet.getNode(removeLinkEdit.getVariable1());
				nodeY = probNet.getNode(removeLinkEdit.getVariable2());
				List<Node> separationSet = cache.get(nodeX).get(nodeY).getSeparationSet();
				linkScore = independenceTester.test(caseDatabase, nodeX, nodeY, separationSet);
				cache.get(nodeX).put(nodeY, new PCEditMotivation(linkScore, separationSet));
			} else if (edit instanceof AddLinkEdit) {
				AddLinkEdit addLinkEdit = (AddLinkEdit) edit;
				nodeX = probNet.getNode(addLinkEdit.getVariable1());
				nodeY = probNet.getNode(addLinkEdit.getVariable2());
				probNet.removeLink(nodeX, nodeY, false);
				phase = INITIAL_PHASE;
			} else if (edit instanceof COrientLinksEdit) {
				phase = INITIAL_PHASE;
			} else if (edit instanceof OrientLinkEdit) {
				phase = HEAD_TO_HEAD_ORIENTATION;
			}
			resetHistory();
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void undoableEditHappened(UndoableEditEvent event) {

		UndoableEdit edit = event.getEdit();
		Node nodeX, nodeY;

		if (edit instanceof RemoveLinkEdit) {
			RemoveLinkEdit removeLinkEdit = (RemoveLinkEdit) edit;
			nodeX = probNet.getNode(removeLinkEdit.getVariable1());
			nodeY = probNet.getNode(removeLinkEdit.getVariable2());
			List<Node> separationSet = new ArrayList<>();
			PCEditMotivation cachedScore = cache.get(nodeX).get(nodeY);
			if (cachedScore != null) {
				separationSet = cachedScore.getSeparationSet();
			}
			cache.get(nodeX).put(nodeY, new PCEditMotivation(ALREADY_DONE, separationSet));
			cache.get(nodeY).put(nodeX, new PCEditMotivation(ALREADY_DONE, separationSet));
			// Remove the cached values X node's neighbors that contained Y in
			// the separation set (and vice versa)
			for (Node neighborNode : nodeX.getNeighbors()) {
				PCEditMotivation neighborScore = cache.get(nodeX).get(neighborNode);
				if (neighborScore != null && neighborScore.getScore() != ALREADY_DONE && neighborScore
						.getSeparationSet().contains(nodeY)) {
					cache.get(nodeX).remove(neighborNode);
				}
			}
			for (Node neighborNode : nodeY.getNeighbors()) {
				PCEditMotivation neighborScore = cache.get(nodeY).get(neighborNode);
				if (neighborScore != null && neighborScore.getScore() != ALREADY_DONE && neighborScore
						.getSeparationSet().contains(nodeX)) {
					cache.get(nodeY).remove(neighborNode);
				}
			}

		}
		//An AddLinkEdit can only be done by the user. Just undirect the link
		if (edit instanceof AddLinkEdit) {
			AddLinkEdit addLinkEdit = (AddLinkEdit) edit;
			nodeX = probNet.getNode(addLinkEdit.getVariable1());
			nodeY = probNet.getNode(addLinkEdit.getVariable2());
			probNet.removeLink(nodeX, nodeY, true);
			probNet.addLink(nodeX, nodeY, false);
			phase = INITIAL_PHASE;
		} else if (edit instanceof COrientLinksEdit) {

		}
		resetHistory();
	}

	public LearningEditMotivation getMotivation(PNEdit edit) {
		Node nodeX, nodeY, nodeZ;
		LearningEditMotivation motivation = null;
		if (edit instanceof RemoveLinkEdit) {
			RemoveLinkEdit removeLinkEdit = (RemoveLinkEdit) edit;
			nodeX = probNet.getNode(removeLinkEdit.getVariable1());
			nodeY = probNet.getNode(removeLinkEdit.getVariable2());
			motivation = cache.get(nodeX).get(nodeY);

		} else if (edit instanceof COrientLinksEdit) {
			try {
				COrientLinksEdit compoundDirectLinkEdit = (COrientLinksEdit) edit;
				nodeX = probNet.getNode(((OrientLinkEdit) compoundDirectLinkEdit.getEdits().get(0)).getVariable1());
				nodeZ = probNet.getNode(((OrientLinkEdit) compoundDirectLinkEdit.getEdits().get(0)).getVariable2());
				nodeY = probNet.getNode(((OrientLinkEdit) compoundDirectLinkEdit.getEdits().get(1)).getVariable1());
				motivation = new StringEditMotivation(
						"Sep. set (" + nodeX.getName() + ", " + nodeY.getName() + ") does not contain variable: "
								+ nodeZ.getName());
			} catch (NonProjectablePotentialException | WrongCriterionException e) {
				e.printStackTrace();
			}
		}
		if (edit instanceof OrientLinkEdit) {
			motivation = new StringEditMotivation("Do not create cycles");
		}
		return motivation;
	}

	@Override public boolean isLastPhase() {
		return (phase >= REMAINING_LINKS_ORIENTATION);
	}

	protected void resetHistory() {
		phase = INITIAL_PHASE;
		lastRemovedEdits.clear();
		lastOrientationEdits.clear();
		lastCompoundOrientationEdits.clear();
	}

}