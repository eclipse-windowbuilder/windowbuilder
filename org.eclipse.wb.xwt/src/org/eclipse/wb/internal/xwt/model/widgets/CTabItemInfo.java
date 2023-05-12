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
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.custom.CTabItem;

import java.util.List;

/**
 * Model for {@link CTabItem}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class CTabItemInfo extends ItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CTabItemInfo(EditorContext context,
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
   * @return the {@link ControlInfo}, may be <code>null</code>.
   */
  public ControlInfo getControl() {
    return GenericsUtils.getFirstOrNull(getChildren(ControlInfo.class));
  }

  /**
   * Makes this item selected.
   */
  public void doSelect() {
    ((CTabFolderInfo) getParent()).setSelectedItem(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new XmlObjectPresentation(this) {
    @Override
    public List<ObjectInfo> getChildrenGraphical() throws Exception {
      return ImmutableList.of();
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
    super.refresh_fetch();
    // set bounds
    {
      CTabItem item = (CTabItem) getObject();
      Rectangle bounds = new Rectangle(item.getBounds());
      setModelBounds(bounds);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notification that this {@link CTabItemInfo} was used as target of CREATE or ADD operation.
   */
  public void command_TARGET_after(ControlInfo control) throws Exception {
    doSelect();
  }
}
