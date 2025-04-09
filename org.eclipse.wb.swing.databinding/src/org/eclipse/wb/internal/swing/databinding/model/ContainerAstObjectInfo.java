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
package org.eclipse.wb.internal.swing.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;

import java.util.List;

/**
 * {@link AstObjectInfo} that contains other {@link AstObjectInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class ContainerAstObjectInfo extends AstObjectInfo {
	private final List<? extends AstObjectInfo> m_objects;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ContainerAstObjectInfo(List<? extends AstObjectInfo> objects) {
		m_objects = objects;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(AstObjectInfoVisitor visitor) throws Exception {
		for (AstObjectInfo object : m_objects) {
			object.accept(visitor);
		}
	}
}