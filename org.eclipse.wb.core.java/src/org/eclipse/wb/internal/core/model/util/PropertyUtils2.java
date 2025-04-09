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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Utils for {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class PropertyUtils2 {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private PropertyUtils2() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	////////////////////////////////////////////////////////////////////////////
	public static GenericPropertyImpl createGenericPropertyImpl(JavaInfo javaInfo,
			GenericPropertyDescription description) {
		GenericPropertyImpl property =
				new GenericPropertyImpl(javaInfo,
						description.getTitle(),
						description.getAccessorsArray(),
						description.getDefaultValue(),
						description.getConverter(),
						description.getEditor());
		property.setDescription(description);
		property.setCategory(description.getCategory());
		return property;
	}
}
