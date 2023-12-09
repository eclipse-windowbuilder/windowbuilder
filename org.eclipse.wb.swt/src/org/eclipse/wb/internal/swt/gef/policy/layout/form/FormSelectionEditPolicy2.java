/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
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
		moveHandle.setBorder(new LineBorder(IColorConstants.lightBlue));
		handles.add(moveHandle);
		handles.add(createResizeHandle(IPositionConstants.NORTH));
		handles.add(createResizeHandle(IPositionConstants.SOUTH));
		handles.add(createResizeHandle(IPositionConstants.WEST));
		handles.add(createResizeHandle(IPositionConstants.EAST));
		handles.add(createResizeHandle(IPositionConstants.SOUTH_EAST));
		handles.add(createResizeHandle(IPositionConstants.SOUTH_WEST));
		handles.add(createResizeHandle(IPositionConstants.NORTH_WEST));
		handles.add(createResizeHandle(IPositionConstants.NORTH_EAST));
		return handles;
	}

	private Handle createResizeHandle(int direction) {
		GraphicalEditPart owner = getHost();
		ResizeHandle handle = new ResizeHandle(owner, direction) {
			@Override
			protected Color getBorderColor() {
				return isPrimary() ? IColorConstants.white : IColorConstants.lightBlue;
			}

			@Override
			protected Color getFillColor() {
				return isPrimary() ? IColorConstants.lightBlue : IColorConstants.white;
			}
		};
		handle.setDragTrackerTool(new ResizeTracker(direction,
				AbsoluteBasedSelectionEditPolicy.REQ_RESIZE));
		return handle;
	}
}
