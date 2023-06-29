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
package org.eclipse.wb.internal.rcp.gef.policy.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.rcp.model.layout.IStackLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Rectangle;

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
		List<Handle> handles = Lists.newArrayList();
		handles.add(new MoveHandle(getHost()));
		handles.add(createHandle(IPositionConstants.SOUTH_EAST));
		handles.add(createHandle(IPositionConstants.SOUTH_WEST));
		handles.add(createHandle(IPositionConstants.NORTH_WEST));
		handles.add(createHandle(IPositionConstants.NORTH_EAST));
		return handles;
	}

	/**
	 * @return the {@link ResizeHandle} for given direction.
	 */
	private Handle createHandle(int direction) {
		ResizeHandle handle = new ResizeHandle(getHost(), direction);
		ResizeTracker tracker = new ResizeTracker(direction, null);
		tracker.setDefaultCursor(ICursorConstants.SIZEALL);
		handle.setDragTrackerTool(tracker);
		handle.setCursor(ICursorConstants.SIZEALL);
		return handle;
	}

	@Override
	protected void showSelection() {
		super.showSelection();
		// add navigate feedback
		if (m_navigationFigure == null) {
			m_navigationFigure = new StackLayoutNavigationFigure(this);
			Figure hostFigure = getHostFigure();
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
		EditPart editPart = viewer.getEditPartByModel(component);
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
		EditPart editPart = viewer.getEditPartByModel(component);
		viewer.select(editPart);
	}
}