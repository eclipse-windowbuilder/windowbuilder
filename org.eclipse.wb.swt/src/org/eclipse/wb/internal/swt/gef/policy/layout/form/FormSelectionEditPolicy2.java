/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteBasedSelectionEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Selection policy for edit containers with {@link FormLayout}.
 *
 * @author mitin_aa
 * @coverage swt.gef.policy.form
 */
public final class FormSelectionEditPolicy2 extends SelectionEditPolicy {
	protected final FormLayoutInfoImplAutomatic<?> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormSelectionEditPolicy2(FormLayoutInfo layout) {
		super();
		m_layout = (FormLayoutInfoImplAutomatic<?>) layout.getImpl();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		MoveHandle moveHandle = new MoveHandle(getHost());
		moveHandle.setBorder(new LineBorder(ColorConstants.lightBlue));
		handles.add(moveHandle);
		handles.add(createResizeHandle(PositionConstants.NORTH));
		handles.add(createResizeHandle(PositionConstants.SOUTH));
		handles.add(createResizeHandle(PositionConstants.WEST));
		handles.add(createResizeHandle(PositionConstants.EAST));
		handles.add(createResizeHandle(PositionConstants.SOUTH_EAST));
		handles.add(createResizeHandle(PositionConstants.SOUTH_WEST));
		handles.add(createResizeHandle(PositionConstants.NORTH_WEST));
		handles.add(createResizeHandle(PositionConstants.NORTH_EAST));
		return handles;
	}

	private Handle createResizeHandle(int direction) {
		GraphicalEditPart owner = getHost();
		ResizeHandle handle = new ResizeHandle(owner, direction) {
			@Override
			protected Color getBorderColor() {
				return isPrimary() ? ColorConstants.white : ColorConstants.lightBlue;
			}

			@Override
			protected Color getFillColor() {
				return isPrimary() ? ColorConstants.lightBlue : ColorConstants.white;
			}
		};
		handle.setDragTracker(new ResizeTracker(direction,
				AbsoluteBasedSelectionEditPolicy.REQ_RESIZE));
		return handle;
	}
}
