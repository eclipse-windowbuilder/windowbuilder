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
package org.eclipse.wb.internal.swing.model.util.surround;

import org.eclipse.wb.internal.core.model.util.surround.SurroundSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import java.util.List;

/**
 * Abstract {@link SurroundSupport} for Swing {@link LayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.model.util
 */
public abstract class LayoutSurroundSupport extends SwingSurroundSupport {
	private final LayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutSurroundSupport(LayoutInfo layout) {
		super(layout.getContainer());
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean validateComponents(List<ComponentInfo> components) throws Exception {
		// perform "surround" only for "active" layout
		if (!m_layout.isActive()) {
			return false;
		}
		// continue
		return super.validateComponents(components);
	}
}
