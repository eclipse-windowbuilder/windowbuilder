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

import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.editparts.LayerManager;

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
}
