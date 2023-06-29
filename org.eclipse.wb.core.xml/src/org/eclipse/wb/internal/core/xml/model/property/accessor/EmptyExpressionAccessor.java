/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.xml.model.property.accessor;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * {@link ExpressionAccessor} without value.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public class EmptyExpressionAccessor extends ExpressionAccessor {
	public static final ExpressionAccessor INSTANCE = new EmptyExpressionAccessor();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EmptyExpressionAccessor() {
		super("no-attribute");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Value
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue(XmlObjectInfo object) throws Exception {
		return Property.UNKNOWN_VALUE;
	}

	@Override
	public Object getDefaultValue(XmlObjectInfo object) throws Exception {
		return Property.UNKNOWN_VALUE;
	}
}
