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
package org.eclipse.wb.internal.core.utils;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import org.osgi.framework.Bundle;

import java.util.Map;

/**
 * Factory for creating {@link Object}'s that does not create new instance each time, but returns
 * always same instance, from <code>public static final INSTANCE</code> field.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class SingletonExtensionFactory
    implements
      IExecutableExtension,
      IExecutableExtensionFactory {
  private Class<?> m_objectClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExecutableExtension
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public void setInitializationData(final IConfigurationElement config,
      final String propertyName,
      final Object data) throws CoreException {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        Bundle extensionBundle = ExternalFactoriesHelper.getExtensionBundle(config);
        String objectClassName = ((Map<String, String>) data).get("class");
        m_objectClass = extensionBundle.loadClass(objectClassName);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExecutableExtensionFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object create() throws CoreException {
    return ExecutionUtils.runObject(new RunnableObjectEx<Object>() {
      public Object runObject() throws Exception {
        return ReflectionUtils.getFieldObject(m_objectClass, "INSTANCE");
      }
    });
  }
}
