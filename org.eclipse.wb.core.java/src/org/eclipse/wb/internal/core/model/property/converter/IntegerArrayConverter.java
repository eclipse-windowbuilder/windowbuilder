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
 * The {@link ExpressionConverter} for array of {@link int}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class IntegerArrayConverter extends ExpressionConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IntegerArrayConverter INSTANCE = new IntegerArrayConverter();

	private IntegerArrayConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionConverter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) {
		if (value == null) {
			return "(int[]) null";
		} else {
			StringBuilder buffer = new StringBuilder();
			buffer.append("new int[] {");
			// add items
			int[] items = (int[]) value;
			for (int i = 0; i < items.length; i++) {
				int item = items[i];
				if (i != 0) {
					buffer.append(", ");
				}
				buffer.append(IntegerConverter.INSTANCE.toJavaSource(javaInfo, item));
			}
			//
			buffer.append("}");
			return buffer.toString();
		}
	}
}
