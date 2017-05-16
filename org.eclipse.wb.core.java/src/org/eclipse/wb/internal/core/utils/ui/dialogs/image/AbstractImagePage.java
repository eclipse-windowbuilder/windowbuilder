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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract {@link Composite} for {@link ImageInfo} selection.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public abstract class AbstractImagePage extends Composite {
  protected final AbstractImageDialog m_imageDialog;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractImagePage(Composite parent, int style, AbstractImageDialog imageDialog) {
    super(parent, style);
    m_imageDialog = imageDialog;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked when user activates this {@link AbstractImagePage}.
   */
  public abstract void activate();

  /**
   * Sets the initial data for page. It is expected that page will use method
   * {@link AbstractImageDialog#setResultImageInfo(ImageInfo)} to display image corresponding to
   * given data.
   */
  public abstract void setInput(Object data);

  /**
   * XXX
   */
  public void init(Object data) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the id of this page.
   */
  public abstract String getId();

  /**
   * @return the title of this page.
   */
  public abstract String getTitle();

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Control} represented this page.
   */
  protected Control getPageControl() {
    return this;
  }
}
