/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.mode;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.util.Utilities;
import org.openmarkov.gui.window.edition.EditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

@EditionState(name = "Edit.Mode.Link", icon = "link.gif", cursor = "link.gif") public class LinkEditionMode
		extends EditionMode {

	public LinkEditionMode(EditorPanel editorPanel, ProbNet probNet) {
		super(editorPanel, probNet);
	}

	@Override public void mousePressed(MouseEvent e, Point2D.Double cursorPosition, Graphics2D g) {

		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
			if (Utilities.noMouseModifiers(e)) {
				visualNetwork.startLinkCreation(cursorPosition, g);
			}
		}
	}

	@Override public void mouseReleased(MouseEvent e, Point2D.Double position, Graphics2D g) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			PNEdit linkEdit = visualNetwork.finishLinkCreation(position, g);
			if (linkEdit != null) {
				try {
					probNet.doEdit(linkEdit);
				} catch (Exception ex) {
					String message = ex.getMessage();
					if (ex.getMessage() == null || ex.getMessage() == "") {
						message = "This link can't be created.";
					}
					JOptionPane.showMessageDialog(Utilities.getOwner(editorPanel), message,
							"Error while creating link", JOptionPane.ERROR_MESSAGE);
				}
			}
			editorPanel.repaint();
		}
	}

	@Override public void mouseDragged(MouseEvent e, Point2D.Double cursorPosition, double diffX, double diffY,
			Graphics2D g) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			visualNetwork.updateLinkCreation(cursorPosition);
			editorPanel.repaint();
		}

	}
}
