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
package org.eclipse.wb.internal.rcp.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.ObservePresentation;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import org.eclipse.swt.graphics.Image;

/**
 * Simple implementation {@link IObservePresentation} that work on static text and image.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public class SimpleObservePresentation extends ObservePresentation {
  private final String m_text;
  private final Image m_image;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleObservePresentation(String text, Class<?> type) {
    m_text = text;
    m_image = TypeImageProvider.getImage(type);
  }

  public SimpleObservePresentation(String text, Image image) {
    m_text = text;
    m_image = image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Image getInternalImage() throws Exception {
    return m_image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObservePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getText() throws Exception {
    return m_text;
  }

  public String getTextForBinding() throws Exception {
    return getText();
  }
}