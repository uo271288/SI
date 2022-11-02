/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.oopn;

import org.openmarkov.gui.graphic.Segment;
import org.openmarkov.gui.graphic.VisualArrow;
import org.openmarkov.gui.graphic.VisualNode;

import java.awt.*;

public class VisualContractedNodeLink extends VisualArrow {
	VisualInstance sourceInstance = null;
	VisualNode destNode = null;

	public VisualContractedNodeLink(VisualInstance sourceInstance, VisualNode destNode) {
		super(sourceInstance.getPosition(), destNode.getPosition());
		this.sourceInstance = sourceInstance;
		this.destNode = destNode;
	}

	/**
	 * Returns the shape of the arrow so that it can be selected with the mouse.
	 *
	 * @return shape of the arrow.
	 */
	@Override public Shape getShape(Graphics2D g) {

		setStartPoint(sourceInstance.getCutPoint(new Segment(sourceInstance.getCenter(), destNode.getCenter()), g));
		setEndPoint(destNode.getCutPoint(new Segment(destNode.getCenter(), sourceInstance.getCenter()), g));
		return super.getShape(g);
	}

	/**
	 * Paints the visual link into the graphics object.
	 *
	 * @param g graphics object where paint the link.
	 */
	@Override public void paint(Graphics2D g) {

		setStartPoint(sourceInstance.getCutPoint(new Segment(sourceInstance.getCenter(), destNode.getCenter()), g));
		setEndPoint(destNode.getCutPoint(new Segment(destNode.getCenter(), sourceInstance.getCenter()), g));

		super.paint(g);
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(sourceInstance.toString());
		sb.append(" |--> ");
		sb.append(destNode.toString());

		return sb.toString();
	}
}
