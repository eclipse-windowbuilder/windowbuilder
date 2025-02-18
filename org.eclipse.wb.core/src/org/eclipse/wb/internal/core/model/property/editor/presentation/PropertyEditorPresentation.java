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
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.draw2d.IFigure;

/**
 * Implementations of {@link PropertyEditorPresentation} are used to show some presentation for
 * visible, but not activated yet {@link PropertyEditor}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public abstract class PropertyEditorPresentation {
	/**
	 * Creates and returns the realization of the property editor presentation as a
	 * GEF {@link IFigure}. This is generally a
	 */
	public abstract IFigure getFigure(PropertyTable propertyTable, Property property);
}
