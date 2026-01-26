/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.gef.policy.layout;

import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.rcp.model.layout.IStackLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SelectionLayoutEditPolicy} for {@link IStackLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class StackLayoutSelectionEditPolicy<C extends IControlInfo>
extends
SelectionEditPolicy {
	private final IStackLayoutInfo<C> m_layout;
	private StackLayoutNavigationFigure m_navigationFigure;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StackLayoutSelectionEditPolicy(IStackLayoutInfo<C> layout) {
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		handles.add(new MoveHandle(getHost()));
		handles.add(createHandle(PositionConstants.SOUTH_EAST));
		handles.add(createHandle(PositionConstants.SOUTH_WEST));
		handles.add(createHandle(PositionConstants.NORTH_WEST));
		handles.add(createHandle(PositionConstants.NORTH_EAST));
		return handles;
	}

	/**
	 * @return the {@link ResizeHandle} for given direction.
	 */
	private Handle createHandle(int direction) {
		ResizeHandle handle = new ResizeHandle(getHost(), direction);
		ResizeTracker tracker = new ResizeTracker(direction, null);
		tracker.setDefaultCursor(Cursors.SIZEALL);
		handle.setDragTracker(tracker);
		handle.setCursor(Cursors.SIZEALL);
		return handle;
	}

	@Override
	protected void showSelection() {
		super.showSelection();
		// add navigate feedback
		if (m_navigationFigure == null) {
			m_navigationFigure = new StackLayoutNavigationFigure(this);
			IFigure hostFigure = getHostFigure();
			Rectangle bounds = hostFigure.getBounds().getCopy();
			FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
			{
				int x = bounds.right() - StackLayoutNavigationFigure.WIDTH * 2 - 3;
				int y = bounds.y - StackLayoutNavigationFigure.HEIGHT / 2;
				m_navigationFigure.setBounds(new Rectangle(x,
						y,
						StackLayoutNavigationFigure.WIDTH * 2,
						StackLayoutNavigationFigure.HEIGHT));
			}
			addFeedback(m_navigationFigure);
		}
	}

	@Override
	protected void hideSelection() {
		super.hideSelection();
		// remove navigate feedback
		if (m_navigationFigure != null) {
			removeFeedback(m_navigationFigure);
			m_navigationFigure = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets show previous component relative of current.
	 */
	void showPrevComponent() {
		IEditPartViewer viewer = getHost().getViewer();
		// show previous component
		C component = m_layout.getPrevControl();
		m_layout.show(component);
		// select EditPart
		EditPart editPart = viewer.getEditPartRegistry().get(component);
		viewer.select(editPart);
	}

	/**
	 * Sets show next component relative of current.
	 */
	void showNextComponent() {
		IEditPartViewer viewer = getHost().getViewer();
		// show next component
		C component = m_layout.getNextControl();
		m_layout.show(component);
		// select EditPart
		EditPart editPart = viewer.getEditPartRegistry().get(component);
		viewer.select(editPart);
	}
}