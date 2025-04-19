/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.gef.EditPart;

/**
 * {@link ILayoutRequestValidator} that checks that "child" has required component class.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public class ComponentClassLayoutRequestValidator extends AbstractLayoutRequestValidator {
	private final String m_requiredClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentClassLayoutRequestValidator(String requiredClass) {
		m_requiredClass = requiredClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean validate(EditPart host, Object child) {
		IComponentDescription description = GlobalState.getDescriptionHelper().getDescription(child);
		if (description != null) {
			return validateDescription(host, description);
		}
		return false;
	}

	@Override
	protected boolean validateDescription(EditPart host, IComponentDescription childDescription) {
		Class<?> componentClass = childDescription.getComponentClass();
		return isValidClass(componentClass);
	}

	/**
	 * @return <code>true</code> if given type is valid.
	 */
	protected boolean isValidClass(Class<?> componentClass) {
		return ReflectionUtils.isSuccessorOf(componentClass, m_requiredClass);
	}
}
