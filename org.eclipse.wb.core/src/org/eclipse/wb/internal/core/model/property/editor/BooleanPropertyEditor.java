/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.editor;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;

/**
 * The {@link PropertyEditor} for <code>boolean</code>.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class BooleanPropertyEditor extends PropertyEditor {
	private static final Image m_trueImage = CoreImages.getSharedImage(CoreImages.PROPERTIES_TRUE);
	private static final Image m_falseImage = CoreImages.getSharedImage(CoreImages.PROPERTIES_FALSE);
	private static final Image m_unknownImage = CoreImages.getSharedImage(CoreImages.PROPERTIES_BOOLEAN_UNKNOWN);
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new BooleanPropertyEditor();

	private BooleanPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void paint(Property property, Graphics graphics, int x, int y, int width, int height) throws Exception {
		Object value = property.getValue();
		if (value instanceof Boolean) {
			boolean booleanValue = ((Boolean) value).booleanValue();
			Image image = booleanValue ? m_trueImage : m_falseImage;
			String text = Boolean.toString(booleanValue);
			paint(graphics, x, y, width, height, image, text);
		} else {
			paint(graphics, x, y, width, height, m_unknownImage, "unknown");
		}
	}

	/**
	 * Paints {@link Image} and text.
	 */
	private void paint(Graphics graphics, int x, int y, int width, int height, Image image, String text) {
		// draw image
		{
			DrawUtils.drawImageCV(graphics, image, x, y, height);
			// prepare new position/width
			int imageWidth = image.getBounds().width + 2;
			x += imageWidth;
			width -= imageWidth;
		}
		// draw text
		DrawUtils.drawStringCV(graphics, text, x, y, width, height);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean activate(PropertyTable propertyTable, Property property, Point location)
			throws Exception {
		// check that user clicked on image
		if (location == null || location.x < m_trueImage.getBounds().width + 2) {
			invertValue(property);
		}
		// don't activate
		return false;
	}

	@Override
	public void doubleClick(Property property, Point location) throws Exception {
		invertValue(property);
	}

	/**
	 * Inverts the value of given boolean {@link Property}.
	 */
	private void invertValue(Property property) throws Exception {
		// prepare current boolean value
		boolean booleanValue = false;
		{
			Object value = property.getValue();
			if (value instanceof Boolean) {
				booleanValue = ((Boolean) value).booleanValue();
			}
		}
		// set inverted value
		property.setValue(!booleanValue);
	}
}