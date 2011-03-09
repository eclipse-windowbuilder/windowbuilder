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
package org.eclipse.wb.internal.swing.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.presentation.ObservePresentation;
import org.eclipse.wb.internal.swing.databinding.ui.providers.TypeImageProvider;

import org.eclipse.swt.graphics.Image;

/**
 * Simple implementation {@link ObservePresentation} that work on static text and image.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public class SimpleObservePresentation extends ObservePresentation {
  private final String m_text;
  private final String m_textForBinding;
  private final Image m_image;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleObservePresentation(String text, String textForBinding, Image image) {
    m_text = text;
    m_textForBinding = textForBinding;
    m_image = image;
  }

  public SimpleObservePresentation(String text, String textForBinding, Class<?> type, Image image) {
    m_text = text;
    m_textForBinding = textForBinding;
    m_image = image == null ? TypeImageProvider.getImage(type) : image;
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