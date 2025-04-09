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

import java.awt.Insets;

/**
 * The {@link ExpressionConverter} for {@link Insets}.
 *
 * @author scheglov_ke
 * @coverage swing.property.converter
 */
public final class InsetsConverter extends ExpressionConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final ExpressionConverter INSTANCE = new InsetsConverter();

	private InsetsConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionConverter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
		if (value == null) {
			return "(java.awt.Insets) null";
		} else {
			Insets insets = (Insets) value;
			return "new java.awt.Insets("
			+ insets.top
			+ ", "
			+ insets.left
			+ ", "
			+ insets.bottom
			+ ", "
			+ insets.right
			+ ")";
		}
	}
}
