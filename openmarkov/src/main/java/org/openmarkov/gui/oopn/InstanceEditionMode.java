/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.oopn;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.oopn.OOPNet;
import org.openmarkov.core.oopn.action.AddInstanceEdit;
import org.openmarkov.gui.util.Utilities;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.EditorPanel;
import org.openmarkov.gui.window.edition.NetworkPanel;
import org.openmarkov.gui.window.edition.mode.EditionMode;
import org.openmarkov.gui.window.edition.mode.EditionState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

@EditionState(name = "Edit.Mode.Instance", icon = "instance.gif", cursor = "instance.gif") public class InstanceEditionMode
		extends EditionMode {

	public InstanceEditionMode(EditorPanel editorPanel, ProbNet probNet) {
		super(editorPanel, probNet);
	}

	@Override public void mousePressed(MouseEvent e, Point2D.Double position, Graphics2D g) {
		if (SwingUtilities.isLeftMouseButton(e) && Utilities.noMouseModifiers(e)) {
			if (visualNetwork.getElementInPosition(position, g) == null) {
				probNet.getPNESupport().setWithUndo(true);
				String selectedClassFrameTitle = getClassComboBox().getSelectedClassFrameTitle();
				NetworkPanel instanceNetworkPanel = (
						(NetworkPanel) MainPanel.getUniqueInstance().getMdi().getFrameByTitle(selectedClassFrameTitle)
				);
				if (instanceNetworkPanel != null) {
					ProbNet classNet = instanceNetworkPanel.getProbNet();
					String instanceName = JOptionPane.showInputDialog(null, "Instance Name:");

					if (instanceName != null) {
						AddInstanceEdit addInstanceEdit = new AddInstanceEdit((OOPNet) probNet, classNet, instanceName,
								position);
						try {
							probNet.doEdit(addInstanceEdit);
						} catch (Exception e1) {
							// TODO Localize
							JOptionPane.showMessageDialog(null, "Error while generating instance node.\n"
											+ "Look in the message window for more details", "Error",
									JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						}
						editorPanel.adjustPanelDimension();
						editorPanel.repaint();
					}
				}
			} else {
				visualNetwork.selectElementInPosition(position, g);
			}
		}
	}

	private ClassComboBox getClassComboBox() {
		ClassComboBox classComboBox = null;

		for (Component toolbar : MainPanel.getUniqueInstance().getToolBarPanel().getComponents()) {
			if (toolbar instanceof OOToolBar) {
				classComboBox = ((OOToolBar) toolbar).getClassComboBox();
			}
		}
		return classComboBox;
	}

	@Override public void mouseReleased(MouseEvent e, Double position, Graphics2D g) {
		// TODO Auto-generated method stub

	}

	@Override public void mouseDragged(MouseEvent e, Double position, double diffX, double diffY, Graphics2D g) {
		// TODO Auto-generated method stub

	}

}
