/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * The {@link ExpressionConverter} for array of {@link String}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class StringArrayConverter extends ExpressionConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final StringArrayConverter INSTANCE = new StringArrayConverter();

	private StringArrayConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionConverter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) {
		if (value == null) {
			return "(String[]) null";
		} else {
			StringBuilder buffer = new StringBuilder();
			buffer.append("new String[] {");
			// add items
			String[] items = (String[]) value;
			for (int i = 0; i < items.length; i++) {
				String item = items[i];
				if (i != 0) {
					buffer.append(", ");
				}
				buffer.append(StringConverter.INSTANCE.toJavaSource(javaInfo, item));
			}
			//
			buffer.append("}");
			return buffer.toString();
		}
	}
}
