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
package org.eclipse.wb.internal.swing.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;

import java.awt.Rectangle;

/**
 * The {@link ExpressionConverter} for {@link Rectangle}.
 *
 * @author scheglov_ke
 * @coverage swing.property.converter
 */
public final class RectangleConverter extends ExpressionConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final ExpressionConverter INSTANCE = new RectangleConverter();

	private RectangleConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionConverter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
		if (value == null) {
			return "(java.awt.Rectangle) null";
		} else {
			Rectangle rectangle = (Rectangle) value;
			return "new java.awt.Rectangle("
			+ rectangle.x
			+ ", "
			+ rectangle.y
			+ ", "
			+ rectangle.width
			+ ", "
			+ rectangle.height
			+ ")";
		}
	}
}
