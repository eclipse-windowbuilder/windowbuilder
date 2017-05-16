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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages;

import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;

import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link AbstractImagePage} that sets "null" as image.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class NullImagePage extends AbstractImagePage {
  public static final String ID = "NULL";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NullImagePage(Composite parent, int style, AbstractImageDialog imageDialog) {
    super(parent, style, imageDialog);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getTitle() {
    return Messages.NullImagePage_title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractImagePage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    m_imageDialog.setResultImageInfo(new ImageInfo(ID, null, null, -1));
  }

  @Override
  public void setInput(Object data) {
  }
}
