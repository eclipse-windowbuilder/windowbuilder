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
package org.eclipse.wb.internal.rcp.gef.policy.jface.action;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.RequestProcessor;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;

/**
 * Implementation of {@link RequestProcessor} for dropping {@link ActionInfo} on
 * {@link MenuManagerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ActionDropRequestProcessor extends RequestProcessor {
	public static final RequestProcessor INSTANCE = new ActionDropRequestProcessor();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private ActionDropRequestProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// RequestProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String KEY_SELECTION_RUNNABLE = "delayed selection runnable";

	@Override
	public Request process(final EditPart editPart, Request request) throws Exception {
		if (request instanceof final ActionDropRequest actionDropRequest) {
			final ActionInfo action = actionDropRequest.getAction();
			scheduleActionItemSelection(actionDropRequest);
			// prepare CreateRequest, that creates our ActionInfo
			CreateRequest createRequest = new CreateRequest(new ICreationFactory() {
				@Override
				public void activate() throws Exception {
				}

				@Override
				public Object getNewObject() {
					return action;
				}
			});
			// OK, we have CreateRequest
			createRequest.copyStateFrom(actionDropRequest);
			return createRequest;
		}
		// no, we don't know this request
		return request;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Schedules selection for {@link ContributionItemInfo}, corresponding to the added
	 * {@link ActionInfo}.
	 */
	private static void scheduleActionItemSelection(final ActionDropRequest actionDropRequest) {
		final ActionInfo action = actionDropRequest.getAction();
		if (action.getArbitraryValue(KEY_SELECTION_RUNNABLE) == null) {
			action.putArbitraryValue(KEY_SELECTION_RUNNABLE, Boolean.TRUE);
			action.addBroadcastListener(new JavaEventListener() {
				@Override
				public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
					action.removeArbitraryValue(KEY_SELECTION_RUNNABLE);
					action.removeBroadcastListener(this);
					if (child instanceof ActionContributionItemInfo) {
						actionDropRequest.setItem((ActionContributionItemInfo) child);
					}
				}
			});
		}
	}
}
