/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.algorithm.pc;

import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.util.ModelNetUse;

/**
 * This class implements the basic structure of any algorithm based on the
 * independence relations approach.
 * The particular behavior of each algorithm is given by the
 * <code>editionsGenerator</code> class.
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
public abstract class IndependenceRelationsAlgorithm extends LearningAlgorithm {

	protected boolean undirectedStructureFound = false;

	public IndependenceRelationsAlgorithm(ProbNet probNet, CaseDatabase caseDatabase, double alpha) {
		super(probNet, caseDatabase, alpha);
	}

	/**
	 * Initializes the algorithm depending on the model net use selected
	 * by the user.
	 *
	 * @param modelNetUse
	 */
	@Override public void init(ModelNetUse modelNetUse) {

		/* If the user allowed links addition, the algorithm starts
		 * with the complete graph
		 */
		if (modelNetUse != null && (modelNetUse.isLinkAdditionAllowed() || modelNetUse.isUseNodePositions())) {
			for (Link<Node> link : probNet.getLinks()) {
				probNet.removeLink(link);
			}
			probNet.marry(probNet.getNodes());
		}
		/* If the user allowed only links deletion, the algorithm starts
		 * with the structure of the model net.
		 */
		else if (modelNetUse != null && (modelNetUse.isLinkRemovalAllowed() || modelNetUse.isLinkInversionAllowed())) {
			for (Link<Node> link : probNet.getLinks()) {
				probNet.removeLink(link);
				probNet.addLink(link.getNode1(), link.getNode2(), false);
			}
		} else {
			for (Link<Node> link : probNet.getLinks()) {
				probNet.removeLink(link);
			}
			probNet.marry(probNet.getNodes());
		}

	}

}
