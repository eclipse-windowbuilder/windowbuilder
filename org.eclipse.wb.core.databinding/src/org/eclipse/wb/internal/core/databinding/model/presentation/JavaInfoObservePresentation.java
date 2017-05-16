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
package org.eclipse.wb.internal.core.databinding.model.presentation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;

import org.eclipse.swt.graphics.Image;

/**
 * {@link IObservePresentation} for presentation {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public class JavaInfoObservePresentation implements IObservePresentation {
  protected ObjectInfo m_javaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoObservePresentation(ObjectInfo javaInfo) {
    m_javaInfo = javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setJavaInfo(ObjectInfo javaInfo) {
    m_javaInfo = javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObservePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getText() throws Exception {
    return ObjectsLabelProvider.INSTANCE.getText(m_javaInfo);
  }

  public String getTextForBinding() throws Exception {
    return m_javaInfo.getPresentation().getText();
  }

  public Image getImage() throws Exception {
    return ObjectsLabelProvider.INSTANCE.getImage(m_javaInfo);
  }
}