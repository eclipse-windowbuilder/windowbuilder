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
package org.eclipse.wb.internal.swt.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;

import org.eclipse.swt.graphics.Point;

/**
 * The {@link ExpressionConverter} for {@link Point}.
 *
 * @author lobas_av
 * @coverage swt.property.converter
 */
public final class PointConverter extends ExpressionConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final ExpressionConverter INSTANCE = new PointConverter();

	private PointConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionConverter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
		if (value instanceof Point point) {
			return "new org.eclipse.swt.graphics.Point(" + point.x + ", " + point.y + ")";
		}
		return "(org.eclipse.swt.graphics.Point) null";
	}
}