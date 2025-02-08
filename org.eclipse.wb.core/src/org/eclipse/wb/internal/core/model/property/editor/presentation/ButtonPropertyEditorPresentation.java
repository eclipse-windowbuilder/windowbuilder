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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.graphics.Image;

import java.util.Optional;

/**
 * Implementation of {@link PropertyEditorPresentation} for displaying {@link Button}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.model.property.editor
 */
public abstract class ButtonPropertyEditorPresentation extends PropertyEditorPresentation {

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public IFigure getFigure(PropertyTable propertyTable, Property property) {
		Clickable button = new PresentationButton(this, getImage());
		// Button should be a perfect square
		button.setPreferredSize(propertyTable.getRowHeight(), propertyTable.getRowHeight());
		// Only create tooltip when necessary
		getTooltip().ifPresent(tooltip -> button.setToolTip(new Label(tooltip)));
		button.getModel().addChangeListener(event -> {
			if (ButtonModel.PRESSED_PROPERTY.equals(event.getPropertyName())) {
				propertyTable.deactivateEditor(true);
				propertyTable.setActiveProperty(property);
			}
		});
		button.getModel().addActionListener(event -> {
			try {
				onClick(propertyTable, property);
			} catch (Throwable e) {
				propertyTable.deactivateEditor(false);
				propertyTable.handleException(e);
			}
		});
		return button;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Image} to display on {@link Button}.
	 */
	protected Image getImage() {
		return DesignerPlugin.getImage("properties/dots.gif");
	}

	/**
	 * @return the tooltip text to display for {@link Button}.
	 */
	protected Optional<String> getTooltip() {
		return Optional.empty();
	}

	/**
	 * Handles click on {@link Button}.
	 */
	protected abstract void onClick(PropertyTable propertyTable, Property property) throws Exception;
}
