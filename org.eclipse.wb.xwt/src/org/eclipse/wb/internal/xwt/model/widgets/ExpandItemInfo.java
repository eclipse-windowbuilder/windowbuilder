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

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

import java.util.List;

/**
 * Model for {@link ExpandItem}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class ExpandItemInfo extends ItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExpandItemInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ControlInfo} or <code>null</code>.
   */
  public ControlInfo getControl() {
    return GenericsUtils.getFirstOrNull(getChildren(ControlInfo.class));
  }

  private boolean isExpanded() throws Exception {
    return (Boolean) ReflectionUtils.invokeMethod(getObject(), "getExpanded()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new XmlObjectPresentation(this) {
    @Override
    public List<ObjectInfo> getChildrenGraphical() throws Exception {
      if (!isExpanded()) {
        return ImmutableList.of();
      }
      return getChildrenTree();
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    {
      Object object = getObject();
      int headerHeight = (Integer) ReflectionUtils.invokeMethod2(object, "getHeaderHeight");
      int x = ReflectionUtils.getFieldInt(object, "x");
      int y = ReflectionUtils.getFieldInt(object, "y");
      int width = ReflectionUtils.getFieldInt(object, "width");
      int height = ReflectionUtils.getFieldInt(object, "height");
      if (isExpanded()) {
        height += headerHeight;
      } else {
        height = headerHeight;
      }
      setModelBounds(new Rectangle(x, y, width, height));
    }
    super.refresh_fetch();
  }

  /**
   * {@link Control} returns bounds on {@link ExpandBar}, but we show it as child of
   * {@link ExpandItem}, so we should tweak {@link Control} bounds.
   */
  void fixControlBounds() {
    for (ControlInfo control : getChildren(ControlInfo.class)) {
      {
        Point offset = getModelBounds().getLocation().getNegated();
        control.getModelBounds().translate(offset);
      }
      {
        Point offset = getBounds().getLocation().getNegated();
        control.getBounds().translate(offset);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * After CREATE or ADD operation.
   */
  public void command_TARGET_after(ControlInfo control) throws Exception {
    getPropertyByTitle("expanded").setValue(true);
  }
}
