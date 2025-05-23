/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

/**
 * {@link EditPolicy} that flips some boolean property between <code>true/false</code> states, for
 * example <code>expanded</code> property in <code>TreeItem</code>, <code>ExpandableComposite</code>
 * , etc.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class FlipBooleanPropertyEditPolicy extends EditPolicy {
	private final ObjectInfo m_component;
	private final String m_propertyPath;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FlipBooleanPropertyEditPolicy(ObjectInfo component, String propertyPath) {
		m_component = component;
		m_propertyPath = propertyPath;
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
			ExecutionUtils.run(m_component, () -> {
				Property property = PropertyUtils.getByPath(m_component, m_propertyPath);
				if (property != null) {
					Object value = property.getValue();
					if (value instanceof Boolean) {
						boolean booleanValue = (Boolean) value;
						property.setValue(!booleanValue);
					}
				}
			});
		}
	}
}
