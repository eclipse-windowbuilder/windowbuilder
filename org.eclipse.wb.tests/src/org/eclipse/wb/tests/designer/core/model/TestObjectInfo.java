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
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;

/**
 * Implementation of {@link ObjectInfo} used during tests.
 *
 * @author scheglov_ke
 */
public class TestObjectInfo extends ObjectInfo {
	private final String m_name;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TestObjectInfo() {
		this("none");
	}

	public TestObjectInfo(String name) {
		m_name = name;
		setBroadcastSupport(new BroadcastSupport());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return m_name;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Required methods
	//
	////////////////////////////////////////////////////////////////////////////
	public void move(ObjectInfo newParent, ObjectInfo nextChild) throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final DefaultObjectPresentation m_presentation = new DefaultObjectPresentation(this) {
		@Override
		public String getText() throws Exception {
			return TestObjectInfo.this.toString();
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}
}