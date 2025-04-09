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

import org.eclipse.gef.EditPart;

/**
 * Abstract implementation of {@link ILayoutRequestValidator} for specific type of model objects.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public abstract class AbstractModelClassLayoutRequestValidator
extends
AbstractLayoutRequestValidator {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected final boolean validate(EditPart host, Object child) {
		Class<?> clazz = child.getClass();
		return isValidClass(clazz);
	}

	@Override
	protected final boolean validateDescription(EditPart host, IComponentDescription childDescription) {
		Class<?> modelClass = childDescription.getModelClass();
		return isValidClass(modelClass);
	}

	/**
	 * @return <code>true</code> if given type is valid.
	 */
	protected abstract boolean isValidClass(Class<?> clazz);
}