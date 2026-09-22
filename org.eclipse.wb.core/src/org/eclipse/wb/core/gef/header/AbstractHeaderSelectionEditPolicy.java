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
package org.eclipse.wb.core.gef.header;

import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Handle;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.handles.MoveHandle;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of {@link SelectionEditPolicy} for headers. It provides additional
 * utilities for interacting with main {@link LayoutEditPolicy} and main {@link EditPartViewer}.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public abstract class AbstractHeaderSelectionEditPolicy extends SelectionEditPolicy {
	private final LayoutEditPolicy m_mainPolicy;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractHeaderSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
		m_mainPolicy = mainPolicy;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedback utilities
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Layer} from main {@link EditPartViewer} with given id.
	 */
	protected final Layer getMainLayer(String layerId) {
		return (Layer) LayerManager.Helper.find(getMainViewer()).getLayer(layerId);
	}

	/**
	 * @return the main {@link EditPartViewer}.
	 */
	private EditPartViewer getMainViewer() {
		return m_mainPolicy.getHost().getViewer();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @since 1.26
	 */
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		// move handle
		{
			MoveHandle moveHandle = new org.eclipse.wb.gef.graphical.handles.MoveHandle(getHost(), new HeaderMoveHandleLocator());
			configureMoveHandle(moveHandle);
			handles.add(moveHandle);
		}
		//
		return handles;
	}

	/**
	 * Configures the {@code Move} selection handle. May be sub-classed.
	 *
	 * @since 1.26
	 */
	protected void configureMoveHandle(MoveHandle moveHandle) {
		moveHandle.setForegroundColor(ColorConstants.red);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move location
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link Locator} to place handle directly on header.
	 */
	private class HeaderMoveHandleLocator implements Locator {
		@Override
		public void relocate(IFigure target) {
			IFigure reference = getHostFigure();
			Rectangle bounds = reference.getBounds().getCopy();
			FigureUtils.translateFigureToFigure(reference, target, bounds);
			target.setBounds(bounds);
		}
	}
}
