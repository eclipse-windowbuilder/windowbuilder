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
package org.eclipse.wb.internal.core.databinding.ui.editor;

import org.eclipse.swt.graphics.Image;

/**
 * {@link IPageListener} wrapper.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class PageListenerWrapper implements IPageListener {
  private final IPageListener m_pageListener;
  private final ICompleteListener m_completeListener;
  private String m_message;
  private String m_errorMessage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PageListenerWrapper(IPageListener pageListener, ICompleteListener completeListener) {
    m_pageListener = pageListener;
    m_completeListener = completeListener;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPageListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setTitle(String title) {
    m_pageListener.setTitle(title);
  }

  public void setTitleImage(Image image) {
    m_pageListener.setTitleImage(image);
  }

  public void setMessage(String newMessage) {
    m_message = newMessage;
  }

  public void setErrorMessage(String newMessage) {
    m_errorMessage = newMessage;
  }

  public void setPageComplete(boolean complete) {
    m_completeListener.calculateFinish();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return current state message or <code>null</code>.
   */
  public String getMessage() {
    return m_message;
  }

  /**
   * @return current error state message or <code>null</code>.
   */
  public String getErrorMessage() {
    return m_errorMessage;
  }
}