/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.model.property.editor.font;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * {@link PropertyEditor} for {@link Font}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public final class FontPropertyEditor extends TextDialogPropertyEditor
implements
IClipboardSourceProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new FontPropertyEditor();

	private FontPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		Object value = property.getValue();
		if (value instanceof Font) {
			Font font = (Font) value;
			return getText(font);
		}
		return null;
	}

	private static String getText(Font font) throws Exception {
		FontData fontData = font.getFontData()[0];
		//
		StringBuilder buffer = new StringBuilder();
		buffer.append(fontData.getName());
		buffer.append(",");
		buffer.append(fontData.getHeight());
		{
			int style = fontData.getStyle();
			String styleText = FontSupport.getFontStyleSource(style);
			if (styleText.length() != 0) {
				buffer.append(",");
				buffer.append(styleText);
			}
		}
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardSourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	public String getClipboardSource(GenericProperty property) throws Exception {
		return getText(property);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void openDialog(Property property) throws Exception {
		GenericProperty genericProperty = (GenericProperty) property;
		FontDialog fontDialog = new FontDialog(DesignerPlugin.getShell());
		// set initial value
		{
			Object value = property.getValue();
			if (value instanceof Font) {
				Font font = (Font) value;
				FontInfo fontInfo = new FontInfo(font, false);
				fontDialog.setFontInfo(fontInfo);
			}
		}
		// open dialog
		if (fontDialog.open() == Window.OK) {
			FontInfo fontInfo = fontDialog.getFontInfo();
			String source = getText(fontInfo.getFont());
			genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
		}
		// clean up
		fontDialog.disposeFont();
	}
}