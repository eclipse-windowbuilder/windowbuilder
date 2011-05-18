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
package org.eclipse.wb.internal.rcp.databinding.xwt.model.beans;

import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;

/**
 * 
 * @author lobas_av
 * 
 */
public class XmlElementBeanBindableInfo extends BeanBindableInfo {
  private final boolean m_dataContext;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlElementBeanBindableInfo(BeanSupport beanSupport,
      Class<?> objectType,
      IReferenceProvider referenceProvider,
      XmlObjectInfo objectInfo,
      boolean dataContext) throws Exception {
    super(beanSupport, null, objectType, referenceProvider, objectInfo);
    m_dataContext = dataContext;
    setBindingDecoration(SwtResourceManager.TOP_RIGHT);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isDataContext() {
    return m_dataContext;
  }
}