/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.ILayoutRequestValidatorHelper;

import org.eclipse.gef.EditPart;

import java.util.List;

/**
 * Typical implementation of {@link ILayoutRequestValidator} for performing check for separate
 * objects.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public abstract class AbstractLayoutRequestValidator implements ILayoutRequestValidator {
	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutRequestValidator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean validateCreateRequest(EditPart host, CreateRequest request) {
		return validate(host, request.getNewObject());
	}

	@Override
	public boolean validatePasteRequest(final EditPart host, final PasteRequest request) {
		return ExecutionUtils.runObjectLog(() -> {
			ILayoutRequestValidatorHelper validatorHelper = GlobalState.getValidatorHelper();
			List<?> mementos = (List<?>) request.getMemento();
			for (Object memento : mementos) {
				IComponentDescription description = validatorHelper.getPasteComponentDescription(memento);
				if (!validateDescription(host, description)) {
					return false;
				}
			}
			// OK, all pasted components are valid
			return true;
		}, false);
	}

	@Override
	public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
		for (EditPart editPart : request.getEditParts()) {
			if (!validate(host, editPart.getModel())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
		return validateMoveRequest(host, request);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given "parent" and "child" are valid.
	 */
	protected abstract boolean validate(EditPart host, Object child);

	/**
	 * @return <code>true</code> if given "parent" and "child" are valid.
	 */
	protected abstract boolean validateDescription(EditPart host,
			IComponentDescription childDescription);
}
