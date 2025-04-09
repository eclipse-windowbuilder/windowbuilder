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
package org.eclipse.wb.internal.core.gef.policy.snapping;

import org.eclipse.wb.core.model.IAbstractComponentInfo;

/**
 * Abstract attachment information while component attached to component.
 *
 * @deprecated
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
@Deprecated
public final class ComponentAttachmentInfo {
	private final IAbstractComponentInfo m_source;
	private final IAbstractComponentInfo m_target;
	private final int m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentAttachmentInfo(IAbstractComponentInfo source,
			IAbstractComponentInfo target,
			int alignment) {
		m_source = source;
		m_target = target;
		m_alignment = alignment;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public int getAlignment() {
		return m_alignment;
	}

	public IAbstractComponentInfo getSource() {
		return m_source;
	}

	public IAbstractComponentInfo getTarget() {
		return m_target;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// toString
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "" + m_source + " -> " + m_target + " by " + m_alignment;
	}
}
