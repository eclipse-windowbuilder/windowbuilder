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
package org.eclipse.wb.internal.core.gefTree.policy;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.gef.EditPart;

/**
 * Implementation of {@link ILayoutRequestValidator} for validate items for <i>array object</i>.
 *
 * @author sablin_aa
 * @coverage core.gefTree.policy
 */
public final class ArrayObjectRequestValidator implements ILayoutRequestValidator {
	private final AbstractArrayObjectInfo m_arrayInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ArrayObjectRequestValidator(AbstractArrayObjectInfo arrayInfo) {
		m_arrayInfo = arrayInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutRequestValidator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean validateCreateRequest(EditPart host, CreateRequest request) {
		return isValidModel(request.getNewObject());
	}

	@Override
	public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
		for (EditPart editPart : request.getEditParts()) {
			// check model
			if (!isValidModel(editPart.getModel())) {
				return false;
			}
			// allow move inside array or empty variable otherwise
			JavaInfo javaInfo = (JavaInfo) editPart.getModel();
			if (!m_arrayInfo.equals(AbstractArrayObjectInfo.getArrayObjectInfo(javaInfo))
					&& !(javaInfo.getVariableSupport() instanceof EmptyVariableSupport)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
		return validateMoveRequest(host, request);
	}

	@Override
	public boolean validatePasteRequest(EditPart host, PasteRequest request) {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean isValidModel(final Object objectModel) {
		if (objectModel instanceof JavaInfo) {
			return ExecutionUtils.runObjectLog(() -> {
				JavaInfo info = (JavaInfo) objectModel;
				return ReflectionUtils.isSuccessorOf(
						info.getDescription().getComponentClass(),
						m_arrayInfo.getItemClass().getCanonicalName());
			}, false);
		}
		return false;
	}
}
