/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.editor.presentation;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.draw2d.geometry.Dimension;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link PropertyEditorPresentation} that contains zero or more other
 * {@link PropertyEditorPresentation}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public class CompoundPropertyEditorPresentation extends PropertyEditorPresentation {
	private final List<PropertyEditorPresentation> m_presentations = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds child {@link PropertyEditorPresentation}.<br>
	 * Child {@link PropertyEditorPresentation}'s are displayed from right to left.
	 */
	public void add(PropertyEditorPresentation presentation) {
		m_presentations.add(presentation);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyEditorPresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void show(PropertyTable propertyTable,
			Property property,
			int x,
			int y,
			int width,
			int height) {
		for (PropertyEditorPresentation presentation : m_presentations) {
			presentation.show(propertyTable, property, x, y, width, height);
			Dimension size = presentation.getSize(width, height);
			width -= size.width;
		}
	}

	@Override
	public void hide(PropertyTable propertyTable, Property property) {
		for (PropertyEditorPresentation presentation : m_presentations) {
			presentation.hide(propertyTable, property);
		}
	}

	@Override
	public Dimension getSize(int wHint, int hHint) {
		Dimension compositeSize = new Dimension(0, 0);
		for (int i = 0; i < m_presentations.size(); ++i) {
			PropertyEditorPresentation presentation = m_presentations.get(i);
			if (i == 0) {
				compositeSize = presentation.getSize(wHint, hHint);
			} else {
				Dimension size = presentation.getSize(wHint, hHint);
				compositeSize.width += size.width;
			}
		}
		return compositeSize;
	}
}
