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
package org.eclipse.wb.internal.core.preferences;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.preference.PreferencePage;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;

/**
 * Factory for creating {@link PreferencePage}'s that accept {@link ToolkitDescription} as single
 * parameter in constructor.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public final class PreferencePageFactory
    implements
      IExecutableExtension,
      IExecutableExtensionFactory {
  private String m_pageClassName;
  private String m_toolkitId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExecutableExtension
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
      throws CoreException {
    try {
      String[] parameters = StringUtils.split((String) data);
      m_pageClassName = parameters[0];
      m_toolkitId = parameters[1];
    } catch (Throwable e) {
      throw new CoreException(StatusUtils.createError(e.getMessage()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExecutableExtensionFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object create() throws CoreException {
    try {
      Class<?> pageClass = Class.forName(m_pageClassName);
      Constructor<?> constructor = pageClass.getConstructor(ToolkitDescription.class);
      //
      ToolkitDescription toolkit = DescriptionHelper.getToolkit(m_toolkitId);
      return constructor.newInstance(toolkit);
    } catch (Throwable e) {
      throw new CoreException(StatusUtils.createError(e.getMessage()));
    }
  }
}
