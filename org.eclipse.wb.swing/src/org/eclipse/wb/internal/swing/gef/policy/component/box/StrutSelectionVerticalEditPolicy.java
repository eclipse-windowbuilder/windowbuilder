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
package org.eclipse.wb.internal.swing.gef.policy.component.box;

import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.swing.gef.part.box.BoxStrutVerticalEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.PositionConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link SelectionEditPolicy} for resizing {@link BoxStrutVerticalEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class StrutSelectionVerticalEditPolicy extends StrutSelectionEditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StrutSelectionVerticalEditPolicy(ComponentInfo strut) {
		super(strut);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createStaticHandles() {
		List<Handle> handles = new ArrayList<>();
		handles.add(createResizeHandle(PositionConstants.TOP, PositionConstants.NORTH));
		handles.add(createResizeHandle(PositionConstants.BOTTOM, PositionConstants.SOUTH));
		return handles;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getTooltip(int width, int height) {
		return Integer.toString(height);
	}

	@Override
	protected String getSource(ComponentInfo strut, int width, int height) throws Exception {
		return IntegerConverter.INSTANCE.toJavaSource(strut, height);
	}
}