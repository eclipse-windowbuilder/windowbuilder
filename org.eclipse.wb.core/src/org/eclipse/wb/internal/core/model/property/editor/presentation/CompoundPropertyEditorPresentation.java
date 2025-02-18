/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.eclipse.draw2d.Container;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;

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
	public IFigure getFigure(PropertyTable propertyTable, Property property) {
		FlowLayout layout = new FlowLayout();
		layout.setMinorSpacing(0);

		IFigure container = new Container(layout);
		for (PropertyEditorPresentation presentation : m_presentations) {
			container.add(presentation.getFigure(propertyTable, property));
		}
		return container;
	}
}
