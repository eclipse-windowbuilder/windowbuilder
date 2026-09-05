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
package org.eclipse.wb.gef.core.tools;

import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.DesignCreationFactory;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreationFactory;

/**
 * The {@link CreationTool} creates new {@link EditPart EditParts} via a {@link CreationFactory}.
 * If the user simply clicks on the viewer, the default sized {@link EditPart} will be created at
 * that point. If the user clicks and drags, the created {@link EditPart} will be sized based on
 * where the user clicked and dragged.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class CreationTool extends AbstractCreationTool {
	private final CreationFactory m_factory;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CreationTool(CreationFactory factory) {
		m_factory = factory;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void activate() {
		super.activate();
		try {
			if (m_factory instanceof DesignCreationFactory factory) {
				factory.activate();
			}
		} catch (Throwable e) {
			getDomain().loadDefaultTool();
		}
	}

	/**
	 * @return the {@link CreationFactory} used to create the new {@link EditPart}'s.
	 */
	public final CreationFactory getFactory() {
		return m_factory;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a {@link CreateRequest} and sets this tool's factory on the request.
	 */
	@Override
	protected Request createTargetRequest() {
		return new CreateRequest(m_factory);
	}

	@Override
	protected String getCommandName() {
		return REQ_CREATE;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void selectAddedObjects() {
		CreateRequest request = (CreateRequest) getTargetRequest();
		Object model = request.getSelectObject();
		if (model != null) {
			EditPartViewer viewer = getCurrentViewer();
			if (viewer != null) {
				EditPart editPart = viewer.getEditPartRegistry().get(model);
				if (editPart != null) {
					viewer.select(editPart);
				}
			}
		}
	}
}