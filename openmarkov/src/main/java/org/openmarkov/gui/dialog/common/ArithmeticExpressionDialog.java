/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.openmarkov.core.model.network.Variable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial") public class ArithmeticExpressionDialog extends OkCancelHorizontalDialog
		implements DocumentListener {

	private static final Color VALID_EXPRESSION_COLOR = new Color(180, 215, 170);
	private static final Color INVALID_EXPRESSION_COLOR = new Color(250, 170, 170);
	private JTextField expressionTextField;
	private JList<String> variableList;
	private JList<String> functionList;
	private JButton helpButton;
	private String expression;
	private Evaluator evaluator;
	private List<Variable> variables;
	private List<String> functionNames = Arrays
			.asList("abs", "acos", "asin", "atan", "atan2", "ceil", "cos", "exp", "log", "max", "min", "pow", "round",
					"sin", "sqrt", "tan", "toDegrees", "toRadians");

	public ArithmeticExpressionDialog(Window owner, List<Variable> variables, String expression) {
		super(owner);
		this.variables = variables;
		setTitle(stringDatabase.getString("ArithmeticExpressionEvaluator.Title"));
		setIconImage(null);
		this.expression = expression;
		evaluator = new Evaluator();
		Map<String, String> variableValues = new HashMap<>();
		for (int i = 0; i < variables.size(); i++) {
			variableValues.put("v" + i, "1.0");
		}
		evaluator.setVariables(variableValues);
		initializeComponents();
		setLocationRelativeTo(null);
		expressionTextField.getDocument().addDocumentListener(this);
		expressionTextField.setBackground((isValidExpression()) ? VALID_EXPRESSION_COLOR : INVALID_EXPRESSION_COLOR);
		DefaultListModel<String> variableListModel = new DefaultListModel<>();
		for (Variable variable : variables) {
			variableListModel.addElement(variable.getName());
		}
		variableList.setModel(variableListModel);
		variableList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getClickCount() > 1) {
					insertTextInExpression("{" + variableList.getSelectedValue() + "}");
				}
			}
		});
		variableList.setToolTipText(stringDatabase.getString("ArithmeticExpressionEvaluator.Instructions.Variables"));
		DefaultListModel<String> functionListModel = new DefaultListModel<>();
		Collections.sort(functionNames);
		for (String functionName : functionNames) {
			functionListModel.addElement(functionName);
		}

		functionList.setModel(functionListModel);
		functionList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getClickCount() > 1) {
					insertTextInExpression(functionList.getSelectedValue() + "()");
				}
			}
		});
		functionList.setToolTipText(stringDatabase.getString("ArithmeticExpressionEvaluator.Instructions.Functions"));

		helpButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JEditorPane ed1 = new JEditorPane("text/html",
						stringDatabase.getString("ArithmeticExpressionEvaluator.Help"));
				ed1.setCaretPosition(0);
				ed1.setEditable(false);
				JScrollPane scrollPane = new JScrollPane(ed1);
				JDialog helpDialog = new JDialog(null, stringDatabase.getString("Help.Help.Label"),
						ModalityType.APPLICATION_MODAL);
				helpDialog.add(scrollPane);
				helpDialog.setSize(600, 600);
				helpDialog.setLocationRelativeTo(null);
				helpDialog.setVisible(true);
				helpDialog.setIconImage(null);
			}
		});

	}

	public ArithmeticExpressionDialog(Window owner, List<Variable> variables) {
		this(owner, variables, null);
	}

	private void initializeComponents() {
		JPanel expressionPanel = new JPanel();
		expressionPanel.setLayout(new BorderLayout());
		JPanel helpPanel = new JPanel();
		helpPanel.setLayout(new BorderLayout());
		helpButton = new JButton(stringDatabase.getString("Help.Help.Label"));
		helpButton.setMaximumSize(new Dimension(40, 20));
		helpPanel.add(helpButton, BorderLayout.LINE_END);
		expressionPanel.add(helpPanel, BorderLayout.NORTH);
		expressionTextField = new JTextField();
		expressionTextField.setPreferredSize(new Dimension(400, 20));
		if (expression != null) {
			expressionTextField.setText(expression);
		}
		expressionPanel.add(expressionTextField, BorderLayout.CENTER);
		JPanel listPanel = new JPanel();
		variableList = new JList<>();
		JScrollPane variableListScroller = new JScrollPane(variableList);
		variableListScroller.setPreferredSize(new Dimension(175, 150));
		JLabel variableListLabel = new JLabel(
				stringDatabase.getString("ArithmeticExpressionEvaluator.Variables.Label"));
		JPanel variableListPanel = new JPanel();
		variableListPanel.setLayout(new BorderLayout());
		variableListPanel.add(variableListLabel, BorderLayout.NORTH);
		variableListPanel.add(variableListScroller, BorderLayout.CENTER);

		functionList = new JList<>();
		JScrollPane functionListScroller = new JScrollPane(functionList);
		functionListScroller.setPreferredSize(new Dimension(175, 150));
		JLabel functionListLabel = new JLabel(
				stringDatabase.getString("ArithmeticExpressionEvaluator.Functions.Label"));
		JPanel functionListPanel = new JPanel();
		functionListPanel.setLayout(new BorderLayout());
		functionListPanel.add(functionListLabel, BorderLayout.NORTH);
		functionListPanel.add(functionListScroller, BorderLayout.CENTER);

		listPanel.add(variableListPanel);
		listPanel.add(functionListPanel);
		expressionPanel.add(listPanel, BorderLayout.SOUTH);
		getComponentsPanel().add(expressionPanel, BorderLayout.NORTH);
		pack();
	}

	@Override protected boolean doOkClickBeforeHide() {
		expression = expressionTextField.getText();
		return isValidExpression();
	}

	@Override protected void doCancelClickBeforeHide() {
		expression = null;
	}

	public String getExpression() {
		return expression;
	}

	private boolean isValidExpression() {
		String processedExpression = processExpression(expressionTextField.getText());
		boolean result = false;
		try {
			evaluator.evaluate(processedExpression);
			result = true;
		} catch (EvaluationException e) {
			// Ignore
		}
		return result;
	}

	private String processExpression(String expression) {
		String processedExpression = expression;
		for (int i = 0; i < variables.size(); i++) {
			processedExpression = processedExpression.replace("{" + variables.get(i).getName() + "}", "#{v" + i + "}");
		}
		for (int i = 0; i < variables.size(); i++) {
			processedExpression = processedExpression.replace(variables.get(i).getName(), "#{v" + i + "}");
		}
		return processedExpression;
	}

	private void insertTextInExpression(String text) {
		try {
			expressionTextField.getDocument().insertString(expressionTextField.getCaretPosition(), text, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override public void insertUpdate(DocumentEvent e) {
		expressionTextField.setBackground((isValidExpression()) ? VALID_EXPRESSION_COLOR : INVALID_EXPRESSION_COLOR);
	}

	@Override public void removeUpdate(DocumentEvent e) {
		expressionTextField.setBackground((isValidExpression()) ? VALID_EXPRESSION_COLOR : INVALID_EXPRESSION_COLOR);
	}

	@Override public void changedUpdate(DocumentEvent e) {
		expressionTextField.setBackground((isValidExpression()) ? VALID_EXPRESSION_COLOR : INVALID_EXPRESSION_COLOR);
	}
}
