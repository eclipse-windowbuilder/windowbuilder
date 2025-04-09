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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementsSupport;

import org.eclipse.draw2d.PositionConstants;

/**
 * Alignment actions which uses {@link PlacementsSupport}.
 *
 * @author mitin_aa
 * @coverage swt.gef.policy
 */
public abstract class ComplexAlignmentActionsSupport<C extends IAbstractComponentInfo>
extends
AbstractAlignmentActionsSupport<C> {
	private final PlacementsSupport m_placementsSupport;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComplexAlignmentActionsSupport(PlacementsSupport placementsSupport) {
		m_placementsSupport = placementsSupport;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignments
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void commandAlignLeft() throws Exception {
		m_placementsSupport.align(m_components, true, PositionConstants.LEFT);
	}

	@Override
	protected void commandAlignRight() throws Exception {
		m_placementsSupport.align(m_components, true, PositionConstants.RIGHT);
	}

	@Override
	protected void commandAlignCenterHorizontally() throws Exception {
		m_placementsSupport.align(m_components, true, PositionConstants.CENTER);
	}

	@Override
	protected void commandAlignTop() throws Exception {
		m_placementsSupport.align(m_components, false, PositionConstants.TOP);
	}

	@Override
	protected void commandAlignBottom() throws Exception {
		m_placementsSupport.align(m_components, false, PositionConstants.BOTTOM);
	}

	@Override
	protected void commandAlignCenterVertically() throws Exception {
		m_placementsSupport.align(m_components, false, PositionConstants.CENTER);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Center in container
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void commandCenterHorizontally() throws Exception {
		m_placementsSupport.center(m_components, true);
	}

	@Override
	protected void commandCenterVertically() throws Exception {
		m_placementsSupport.center(m_components, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Replicate size
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void commandReplicateWidth() throws Exception {
		m_placementsSupport.replicateSize(m_components, true);
	}

	@Override
	protected void commandReplicateHeight() throws Exception {
		m_placementsSupport.replicateSize(m_components, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Distribute space
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void commandDistributeSpaceHorizontally() throws Exception {
		m_placementsSupport.distributeSpace(m_components, true);
	}

	@Override
	protected void commandDistributeSpaceVertically() throws Exception {
		m_placementsSupport.distributeSpace(m_components, false);
	}
}