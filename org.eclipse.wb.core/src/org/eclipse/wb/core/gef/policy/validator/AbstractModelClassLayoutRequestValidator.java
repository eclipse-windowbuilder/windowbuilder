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
package org.eclipse.wb.core.gef.policy.validator;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;

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