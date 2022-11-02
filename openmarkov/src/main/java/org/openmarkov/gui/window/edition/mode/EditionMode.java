/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.mode;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.window.edition.EditorPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * This class the defines the behaviour of the editor panel in a certain edition
 * state such as selection, node creation, link creation, etc.
 *
 * @author ibermejo
 */
public abstract class EditionMode {

	protected EditorPanel editorPanel;
	protected VisualNetwork visualNetwork;
	protected ProbNet probNet;

	public EditionMode(EditorPanel editorPanel, ProbNet probNet) {
		this.editorPanel = editorPanel;
		this.visualNetwork = editorPanel.getVisualNetwork();
		this.probNet = probNet;
	}

	public abstract void mousePressed(MouseEvent e, Point2D.Double position, Graphics2D g);

	public abstract void mouseReleased(MouseEvent e, Point2D.Double position, Graphics2D g);

	public abstract void mouseDragged(MouseEvent e, Point2D.Double position, double diffX, double diffY, Graphics2D g);
}
