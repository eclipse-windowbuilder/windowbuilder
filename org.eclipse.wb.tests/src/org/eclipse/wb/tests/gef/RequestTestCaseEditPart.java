/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

/**
 * Helper {@link EditPart} used to check invoke different method and logging it use format:
 * EditPart.name = method [ Request ].
 *
 * @author lobas_av
 */
public class RequestTestCaseEditPart extends GraphicalEditPart {
	private final String m_name;
	private final RequestsLogger m_logger;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create helper {@link EditPart}, <code>name</code> used to identify {@link EditPart}.
	 */
	public RequestTestCaseEditPart(String name, RequestsLogger logger) {
		m_name = name;
		m_logger = logger;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EditPart
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Figure createFigure() {
		return new Figure();
	}

	@Override
	public void performRequest(Request request) {
		m_logger.log(this, "performRequest", request);
		super.performRequest(request);
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		m_logger.log(this, "getTargetEditPart", request);
		return this;
	}

	@Override
	public void showSourceFeedback(Request request) {
		m_logger.log(this, "showSourceFeedback", request);
		super.showSourceFeedback(request);
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		m_logger.log(this, "eraseSourceFeedback", request);
		super.eraseSourceFeedback(request);
	}

	@Override
	public void showTargetFeedback(Request request) {
		m_logger.log(this, "showTargetFeedback", request);
		super.showTargetFeedback(request);
	}

	@Override
	public void eraseTargetFeedback(Request request) {
		m_logger.log(this, "eraseTargetFeedback", request);
		super.eraseTargetFeedback(request);
	}

	@Override
	public Command getCommand(Request request) {
		m_logger.log(this, "getCommand", request);
		return super.getCommand(request);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return m_name;
	}
}