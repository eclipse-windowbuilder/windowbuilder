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
package org.eclipse.wb.internal.swing.model.property.editor.beans;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditorProvider;

import org.apache.commons.lang3.ArrayUtils;

import java.beans.PropertyEditorManager;

/**
 * Implementation of {@link PropertyEditorProvider} that creates wrappers for AWT
 * {@link java.beans.PropertyEditor}'s.
 *
 * @author lobas_av
 * @coverage swing.property.beans
 */
public final class JavaBeanEditorProvider extends PropertyEditorProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyEditorProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public PropertyEditor getEditorForEditorType(Class<?> editorType) throws Exception {
		if (java.beans.PropertyEditor.class.isAssignableFrom(editorType)) {
			try {
				return createEditor((java.beans.PropertyEditor) editorType.getDeclaredConstructor().newInstance());
			} catch (Throwable e) {
				// silently ignore any errors, as Introspector does
			}
		}
		return null;
	}

	@Override
	public PropertyEditor getEditorForType(Class<?> propertyType) throws Exception {
		String propertyTypeName = propertyType.getName();
		if (Object.class.isAssignableFrom(propertyType)
				&& propertyType != char[].class
				&& propertyType != byte[].class
				&& propertyType != int[].class
				&& propertyType != int[][].class
				&& propertyTypeName.indexOf("java.lang.") == -1
				&& propertyTypeName.indexOf("java.util.") == -1
				&& propertyTypeName.indexOf("java.awt.") == -1
				&& propertyTypeName.indexOf("javax.swing.") == -1
				&& propertyTypeName.indexOf("org.eclipse.") == -1) {
			String[] standard_editorSearchPath = PropertyEditorManager.getEditorSearchPath();
			try {
				PropertyEditorManager.setEditorSearchPath(ArrayUtils.removeElement(
						standard_editorSearchPath,
						"sun.beans.editors"));
				java.beans.PropertyEditor propertyEditor = PropertyEditorManager.findEditor(propertyType);
				if (propertyEditor != null) {
					return createEditor(propertyEditor);
				}
			} finally {
				PropertyEditorManager.setEditorSearchPath(standard_editorSearchPath);
			}
		}
		return null;
	}

	private static PropertyEditor createEditor(java.beans.PropertyEditor propertyEditor)
			throws Exception {
		PropertyEditorWrapper editorSite = new PropertyEditorWrapper(propertyEditor);
		if (editorSite.getTags(null) == null) {
			return new TextPropertyEditor(editorSite);
		}
		return new ComboPropertyEditor(editorSite);
	}
}