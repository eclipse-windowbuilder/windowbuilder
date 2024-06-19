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
package org.eclipse.wb.internal.core.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * The {@link ExpressionConverter} for <code>byte</code>.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class ByteConverter extends ExpressionConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final ByteConverter INSTANCE = new ByteConverter();

	private ByteConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionConverter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toJavaSource(JavaInfo javaInfo, Object value) {
		return "(byte) " + ((Byte) value).toString();
	}
}
