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
