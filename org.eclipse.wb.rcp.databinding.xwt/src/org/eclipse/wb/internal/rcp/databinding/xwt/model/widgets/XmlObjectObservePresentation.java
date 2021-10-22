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
package org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.presentation.JavaInfoObservePresentation;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author lobas_av
 *
 */
public class XmlObjectObservePresentation extends JavaInfoObservePresentation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlObjectObservePresentation(ObjectInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getText() throws Exception {
    String text = super.getText();
    String reference = XmlObjectReferenceProvider.getName(m_javaInfo);
    if (!StringUtils.isEmpty(reference)) {
      text += " - " + reference;
    }
    return text;
  }

  @Override
  public String getTextForBinding() throws Exception {
    String reference = XmlObjectReferenceProvider.getName(m_javaInfo);
    return StringUtils.isEmpty(reference) ? super.getText() : reference;
  }
}