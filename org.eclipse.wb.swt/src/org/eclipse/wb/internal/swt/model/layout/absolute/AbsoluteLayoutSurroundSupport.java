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

import org.eclipse.wb.internal.swt.model.util.surround.LayoutSurroundSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * Helper for surrounding {@link ControlInfo}'s with some {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class AbsoluteLayoutSurroundSupport extends LayoutSurroundSupport {
	static final String BOUNDS_KEY = "SurroundSupport_BOUNDS";
	private final AbsoluteLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbsoluteLayoutSurroundSupport(AbsoluteLayoutInfo layout) {
		super(layout);
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addContainer(CompositeInfo container, List<ControlInfo> components)
			throws Exception {
		m_layout.command_CREATE(container, components.get(0));
		// prepare enclosing bounds
		Rectangle enclosingRectangle;
		{
			enclosingRectangle = components.get(0).getModelBounds().getCopy();
			for (ControlInfo component : components) {
				enclosingRectangle.union(component.getModelBounds());
			}
		}
		// set bounds
		m_layout.commandChangeBounds(
				container,
				enclosingRectangle.getLocation(),
				enclosingRectangle.getSize());
		container.putArbitraryValue(BOUNDS_KEY, enclosingRectangle);
	}
}
