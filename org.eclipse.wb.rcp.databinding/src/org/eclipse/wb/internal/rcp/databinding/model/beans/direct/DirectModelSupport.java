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
package org.eclipse.wb.internal.rcp.databinding.model.beans.direct;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.parser.IModelSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * {@link IModelSupport} for direct observable objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class DirectModelSupport implements IModelSupport {
	private final ObservableInfo m_observable;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DirectModelSupport(ObservableInfo observable) {
		m_observable = observable;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IModelSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public AstObjectInfo getModel() {
		return m_observable;
	}

	@Override
	public boolean isRepresentedBy(Expression expression) throws Exception {
		if (expression instanceof MethodInvocation) {
			return m_observable.getVariableIdentifier().equals(CoreUtils.getNodeReference(expression));
		}
		return false;
	}
}