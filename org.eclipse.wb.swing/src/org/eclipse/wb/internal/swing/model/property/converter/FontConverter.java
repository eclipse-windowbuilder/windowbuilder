/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.swing.model.property.editor.font.ExplicitFontInfo;

import java.awt.Font;

public class FontConverter extends ExpressionConverter {
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
		if (value == null) {
			return "(java.awt.Font) null";
		}

		Font font = (Font) value;
		return new ExplicitFontInfo(font).getSource();
	}
}
