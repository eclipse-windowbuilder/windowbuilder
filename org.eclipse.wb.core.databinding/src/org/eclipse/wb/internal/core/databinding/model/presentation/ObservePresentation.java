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

import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.swt.graphics.Image;

/**
 * Base class for all observable presentations.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public abstract class ObservePresentation
    implements
      IObservePresentation,
      IObservePresentationDecorator {
  private Image m_decorateImage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObservePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public final Image getImage() throws Exception {
    return m_decorateImage == null ? getInternalImage() : m_decorateImage;
  }

  /**
   * @return {@link Image} for displaying and decorate.
   */
  protected abstract Image getInternalImage() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObservePresentationDecorator
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void setBindingDecorator(int corner) throws Exception {
    if (corner != 0) {
      Image image = getInternalImage();
      if (image != null) {
        m_decorateImage = SwtResourceManager.decorateImage(image, JavaInfoDecorator.IMAGE, corner);
      }
    } else {
      m_decorateImage = null;
    }
  }
}