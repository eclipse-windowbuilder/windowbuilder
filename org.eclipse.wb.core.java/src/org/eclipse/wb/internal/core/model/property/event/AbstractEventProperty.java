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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * Abstract super class for any event property.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
abstract class AbstractEventProperty extends JavaProperty {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractEventProperty(JavaInfo javaInfo, String title, PropertyEditor propertyEditor) {
		super(javaInfo, title, propertyEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Property
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Object getValue() throws Exception {
		return UNKNOWN_VALUE;
	}

	@Override
	public void setValue(Object value) throws Exception {
	}
}
