/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;

import java.awt.Cursor;

public class CursorConverter extends ExpressionConverter {
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
		if (value == null) {
			return "(java.awt.Cursor) null";
		}

		Cursor cursor = (Cursor) value;
		if (cursor.getType() == Cursor.CUSTOM_CURSOR) {
			return "java.awt.Cursor.getSystemCustomCursor(%s)"
					.formatted(StringConverter.INSTANCE.toJavaSource(javaInfo, cursor.getName()));
		}

		for (CursorInfo cursorInfo : CursorInfo.getCursors()) {
			if (cursorInfo.type() == cursor.getType()) {
				return "java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.%s)"
						.formatted(cursorInfo.name());
			}
		}
		return "java.awt.Cursor.getPredefinedCursor(%s)"
				.formatted(IntegerConverter.INSTANCE.toJavaSource(javaInfo, cursor.getType()));
	}

}
