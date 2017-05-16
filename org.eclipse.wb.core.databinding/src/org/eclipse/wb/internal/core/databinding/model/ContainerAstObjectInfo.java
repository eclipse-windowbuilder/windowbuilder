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
package org.eclipse.wb.internal.core.databinding.model;

import java.util.List;

/**
 * {@link AstObjectInfo} that contains other {@link AstObjectInfo}.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public final class ContainerAstObjectInfo extends AstObjectInfo {
  private final List<? extends AstObjectInfo> m_objects;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContainerAstObjectInfo(List<? extends AstObjectInfo> objects) {
    m_objects = objects;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(AstObjectInfoVisitor visitor) throws Exception {
    for (AstObjectInfo object : m_objects) {
      object.accept(visitor);
    }
  }
}