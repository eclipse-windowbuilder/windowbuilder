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
package org.eclipse.wb.internal.rcp.databinding.xwt.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.ConverterInfo;

import org.eclipse.jdt.core.IJavaProject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author lobas_av
 *
 */
public class ConverterUiContentProvider extends ChooseClassUiContentProvider {
  private final DatabindingsProvider m_provider;
  private final ConverterInfo m_converter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ConverterUiContentProvider(DatabindingsProvider provider,
      ChooseClassConfiguration configuration,
      ConverterInfo converter) {
    super(configuration);
    m_provider = provider;
    m_converter = converter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Class<?> loadClass(String className) throws ClassNotFoundException {
    return CoreUtils.load(m_provider.getXmlObjectRoot().getContext().getClassLoader(), className);
  }

  @Override
  protected IJavaProject getJavaProject() {
    return m_provider.getXmlObjectRoot().getContext().getJavaProject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    setClassName(StringUtils.defaultString(m_converter.getValue(), "N/S"));
  }

  public void saveToObject() throws Exception {
    String className = getClassName();
    // check set or clear value
    if ("N/S".equals(className)) {
      m_converter.setValue(null, false);
    } else {
      String[] defaultValues = getConfiguration().getDefaultValues();
      boolean staticResurce =
          defaultValues == null ? false : ArrayUtils.contains(defaultValues, className);
      m_converter.setValue(className, staticResurce);
    }
  }
}