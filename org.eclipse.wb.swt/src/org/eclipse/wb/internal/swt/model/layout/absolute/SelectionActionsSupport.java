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
package org.eclipse.wb.internal.swt.model.layout.absolute;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * Helper for adding selection actions for {@link IAbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class SelectionActionsSupport<C extends IControlInfo>
extends
org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.SimpleAlignmentActionsSupport<C> {
	private final IAbsoluteLayoutInfo<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionActionsSupport(IAbsoluteLayoutInfo<C> layout) {
		super();
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SelectionActionsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isComponentInfo(ObjectInfo object) {
		return object instanceof IControlInfo;
	}

	@Override
	protected boolean isValidObjectOnRootPath(IAbstractComponentInfo parent) {
		CompositeInfo composite = (CompositeInfo) parent;
		return composite.hasLayout() && composite.getLayout() instanceof IAbsoluteLayoutInfo<?>;
	}

	@Override
	protected IAbstractComponentInfo getLayoutContainer() {
		return m_layout.getComposite();
	}

	@Override
	protected void commandChangeBounds(C control, Point location, Dimension size) throws Exception {
		m_layout.commandChangeBounds(control, location, size);
	}
}