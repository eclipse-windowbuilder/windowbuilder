/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.gef.part.nonvisual;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.draw2d.Label;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * A figure that can display text and image.
 *
 * @author lobas_av
 * @coverage core.gef.nonvisual
 */
public class BeanFigure extends Figure {
	private final ImageDescriptor m_imageDescriptor;
	private final Label m_label = new Label();
	private final Point m_imageLocation = new Point();
	private final Dimension m_imageSize;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	@Deprecated
	public BeanFigure(Image image) {
		this(new ImageImageDescriptor(image));
	}

	public BeanFigure(ImageDescriptor imageDescriptor) {
		final ImageData imageData = imageDescriptor.getImageData(100);
		m_imageDescriptor = imageDescriptor;
		m_imageSize = new Dimension(imageData.width, imageData.height);
		add(m_label);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void update(String text, Point location) {
		if (m_label.getText().equals(text)) {
			setLocation(location);
		} else {
			// configure text
			m_label.setText(text);
			Dimension textSize = m_label.getPreferredSize();
			// calculate total width
			int width = Math.max(m_imageSize.width, textSize.width);
			// set all bounds
			setBounds(new Rectangle(location.x, location.y, width, m_imageSize.height + textSize.height));
			m_imageLocation.x = width / 2 - m_imageSize.width / 2;
			m_label.setBounds(new Rectangle(width / 2 - textSize.width / 2,
					m_imageSize.height,
					textSize.width,
					textSize.height));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		Image image = m_imageDescriptor.createImage();
		graphics.drawImage(image, m_imageLocation);
		image.dispose();
	}
}