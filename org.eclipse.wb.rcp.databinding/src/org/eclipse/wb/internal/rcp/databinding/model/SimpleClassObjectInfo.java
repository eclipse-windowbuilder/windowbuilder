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
package org.eclipse.wb.internal.rcp.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;

import org.apache.commons.lang.ClassUtils;

/**
 * Model for abstract objects or interfaces and objects that supported extends.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public abstract class SimpleClassObjectInfo extends AstObjectInfo {
	protected String m_className;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public SimpleClassObjectInfo() {
	}

	public SimpleClassObjectInfo(String className) {
		m_className = className;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public String getClassName() {
		return m_className;
	}

	public void setClassName(String className) {
		m_className = className;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationText() throws Exception {
		return ClassUtils.getShortClassName(m_className);
	}
}