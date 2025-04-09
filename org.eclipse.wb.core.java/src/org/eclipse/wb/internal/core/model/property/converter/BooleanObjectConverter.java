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
 * The {@link ExpressionConverter} for {@link Boolean}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class BooleanObjectConverter extends ExpressionConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final BooleanObjectConverter INSTANCE = new BooleanObjectConverter();

	private BooleanObjectConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionConverter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) {
		if (value == null) {
			return "(Boolean) null";
		}
		// has value
		boolean b = ((Boolean) value).booleanValue();
		// may be use auto-boxing
		if (javaInfo != null) {
			return Boolean.toString(b);
		}
		// use explicit boxing
		if (b) {
			return "Boolean.TRUE";
		} else {
			return "Boolean.FALSE";
		}
	}
}
