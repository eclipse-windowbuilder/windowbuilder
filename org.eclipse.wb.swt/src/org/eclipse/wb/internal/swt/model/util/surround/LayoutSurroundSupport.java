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
package org.eclipse.wb.internal.swt.model.util.surround;

import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.util.surround.SurroundSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import java.util.List;

/**
 * Abstract {@link SurroundSupport} for SWT {@link LayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.util
 */
public abstract class LayoutSurroundSupport extends SwtSurroundSupport {
	private final LayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutSurroundSupport(LayoutInfo layout) {
		super(layout.getComposite());
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isActive() {
		return m_layout.isActive();
	}

	@Override
	protected boolean validateComponents(List<ControlInfo> components) throws Exception {
		if (!super.validateComponents(components)) {
			return false;
		}
		// perform "surround" only for "active" layout
		if (!m_layout.isActive()) {
			return false;
		}
		// don't handle implicit
		for (ControlInfo component : components) {
			if (component.getCreationSupport() instanceof IImplicitCreationSupport) {
				return false;
			}
		}
		// OK
		return true;
	}
}
