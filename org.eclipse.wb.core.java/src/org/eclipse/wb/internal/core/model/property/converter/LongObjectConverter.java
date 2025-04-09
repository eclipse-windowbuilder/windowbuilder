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
 * The {@link ExpressionConverter} for {@link Long}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class LongObjectConverter extends AbstractNumberConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final LongObjectConverter INSTANCE = new LongObjectConverter();

	private LongObjectConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionConverter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) {
		if (value == null) {
			return "(Long) null";
		}
		// has value
		String text = ((Long) value).toString() + "L";
		// may be use auto-boxing
		if (isBoxingEnabled(javaInfo)) {
			return text;
		}
		// use explicit boxing
		return "Long.valueOf(" + text + ")";
	}
}
