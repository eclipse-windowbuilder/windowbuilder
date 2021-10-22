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
package org.eclipse.wb.internal.xwt.model.layout;

import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.XmlObjectPresentation;

/**
 * Implementation of {@link IObjectPresentation} for {@link LayoutDataInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public final class LayoutDataPresentation extends XmlObjectPresentation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutDataPresentation(LayoutDataInfo layoutData) {
    super(layoutData);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObjectPresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isVisible() throws Exception {
    return false;
  }
}