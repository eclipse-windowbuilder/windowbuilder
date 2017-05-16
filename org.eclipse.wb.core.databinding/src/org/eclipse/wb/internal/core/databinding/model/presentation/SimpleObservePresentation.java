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

import org.eclipse.swt.graphics.Image;

/**
 *
 * @author lobas_av
 *
 */
public class SimpleObservePresentation extends ObservePresentation {
  private final String m_text;
  private final String m_textForBinding;
  private final Image m_image;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleObservePresentation(String text, Image image) {
    this(text, text, image);
  }

  public SimpleObservePresentation(String text, String textForBinding, Image image) {
    m_text = text;
    m_textForBinding = textForBinding;
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
    return m_textForBinding;
  }
}