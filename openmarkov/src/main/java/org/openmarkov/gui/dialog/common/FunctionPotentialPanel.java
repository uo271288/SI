/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.PotentialChangeEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.FunctionPotential;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@SuppressWarnings("serial") @PotentialPanelPlugin(potentialType = "Function") public class FunctionPotentialPanel
		extends PotentialPanel {

	/**
	 * Panel with the function
	 */

	protected JTextArea functionTextArea = null;
	/**
	 *
	 */
	protected String function;
	/**
	 * Variables list
	 */

	protected List<Variable> variables;
	/**
	 * Parents list
	 */
	protected List<Variable> parents;
	private JPanel functionPanel;
	private Node node = null;
	private FunctionPotential potential = null;

	public FunctionPotentialPanel(Node node) {
		super();
		initComponents();
		setData(node);
	}

	private void initComponents() {

		setLayout(new BorderLayout());
		JPanel northPanel = new JPanel();
		northPanel.setBorder(new TitledBorder("Function"));
		northPanel.setPreferredSize(new Dimension(800, 100));
		// String function= (potential.getCovariates()==null)?null:potential.getCovariates()[0];
		functionPanel = new JPanel();
		functionPanel.setLayout(new BorderLayout());
		functionPanel.setPreferredSize(new Dimension(750, 50));
		functionPanel.setBorder(new LineBorder(UIManager.getColor("Table.dropLineColor"), 1, false));
		functionPanel.add(getFunctionTextArea());
		functionTextArea.addMouseListener(new FunctionTextAreaMouseListener());

		northPanel.add(functionPanel, BorderLayout.NORTH);
		add(northPanel, BorderLayout.NORTH);
	}

	protected JTextArea getFunctionTextArea() {
		if (functionTextArea == null) {
			functionTextArea = new JTextArea();
			functionTextArea.setEditable(false);
		}
		return functionTextArea;
	}

	@Override public void setData(Node node) {
		this.node = node;
		this.potential = (FunctionPotential) this.node.getPotentials().get(0);
		this.variables = potential.getVariables();
		this.parents = variables.subList(1, variables.size());
		this.function = potential.getFunction();
		if (function == null) {
			function = FunctionPotential.DEFAULT_FUNCTION;
		}
		functionTextArea.setText(function);
	}

	public String getFunction() {
		return function;
	}

	public boolean saveChanges() {
		FunctionPotential newPotential = (FunctionPotential) this.potential.copy();

		newPotential.setFunction(function);

		PotentialChangeEdit potentialChangeEdit = new PotentialChangeEdit(node.getProbNet(), this.potential,
				newPotential);
		try {
			node.getProbNet().doEdit(potentialChangeEdit);
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override public void close() {

	}

	private class FunctionTextAreaMouseListener extends MouseAdapter {
		@Override public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				ArithmeticExpressionDialog expressionDialog = new ArithmeticExpressionDialog(null, parents, function);
				expressionDialog.setVisible(true);
				if (expressionDialog.getSelectedButton() == OkCancelHorizontalDialog.OK_BUTTON) {
					function = expressionDialog.getExpression();
					functionTextArea.setText(function);
				}
			}
		}
	}

}
