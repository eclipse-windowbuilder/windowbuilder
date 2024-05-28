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
package org.eclipse.wb.tests.designer.tests.common;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Implementation of {@link GenericProperty} that is not modified and has no value.
 *
 * @author scheglov_ke
 */
public class GenericPropertyNoValue extends GenericProperty {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GenericPropertyNoValue(JavaInfo javaInfo, String title, PropertyEditor propertyEditor) {
		super(javaInfo, title, propertyEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Property
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isModified() throws Exception {
		return false;
	}

	@Override
	public Object getValue() throws Exception {
		return UNKNOWN_VALUE;
	}

	@Override
	public Object getDefaultValue() {
		return UNKNOWN_VALUE;
	}

	@Override
	public void setValue(Object value) throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GenericProperty
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Class<?> getType() {
		return null;
	}

	@Override
	public Expression getExpression() {
		return null;
	}

	@Override
	public void setExpression(String source, Object value) throws Exception {
	}
}
