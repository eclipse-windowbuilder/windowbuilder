/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * The {@link ExpressionConverter} for {@link org.eclipse.swt.graphics.Rectangle}.
 *
 * @author lobas_av
 * @coverage swt.property.converter
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
		if (value instanceof Rectangle rectangle) {
			return "new org.eclipse.swt.graphics.Rectangle("
			+ rectangle.x
			+ ", "
			+ rectangle.y
			+ ", "
			+ rectangle.width
			+ ", "
			+ rectangle.height
			+ ")";
		}
		return "(org.eclipse.swt.graphics.Rectangle) null";
	}
}