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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.swt.model.widgets.IScrollableInfo;
import org.eclipse.wb.internal.xwt.support.CoordinateUtils;

import org.eclipse.swt.widgets.Scrollable;

/**
 * Model for {@link Scrollable}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage XWT.model.widgets
 */
public class ScrollableInfo extends ControlInfo implements IScrollableInfo {
  private Rectangle m_clientArea;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ScrollableInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    m_clientArea = CoordinateUtils.getClientArea((Scrollable) getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Rectangle}, same as {@link Scrollable#getClientArea()}.
   */
  public final Rectangle getClientArea() {
    return m_clientArea;
  }
}
