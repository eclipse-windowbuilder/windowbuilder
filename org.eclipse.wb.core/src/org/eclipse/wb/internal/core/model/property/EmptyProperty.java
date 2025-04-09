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
package org.eclipse.wb.internal.core.model.property;

import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * Empty {@link Property}, that has no title or value.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public class EmptyProperty extends Property {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EmptyProperty() {
		super(BooleanPropertyEditor.INSTANCE);
	}
	public EmptyProperty(PropertyEditor editor) {
		super(editor);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Property
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		return null;
	}
	@Override
	public boolean isModified() throws Exception {
		return false;
	}
	@Override
	public Object getValue() throws Exception {
		return UNKNOWN_VALUE;
	}
	@Override
	public void setValue(Object value) throws Exception {
	}
}
