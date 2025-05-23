/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.core.dom.Expression;

/**
 * {@link PropertyEditor} that displays source of {@link Expression} from {@link GenericProperty}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class DisplayExpressionPropertyEditor extends TextDisplayPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new DisplayExpressionPropertyEditor();

	private DisplayExpressionPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		GenericProperty genericProperty = (GenericProperty) property;
		Expression expression = genericProperty.getExpression();
		if (expression != null) {
			AstEditor editor = genericProperty.getJavaInfo().getEditor();
			return editor.getSource(expression);
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean activate(PropertyTable propertyTable, Property property, Point location)
			throws Exception {
		return false;
	}
}