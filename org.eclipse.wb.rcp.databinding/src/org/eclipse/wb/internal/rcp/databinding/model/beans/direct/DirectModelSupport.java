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
  public AstObjectInfo getModel() {
    return m_observable;
  }

  public boolean isRepresentedBy(Expression expression) throws Exception {
    if (expression instanceof MethodInvocation) {
      return m_observable.getVariableIdentifier().equals(CoreUtils.getNodeReference(expression));
    }
    return false;
  }
}