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
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

import org.eclipse.jdt.core.dom.Expression;

/**
 * {@link IModelSupport} for direct observable objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class DirectFieldModelSupport implements IModelSupport {
  private final ObservableInfo m_observable;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DirectFieldModelSupport(ObservableInfo observable) {
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
    if (AstNodeUtils.isVariable(expression)) {
      return m_observable.getVariableIdentifier().equals(CoreUtils.getNodeReference(expression));
    }
    return false;
  }
}