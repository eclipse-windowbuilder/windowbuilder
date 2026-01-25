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
package org.eclipse.wb.gef.graphical;

import org.eclipse.wb.gef.core.RequestProcessor;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * An edit part capable of pre-processing requests.
 *
 * @see {@link RequestProcessor}
 */
public abstract class DesignEditPart extends GraphicalEditPart {

	////////////////////////////////////////////////////////////////////////////
	//
	// Request processors
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<RequestProcessor> m_requestProcessors = new ArrayList<>();

	/**
	 * Adds the {@link RequestProcessor}, if not added yet.
	 */
	public final void addRequestProcessor(RequestProcessor processor) {
		if (!m_requestProcessors.contains(processor)) {
			m_requestProcessors.add(processor);
		}
	}

	/**
	 * Removes the {@link RequestProcessor}.
	 */
	public final void removeRequestProcessor(RequestProcessor processor) {
		m_requestProcessors.remove(processor);
	}

	/**
	 * @return the {@link Request} processed with registered
	 *         {@link RequestProcessor}'s.
	 */
	protected final Request processRequestProcessors(Request request) {
		try {
			for (RequestProcessor processor : m_requestProcessors) {
				request = processor.process(this, request);
			}
		} catch (Throwable e) {
		}
		return request;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request/Command
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public Command getCommand(Request request) {
		return super.getCommand(processRequestProcessors(request));
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		return super.getTargetEditPart(processRequestProcessors(request));
	}

	@Override
	public void performRequest(Request request) {
		super.performRequest(processRequestProcessors(request));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source Feedback
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void showSourceFeedback(Request request) {
		super.showSourceFeedback(processRequestProcessors(request));
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		super.eraseSourceFeedback(processRequestProcessors(request));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Target Feedback
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void showTargetFeedback(Request request) {
		super.showTargetFeedback(processRequestProcessors(request));
	}

	@Override
	public void eraseTargetFeedback(Request request) {
		super.eraseTargetFeedback(processRequestProcessors(request));
	}
}