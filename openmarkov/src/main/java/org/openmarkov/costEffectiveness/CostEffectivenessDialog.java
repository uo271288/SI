/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.costEffectiveness;

import org.apache.commons.io.FilenameUtils;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.dialog.common.OkCancelHorizontalDialog;
import org.openmarkov.gui.dialog.inference.common.ScopeSelectorPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Input dialog for cost effectiveness purposes used to introduce relevant
 * information such as cycle length, number of cycles, introduce findings to
 * numerical variables within the network, cost and effectiveness discount
 * rate...
 *
 * @author myebra
 */
public class CostEffectivenessDialog extends OkCancelHorizontalDialog {
	private static final long serialVersionUID = 1L;
	private ProbNet probNet;
	private ScopeSelectorPanel scopeSelectorPanel;
	private EvidenceCase preResolutionEvidence;

	/**
	 * Creates a CostEffectivenessDialog for temporal evolution
	 *
	 * @param owner The parent of the dialog
	 */
	public CostEffectivenessDialog(Window owner, ProbNet probNet, EvidenceCase preResolutionEvidence) {
		super(owner);
		this.probNet = probNet;
		this.preResolutionEvidence = preResolutionEvidence;
		initialize();
		setResizable(true);
		setTitle(probNet.getName());
		pack();
		Point parentLocation = owner.getLocation();
		Dimension parentSize = owner.getSize();
		int x = (int) (parentLocation.getX() + parentSize.getWidth() / 2 - getSize().getWidth() / 2);
		int y = (int) (parentLocation.getY() + parentSize.getHeight() / 2 - getSize().getHeight() / 2);
		setLocation(new Point(x, y));
		repaint();
	}

	private void initialize() {
		setMinimumSize(new Dimension(300, 300));
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JPanel otherPanel = new JPanel();
		otherPanel.setLayout(new BorderLayout());
		if (scopeSelectorPanel == null) {
			scopeSelectorPanel = new ScopeSelectorPanel(probNet, preResolutionEvidence);
		}
		otherPanel.add(scopeSelectorPanel);

		panel.add(otherPanel, BorderLayout.NORTH);

		getComponentsPanel().setLayout(new BorderLayout(20, 0));
		getComponentsPanel().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getComponentsPanel().add(panel, BorderLayout.NORTH);

		pack();
		repaint();
	}

	public int requestData() {
		setVisible(true);
		return selectedButton;
	}

	@Override protected boolean doOkClickBeforeHide() {

		return true;
	}

	public void setTitle(String netName) {
		String title = stringDatabase.getString("CostEffectivenessResults.Scope");
		super.setTitle(title + " - " + FilenameUtils.getBaseName(netName));
	}

	public ScopeSelectorPanel getScopeSelectorPanel() {
		return scopeSelectorPanel;
	}

}
