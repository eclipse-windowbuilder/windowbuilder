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

import org.eclipse.wb.core.model.IWrapper;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;

import org.eclipse.swt.widgets.Control;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract utility for any Java-based wrapper object.
 */
public abstract class AbstractWrapper implements IWrapper {
  protected final JavaInfo m_wrapperInfo;
  protected JavaInfo m_wrappedInfo = null;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractWrapper(JavaInfo host) {
    m_wrapperInfo = host;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return wrapper {@link JavaInfo}.
   */
  public JavaInfo getWrapperInfo() {
    return m_wrapperInfo;
  }

  /**
   * @return wrapped {@link JavaInfo}.
   */
  public JavaInfo getWrappedInfo() throws Exception {
    if (m_wrappedInfo == null) {
      m_wrappedInfo =
          JavaInfoUtils.createJavaInfo(
              m_wrapperInfo.getEditor(),
              getWrappedType(),
              newWrappedCreationSupport());
      inheritParameters(m_wrappedInfo);
    }
    return m_wrappedInfo;
  }

  /**
   * @return the exact type of {@link Control}.
   */
  public abstract Class<?> getWrappedType();

  /**
   * @return {@link CreationSupport} for new instance of wrapped model.
   */
  protected abstract CreationSupport newWrappedCreationSupport() throws Exception;

  /**
   * Inherit parameters from viewer to wrapped {@link JavaInfo}
   */
  protected void inheritParameters(JavaInfo javaInfo) {
    Map<String, String> parameters = JavaInfoUtils.getParameters(m_wrapperInfo);
    for (Entry<String, String> entry : parameters.entrySet()) {
      String keyName = entry.getKey();
      if (keyName.startsWith("wrapped.")) {
        JavaInfoUtils.setParameter(javaInfo, keyName.substring(8), entry.getValue());
      }
    }
  }
}
