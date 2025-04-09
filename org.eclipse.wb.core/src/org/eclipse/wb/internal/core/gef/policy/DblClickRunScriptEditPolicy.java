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
package org.eclipse.wb.internal.core.gef.policy;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

/**
 * {@link EditPolicy} that runs given MVEL script on double click.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class DblClickRunScriptEditPolicy extends EditPolicy {
	private final ObjectInfo m_component;
	private final String m_script;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DblClickRunScriptEditPolicy(ObjectInfo component, String script) {
		m_component = component;
		m_script = script;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void performRequest(Request request) {
		super.performRequest(request);
		if (RequestConstants.REQ_OPEN.equals(request.getType())) {
			ExecutionUtils.run(m_component, new RunnableEx() {
				@Override
				public void run() throws Exception {
					ScriptUtils.evaluate(m_script, m_component);
				}
			});
		}
	}
}
