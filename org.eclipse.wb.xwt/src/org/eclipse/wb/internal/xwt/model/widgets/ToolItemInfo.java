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
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.xwt.support.ControlSupport;
import org.eclipse.wb.internal.xwt.support.CoordinateUtils;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Model for {@link ToolItem}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class ToolItemInfo extends ItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolItemInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new StylePresentation(this) {
    @Override
    protected void initImages() throws Exception {
      addImage(SWT.CHECK, "wbp-meta/org/eclipse/swt/widgets/ToolItem_check.gif");
      addImage(SWT.RADIO, "wbp-meta/org/eclipse/swt/widgets/ToolItem_radio.gif");
      addImage(SWT.DROP_DOWN, "wbp-meta/org/eclipse/swt/widgets/ToolItem_dropDown.gif");
      addImage(SWT.SEPARATOR, "wbp-meta/org/eclipse/swt/widgets/ToolItem_separator.gif");
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link ToolItemInfo} is separator.
   */
  public boolean isSeparator() {
    return ControlSupport.hasStyle(getWidget(), SWT.SEPARATOR);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    {
      Rectangle bounds = CoordinateUtils.getRectangle(((ToolItem) getObject()).getBounds());
      setModelBounds(bounds);
    }
    super.refresh_fetch();
    fixControlBounds();
  }

  /**
   * {@link Control} returns bounds on {@link ToolBar}, but we show it as child of {@link ToolItem},
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
