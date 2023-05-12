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

import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.xwt.support.CoordinateUtils;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;

/**
 * Model for {@link CoolItem}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class CoolItemInfo extends ItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CoolItemInfo(EditorContext context,
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
    {
      Rectangle bounds = CoordinateUtils.getRectangle(((CoolItem) getObject()).getBounds());
      setModelBounds(bounds);
    }
    super.refresh_fetch();
    fixControlBounds();
  }

  /**
   * {@link Control} returns bounds on {@link CoolBar}, but we show it as child of {@link CoolItem},
   * so we should tweak {@link Control} bounds.
   */
  private void fixControlBounds() {
    for (ControlInfo control : getChildren(ControlInfo.class)) {
      {
        Point offset = getModelBounds().getLocation().getNegated();
        control.getModelBounds().performTranslate(offset);
      }
      {
        Point offset = getBounds().getLocation().getNegated();
        control.getBounds().performTranslate(offset);
      }
    }
  }
}
