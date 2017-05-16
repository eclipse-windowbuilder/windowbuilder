/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.gefTree.policy;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

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
  public boolean validateCreateRequest(EditPart host, CreateRequest request) {
    return isValidModel(request.getNewObject());
  }

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

  public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
    return validateMoveRequest(host, request);
  }

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
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
        public Boolean runObject() throws Exception {
          JavaInfo info = (JavaInfo) objectModel;
          return ReflectionUtils.isSuccessorOf(
              info.getDescription().getComponentClass(),
              m_arrayInfo.getItemClass().getCanonicalName());
        }
      }, false);
    }
    return false;
  }
}
