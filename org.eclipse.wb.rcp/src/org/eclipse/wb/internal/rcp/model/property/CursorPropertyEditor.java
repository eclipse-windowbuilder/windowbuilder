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
package org.eclipse.wb.internal.rcp.model.property;

import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Cursor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link PropertyEditor} for {@link Cursor}.
 *
 * @author scheglov_ke
 * @coverage rcp.property.editor
 */
public final class CursorPropertyEditor extends AbstractComboPropertyEditor
implements
IClipboardSourceProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new CursorPropertyEditor();

	private CursorPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		if (property instanceof GenericProperty) {
			Expression expression = ((GenericProperty) property).getExpression();
			// new Cursor(device,style)
			if (AstNodeUtils.isCreation(
					expression,
					"org.eclipse.swt.graphics.Cursor",
					"<init>(org.eclipse.swt.graphics.Device,int)")) {
				Expression styleExpression = DomGenerics.arguments(expression).get(1);
				return getTextForStyle(styleExpression);
			}
			// Only here for backwards compatibility
			// SWTResourceManager.getCursor(style)
			if (AstNodeUtils.isMethodInvocation(
					expression,
					"org.eclipse.wb.swt.SWTResourceManager",
					"getCursor(int)")) {
				Expression styleExpression = DomGenerics.arguments(expression).get(0);
				return getTextForStyle(styleExpression);
			}
			// Display.getSystemCursor(style)
			if (AstNodeUtils.isMethodInvocation(
					expression,
					"org.eclipse.swt.widgets.Display",
					"getSystemCursor(int)")) {
				Expression styleExpression = DomGenerics.arguments(expression).get(0);
				return getTextForStyle(styleExpression);
			}

		}
		// unknown value
		return null;
	}

	private String getTextForStyle(Expression styleExpression) {
		if (styleExpression instanceof QualifiedName qualifiedName) {
			if (AstNodeUtils.isSuccessorOf(qualifiedName.getQualifier(), "org.eclipse.swt.SWT")) {
				return qualifiedName.getName().getIdentifier();
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardSourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getClipboardSource(GenericProperty property) throws Exception {
		String text = getText(property);
		if (text != null) {
			return "org.eclipse.swt.widgets.Display.getCurrent().getSystemCursor(org.eclipse.swt.SWT." + text + ")";
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractComboPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addItems(Property property, CCombo combo) throws Exception {
		for (Field cursorField : getCursorFields()) {
			combo.add(cursorField.getName());
		}
	}

	@Override
	protected void selectItem(Property property, CCombo combo) throws Exception {
		combo.setText(getText(property));
	}

	@Override
	protected void toPropertyEx(Property property, CCombo combo, int index) throws Exception {
		if (property instanceof GenericProperty genericProperty) {
			ManagerUtils.ensure_SWTResourceManager(genericProperty.getJavaInfo());
			// prepare source
			String source;
			{
				Field cursorField = getCursorFields().get(index);
				source =
						"org.eclipse.swt.widgets.Display.getCurrent().getSystemCursor(org.eclipse.swt.SWT."
								+ cursorField.getName()
								+ ")";
			}
			// set source
			genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Field}'s with cursor constants from {@link SWT}.
	 */
	private static List<Field> getCursorFields() throws Exception {
		List<Field> cursorFields = new ArrayList<>();
		for (Field field : SWT.class.getFields()) {
			if (field.getName().startsWith("CURSOR_")) {
				cursorFields.add(field);
			}
		}
		return cursorFields;
	}
}
