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
package org.eclipse.wb.internal.core.gefTree.policy.generic;

import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.gefTree.policy.SingleObjectLayoutEditPolicy;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.generic.AbstractContainerRequestValidator;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;

/**
 * {@link LayoutEditPolicy} for {@link SimpleContainer}.
 *
 * @author scheglov_ke
 * @coverage core.gefTree.policy
 */
public final class SimpleContainerLayoutEditPolicy extends SingleObjectLayoutEditPolicy<Object> {
	private final SimpleContainer m_container;
	private final ILayoutRequestValidator m_requestValidator;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SimpleContainerLayoutEditPolicy(ObjectInfo model, SimpleContainer container) {
		super(model);
		m_container = container;
		{
			ILayoutRequestValidator validator = new AbstractContainerRequestValidator(container);
			validator = LayoutRequestValidators.cache(validator);
			m_requestValidator = validator;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return m_requestValidator;
	}

	@Override
	protected boolean isEmpty() {
		return m_container.isEmpty();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void command_CREATE(Object component) throws Exception {
		m_container.command_CREATE(component);
	}

	@Override
	protected void command_ADD(Object component) throws Exception {
		m_container.command_ADD(component);
	}
}