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
package org.eclipse.wb.internal.core.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;

import java.util.List;

/**
 * Designer root object for GEF. It contains root {@link JavaInfo} and more <i>non visual beans</i>
 * for this root.
 *
 * @author lobas_av
 * @coverage core.model
 */
public final class DesignRootObject {
  private final JavaInfo m_rootObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignRootObject(JavaInfo rootObject) {
    m_rootObject = rootObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfo getRootObject() {
    return m_rootObject;
  }

  public List<?> getChildren() {
    List<Object> children = Lists.newArrayList();
    // add "info" root
    children.add(m_rootObject);
    // add exist non visual beans
    NonVisualBeanContainerInfo container = NonVisualBeanContainerInfo.find(m_rootObject);
    if (container != null) {
      children.addAll(container.getChildren());
    }
    return children;
  }
}