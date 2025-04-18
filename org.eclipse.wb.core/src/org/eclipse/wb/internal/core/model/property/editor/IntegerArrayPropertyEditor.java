/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * The {@link PropertyEditor} for <code>int[]</code>.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class IntegerArrayPropertyEditor extends AbstractTextPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IntegerArrayPropertyEditor INSTANCE = new IntegerArrayPropertyEditor();

	private IntegerArrayPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Property property) throws Exception {
		Object value = property.getValue();
		if (value instanceof int[] array) {
			return StringUtils.join(ArrayUtils.toObject(array), ' ');
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getEditorText(Property property) throws Exception {
		return getText(property);
	}

	@Override
	protected boolean setEditorText(Property property, String text) throws Exception {
		text = text.trim();
		// check for delete
		if (text.length() == 0) {
			property.setValue(Property.UNKNOWN_VALUE);
			return true;
		}
		// prepare value
		int[] array;
		try {
			String[] parts = StringUtils.split(text);
			array = new int[parts.length];
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];
				array[i] = Integer.valueOf(part);
			}
		} catch (Throwable e) {
			UiUtils.openWarning(
					DesignerPlugin.getShell(),
					property.getTitle(),
					MessageFormat.format(ModelMessages.IntegerPropertyEditor_notValidInt, text));
			return false;
		}
		// modify property
		property.setValue(array);
		return true;
	}
}
