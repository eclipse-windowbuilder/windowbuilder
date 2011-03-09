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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;

/**
 * Implementation of {@link IObjectPresentation} for {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class LayoutPresentation extends DefaultJavaInfoPresentation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutPresentation(LayoutInfo layoutInfo) {
    super(layoutInfo);
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
