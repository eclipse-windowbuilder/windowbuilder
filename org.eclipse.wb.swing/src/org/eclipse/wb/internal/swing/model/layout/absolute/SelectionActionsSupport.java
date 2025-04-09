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
package org.eclipse.wb.internal.swing.model.layout.absolute;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * Helper for adding selection actions for {@link AbsoluteLayoutInfo}.
 *
 * @author lobas_av
 * @coverage swing.model.layout
 */
public final class SelectionActionsSupport
extends
org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.SimpleAlignmentActionsSupport<ComponentInfo> {
	private final AbstractAbsoluteLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionActionsSupport(AbstractAbsoluteLayoutInfo layout) {
		super();
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isComponentInfo(ObjectInfo object) {
		return object instanceof ComponentInfo;
	}

	@Override
	protected boolean isValidObjectOnRootPath(IAbstractComponentInfo parent) {
		ContainerInfo container = (ContainerInfo) parent;
		return container.hasLayout() && container.getLayout() instanceof AbstractAbsoluteLayoutInfo;
	}

	@Override
	protected IAbstractComponentInfo getLayoutContainer() {
		return m_layout.getContainer();
	}

	@Override
	protected void commandChangeBounds(ComponentInfo component, Point location, Dimension size)
			throws Exception {
		m_layout.command_BOUNDS(component, location, size);
	}
}