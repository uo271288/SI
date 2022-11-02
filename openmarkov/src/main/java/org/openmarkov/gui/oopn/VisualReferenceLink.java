/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.oopn;

import org.openmarkov.core.oopn.ReferenceLink;
import org.openmarkov.gui.graphic.Segment;
import org.openmarkov.gui.graphic.VisualArrow;
import org.openmarkov.gui.graphic.VisualElement;

import java.awt.*;

public class VisualReferenceLink extends VisualArrow {

	/**
	 * Used to paint normal lines.
	 */
	protected static final BasicStroke NORMAL_INSTANCE_LINK_STROKE = new BasicStroke(3.0f);

	/**
	 * Used to paint wide lines.
	 */
	protected static final BasicStroke SELECTED_INSTANCE_LINK_STROKE = new BasicStroke(5.0f);

	private VisualElement sourceElement;
	private VisualElement destinationElement;

	/**
	 * ReferenceLink this VisualLink represents
	 */
	private ReferenceLink referenceLink;

	public VisualReferenceLink(ReferenceLink referenceLink, VisualElement sourceElement,
			VisualElement destinationElement) {
		super(sourceElement.getPosition(), destinationElement.getPosition());
		this.referenceLink = referenceLink;
		this.sourceElement = sourceElement;
		this.destinationElement = destinationElement;
	}

	/**
	 * Returns the shape of the arrow so that it can be selected with the mouse.
	 *
	 * @return shape of the arrow.
	 */
	@Override public Shape getShape(Graphics2D g) {

		setStartPoint(
				sourceElement.getCutPoint(new Segment(sourceElement.getCenter(), destinationElement.getCenter()), g));
		setEndPoint(destinationElement
				.getCutPoint(new Segment(destinationElement.getCenter(), sourceElement.getCenter()), g));
		return super.getShape(g);
	}

	/**
	 * Paints the visual link into the graphics object.
	 *
	 * @param g graphics object where paint the link.
	 */
	@Override public void paint(Graphics2D g) {

		setStartPoint(
				sourceElement.getCutPoint(new Segment(sourceElement.getCenter(), destinationElement.getCenter()), g));
		setEndPoint(destinationElement
				.getCutPoint(new Segment(destinationElement.getCenter(), sourceElement.getCenter()), g));

		super.paint(g);
	}

	@Override protected Stroke getStroke() {
		return (isSelected()) ? WIDE_DASHED_STROKE : NORMAL_DASHED_STROKE;
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(sourceElement.toString());
		sb.append(" |--> ");
		sb.append(destinationElement.toString());

		return sb.toString();
	}

	public VisualElement getSourceElement() {
		return sourceElement;
	}

	public VisualElement getDestinationElement() {
		return destinationElement;
	}

	public ReferenceLink getReferenceLink() {
		return referenceLink;
	}

}
