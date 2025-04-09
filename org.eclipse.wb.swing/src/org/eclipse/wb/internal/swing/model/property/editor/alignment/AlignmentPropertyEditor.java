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
package org.eclipse.wb.internal.swing.model.property.editor.alignment;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.FloatPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.CompoundPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PresentationButton;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.draw2d.ButtonGroup;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Image;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JComponent;

/**
 * The {@link PropertyEditor} for {@link JComponent#setAlignmentX(float)} or
 * {@link JComponent#setAlignmentY(float)}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
abstract class AlignmentPropertyEditor extends FloatPropertyEditor {
	private final Map<ButtonPropertyEditorPresentation, Float> m_valueToPresentation =
			new HashMap<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AlignmentPropertyEditor(String[] fields, String[] images) {
		Assert.equals(fields.length, images.length);
		for (int i = 0; i < fields.length; i++) {
			final String field = fields[i];
			final float value = ReflectionUtils.getFieldFloat(Component.class, field);
			final Image image = Activator.getImage("info/alignment/" + images[i]);
			ButtonPropertyEditorPresentation presentation = new ButtonPropertyEditorPresentation() {
				@Override
				protected Image getImage() {
					return image;
				}

				@Override
				protected Optional<String> getTooltip() {
					return Optional.of(field);
				}

				@Override
				protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
					GenericProperty genericProperty = (GenericProperty) property;
					genericProperty.setExpression("java.awt.Component." + field, value);
				}
			};
			m_presentation.add(presentation);
			// remember presentation for value
			m_valueToPresentation.put(presentation, value);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final CompoundPropertyEditorPresentation m_presentation = new CompoundPropertyEditorPresentation() {
		@Override
		public IFigure getFigure(PropertyTable propertyTable, Property property) {
			IFigure container = super.getFigure(propertyTable, property);

			// Only one toggle can be selected at a time
			ButtonGroup buttonGroup = new ButtonGroup();
			for (IFigure figure : container.getChildren()) {
				Clickable button = (Clickable) figure;
				buttonGroup.add(button.getModel());
			}

			// Initialize selection
			ExecutionUtils.runLog(() -> {
				Object value = property.getValue();
				for (IFigure figure : container.getChildren()) {
					PresentationButton button = (PresentationButton) figure;
					boolean selected = Objects.equals(m_valueToPresentation.get(button.getPresentation()), value);
					button.setSelected(selected);
				}
			});

			return container;
		}
	};

	@Override
	public PropertyEditorPresentation getPresentation() {
		return m_presentation;
	}
}
