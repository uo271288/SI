/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;

@SuppressWarnings("serial") public class NodeDecisionCriteriaEdit extends SimplePNEdit {

	private Criterion currentDecisionCriteria;
	private Criterion newDecisionCriteria;
	private Node node;

	public NodeDecisionCriteriaEdit(Node node, Criterion decisionCriteria) {
		super(node.getProbNet());
		this.node = node;
		this.currentDecisionCriteria = node.getVariable().getDecisionCriterion();
		this.newDecisionCriteria = decisionCriteria;
	}

	@Override public void doEdit() throws DoEditException {
		node.getVariable().setDecisionCriterion(newDecisionCriteria);
	}

	@Override public void undo() {
		super.undo();
		node.getVariable().setDecisionCriterion(currentDecisionCriteria);
	}

}

