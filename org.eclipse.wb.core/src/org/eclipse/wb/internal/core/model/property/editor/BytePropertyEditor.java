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

import java.text.MessageFormat;

/**
 * The {@link PropertyEditor} for <code>byte</code>.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public class BytePropertyEditor extends AbstractTextPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new BytePropertyEditor();

	private BytePropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Property property) throws Exception {
		Object value = property.getValue();
		if (value instanceof Byte) {
			return value.toString();
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
		}
		// prepare value
		Byte value;
		try {
			value = Byte.valueOf(text);
		} catch (Throwable e) {
			UiUtils.openWarning(
					DesignerPlugin.getShell(),
					property.getTitle(),
					MessageFormat.format(ModelMessages.BytePropertyEditor_notValidByte, text));
			return false;
		}
		// modify property
		property.setValue(value);
		return true;
	}
}