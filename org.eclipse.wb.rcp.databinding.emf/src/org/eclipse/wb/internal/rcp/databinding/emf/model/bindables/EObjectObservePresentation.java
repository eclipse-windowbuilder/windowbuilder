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
package org.eclipse.wb.internal.rcp.databinding.emf.model.bindables;

import org.eclipse.wb.internal.core.databinding.model.presentation.ObservePresentation;
import org.eclipse.wb.internal.rcp.databinding.emf.Activator;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ClassUtils;

/**
 * Presentation for {@link EObjectBindableInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class EObjectObservePresentation extends ObservePresentation {
  private static final Image IMAGE = Activator.getImage("EObject.gif");
  private final EObjectBindableInfo m_eObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EObjectObservePresentation(EObjectBindableInfo eObject) {
    m_eObject = eObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Image getInternalImage() throws Exception {
    return IMAGE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObservePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getText() throws Exception {
    return m_eObject.getReference()
        + " - "
        + ClassUtils.getShortClassName(m_eObject.getObjectType());
  }

  public String getTextForBinding() throws Exception {
    return m_eObject.getReference();
  }
}