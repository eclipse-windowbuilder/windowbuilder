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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.swt.model.util.surround.LayoutSurroundSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import java.util.List;

/**
 * Helper for surrounding {@link ControlInfo}'s on {@link GenericFlowLayoutInfo} with some
 * {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class GenericFlowLayoutSurroundSupport extends LayoutSurroundSupport {
	private final GenericFlowLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GenericFlowLayoutSurroundSupport(GenericFlowLayoutInfo layout) {
		super(layout);
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean validateComponents(List<ControlInfo> components) throws Exception {
		// check that components are adjacent
		{
			List<ControlInfo> allComponents = m_layout.getComposite().getChildrenControls();
			if (!GenericsUtils.areAdjacent(allComponents, components)) {
				return false;
			}
		}
		// continue
		return super.validateComponents(components);
	}

	@Override
	protected void addContainer(CompositeInfo container, List<ControlInfo> components)
			throws Exception {
		m_layout.command_CREATE(container, components.get(0));
	}
}
