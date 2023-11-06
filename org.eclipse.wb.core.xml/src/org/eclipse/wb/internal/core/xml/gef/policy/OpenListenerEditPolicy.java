/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.xml.gef.policy;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.event.EventsProperty;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.gef.RequestConstants;

/**
 * {@link EditPolicy} which adds listener code by handling "Open" request (double-clicking on its
 * {@link EditPart}). Component description should have lines like this:
 *
 * <pre><code>
 * &lt;parameters&gt;
 *   &lt;parameter name="x.double-click.listener" value="Selection"/&gt;
 * &lt;/parameters&gt; </code></pre>
 *
 * Where parameter <code>double-click.listener</code> is '/' separated name of listener method to be
 * created.
 *
 * @author scheglov_ke
 * @coverage XML.gef.policy
 */
public final class OpenListenerEditPolicy extends EditPolicy {
	private static final String DOUBLE_CLICK_LISTENER = "x.double-click.listener";
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final String m_listenerName;
	private final XmlObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public OpenListenerEditPolicy(XmlObjectInfo object) {
		m_object = object;
		m_listenerName = XmlObjectUtils.getParameter(object, DOUBLE_CLICK_LISTENER);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void performRequest(Request request) {
		if (m_listenerName != null && RequestConstants.REQ_OPEN.equals(request.getType())) {
			ExecutionUtils.run(m_object, new RunnableEx() {
				@Override
				public void run() throws Exception {
					EventsProperty eventsProperty = (EventsProperty) m_object.getPropertyByTitle("Events");
					eventsProperty.openListener(m_listenerName);
				}
			});
		}
		super.performRequest(request);
	}
}
