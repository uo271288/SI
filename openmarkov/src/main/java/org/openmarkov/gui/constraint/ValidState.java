/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.constraint;

import org.openmarkov.core.action.NodeStateEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.StateAction;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.constraint.UtilConstraints;

import java.util.List;

/**
 * Checks that the state field is filled and there isn't any node with the same
 * name.
 */
public class ValidState extends PNConstraint {
	// Attributes.
	private String message;

	public boolean checkEdit(ProbNet probNet, PNEdit edit)
			throws NonProjectablePotentialException, WrongCriterionException {
		List<PNEdit> edits = UtilConstraints.getSimpleEditsByType(edit, NodeStateEdit.class);
		for (PNEdit simpleEdit : edits) {
			State state = ((NodeStateEdit) simpleEdit).getNewState();
			State currentState = ((NodeStateEdit) simpleEdit).getLastState();
			Node node = ((NodeStateEdit) simpleEdit).getNode();
			StateAction stateAction = ((NodeStateEdit) simpleEdit).getStateAction();
			// if ((name == null) || (name.contentEquals(""))) {
			if (!checkState(state.getName(), currentState.getName(), node, stateAction)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method checks that the state field is filled and there isn't any
	 * node with the same name.
	 *
	 * @return true, if the state field isn't empty and there isn't any node with
	 * this name; otherwise, false.
	 */
	public boolean checkState(String newState, String currentState, Node node, StateAction stateAction) {
		switch (stateAction) {
		case RENAME:
		case ADD:
			if ((newState == null) || newState.equals("")) {
				message = "NodeStateEmpty.Text.Label";
				return false;
			} else if (existState(newState, node)) {
				message = "DuplicatedState.Text.Label";
				return false;
			}
			break;
		case REMOVE:
		}
		return true;
	}

	/**
	 * This method checks if exists the state specified.
	 *
	 * @param node  node to search.
	 * @param state state to be checked
	 * @return true if the state exists; otherwise, false.
	 */
	public boolean existState(String state, Node node) {
		for (State states : node.getVariable().getStates()) {
			if (states.getName().toUpperCase().equals(state.toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	public boolean checkProbNet(ProbNet probNet) {
		List<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			String name = variable.getName();
			if ((name == null) || (name.contentEquals(""))) {
				return false;
			}
		}
		return true;
	}

	@Override protected String getMessage() {
		return message;
	}
}
