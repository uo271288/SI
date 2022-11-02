/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.gui.dialog.common.OkCancelHorizontalDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") public class DistributionParameterDialog extends OkCancelHorizontalDialog {

	private double[] parameters = null;
	private List<TextField> parameterTextFields = null;
	public DistributionParameterDialog(Window owner, String distributionType, double[] parameters) {
		super(owner);
		this.parameters = parameters;
		ProbDensFunctionManager distrManager = ProbDensFunctionManager.getUniqueInstance();
		String[] parameterNames = distrManager.getParameters(distributionType);
		this.parameterTextFields = new ArrayList<>(parameterNames.length);

		setTitle(distributionType);

		BorderLayout layout = new BorderLayout(5, 5);
		getComponentsPanel().setLayout(layout);

		JPanel parametersPanel = new JPanel(new GridLayout(parameterNames.length, 1, 5, 3));
		parametersPanel.setBorder(new TitledBorder("Parameters"));

		for (int i = 0; i < parameterNames.length; ++i) {
			JLabel parameterLabel = new JLabel(parameterNames[i]);
			TextField parameterTextField = new TextField(5);
			if (parameters != null) {
				parameterTextField.setText(String.valueOf(parameters[i]));
			}
			parameterTextField.addFocusListener(new TextFieldFocusListener());
			parametersPanel.add(parameterLabel);
			parametersPanel.add(parameterTextField);
			parameterTextFields.add(parameterTextField);
		}
		getComponentsPanel().add(parametersPanel, BorderLayout.NORTH);
		pack();

		Point parentLocation = owner.getLocation();
		Dimension parentSize = owner.getSize();
		int x = (int) (parentLocation.getX() + parentSize.getWidth() / 2 - getSize().getWidth() / 2);
		int y = (int) (parentLocation.getY() + parentSize.getHeight() / 2 - getSize().getHeight() / 2);
		setLocation(new Point(x, y));

		if (parameterTextFields.isEmpty()) {
			this.setVisible(false);
		}
	}

	public DistributionParameterDialog(Window owner, String distributionType) {
		this(owner, distributionType, null);
	}

	@Override protected boolean doOkClickBeforeHide() {
		if (parameters == null) {
			parameters = new double[parameterTextFields.size()];
		}
		for (int i = 0; i < parameterTextFields.size(); ++i) {
			TextField parameterTextField = parameterTextFields.get(i);
			double parameterValue = Double.parseDouble(parameterTextField.getText());
			parameters[i] = parameterValue;
		}
		return true;
	}

	@Override protected void doCancelClickBeforeHide() {
		if (parameters == null) {
			parameters = new double[parameterTextFields.size()];
		}
	}

	public double[] getParameters() {
		return parameters;
	}

	private class TextFieldFocusListener implements FocusListener {
		@Override public void focusGained(FocusEvent e) {
			((TextComponent) e.getSource()).selectAll();

		}

		@Override public void focusLost(FocusEvent e) {
			// ignore

		}

	}

}
