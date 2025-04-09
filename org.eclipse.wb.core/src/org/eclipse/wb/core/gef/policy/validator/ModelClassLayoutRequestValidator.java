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
package org.eclipse.wb.core.gef.policy.validator;

import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;

/**
 * Implementation of {@link ILayoutRequestValidator} for specific type of model objects.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public class ModelClassLayoutRequestValidator extends AbstractModelClassLayoutRequestValidator {
	private final Class<?> m_requiredModelClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ModelClassLayoutRequestValidator(Class<?> requiredModelClass) {
		m_requiredModelClass = requiredModelClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isValidClass(Class<?> clazz) {
		return m_requiredModelClass.isAssignableFrom(clazz);
	}
}