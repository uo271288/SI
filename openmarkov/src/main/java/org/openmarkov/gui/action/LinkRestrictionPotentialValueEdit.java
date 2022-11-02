/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.potential.TablePotential;

@SuppressWarnings("serial") public class LinkRestrictionPotentialValueEdit extends SimplePNEdit {

	/**
	 * The column of the table where is the potential
	 */
	private int col;
	/**
	 * The row of the table where is the potential
	 */
	private int row;
	/**
	 * The new value of the potential
	 */
	private Integer newValue;

	/***
	 * The link with the link restriction potential.
	 */
	private Link<Node> link;
	/****
	 * The parent node of the link.
	 */
	private Node node1;
	/****
	 * The child node of the link.
	 */
	private Node node2;

	/**
	 * the table potential before the edit
	 */
	private double[] lastTable;

	/***
	 * the table potential after the edit
	 */
	private double[] newTable;

	/**
	 * The potential of the link restriction
	 */
	private TablePotential tablePotential;

	public LinkRestrictionPotentialValueEdit(Link<Node> link, Integer newValue, int row, int col) {
		super(link.getNode1().getProbNet());
		this.link = link;
		this.node1 = link.getNode1();
		this.node2 = link.getNode2();
		this.col = col;
		this.row = row;
		this.tablePotential = (TablePotential) link.getRestrictionsPotential();
		this.newValue = newValue;
		this.lastTable = ((TablePotential) link.getRestrictionsPotential()).getValues().clone();
	}

	@Override public void doEdit() throws DoEditException {
		int numStates2 = node2.getVariable().getNumStates();
		int stateIndex1 = col - 1;
		int stateIndex2 = numStates2 - row;
		State state1 = node1.getVariable().getStates()[stateIndex1];
		State state2 = node2.getVariable().getStates()[stateIndex2];
		link.setCompatibilityValue(state1, state2, this.newValue.intValue());
		newTable = ((TablePotential) link.getRestrictionsPotential()).values.clone();

	}

	public void redo() {
		this.setTypicalRedo(false);
		super.redo();
		if (!link.hasRestrictions()) {
			link.initializesRestrictionsPotential();
			this.tablePotential = (TablePotential) link.getRestrictionsPotential();
		}
		tablePotential.setValues(newTable);
		checkRestrictionPotential(newTable);
	}

	public void undo() {
		super.undo();
		if (!link.hasRestrictions()) {
			link.initializesRestrictionsPotential();
			this.tablePotential = (TablePotential) link.getRestrictionsPotential();
		}
		tablePotential.setValues(lastTable);
		checkRestrictionPotential(lastTable);
	}

	public TablePotential getPotential() {
		return tablePotential;
	}

	/**
	 * Gets the row position associated to value edited if priorityList no
	 * exists
	 *
	 * @return the position in the table
	 */
	public int getRowPosition() {
		return row;
	}

	/**
	 * Gets the column where the value is edited
	 *
	 * @return the column edited
	 */
	public int getColumnPosition() {
		return col;
	}

	public Integer getNewValue() {
		return newValue;
	}

	public void checkRestrictionPotential(double[] table) {
		boolean hasRestriction = false;

		for (int i = 0; i < table.length && !hasRestriction; i++) {
			if (table[i] == 0) {
				hasRestriction = true;
			}
		}
		if (!hasRestriction) {
			tablePotential = null;
			this.link.setRestrictionsPotential(tablePotential);
		}

	}

}
