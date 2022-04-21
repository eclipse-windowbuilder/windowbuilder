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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPageLayout;

/**
 * Model for editor area in {@link IPageLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class EditorAreaInfo extends ObjectInfo implements IPageLayoutTopLevelInfo {
  private final PageLayoutInfo m_page;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditorAreaInfo(PageLayoutInfo page) throws Exception {
    m_page = page;
    m_page.addChild(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "(editor area)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObjectPresentation getPresentation() {
    return new DefaultObjectPresentation(this) {
      @Override
      public Image getIcon() throws Exception {
        return Activator.getImage("info/perspective/editor.gif");
      }

      @Override
      public String getText() throws Exception {
        return "(editor area)";
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  private CTabFolder m_folder;

  /**
   * Renders {@link EditorAreaInfo} by creating its {@link Control}.
   */
  void render(Composite parent) throws Exception {
    m_folder = PageLayoutInfo.createPartFolder(parent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private Rectangle m_bounds;

  /**
   * @return the {@link PageLayoutInfo}, i.e. just casted parent.
   */
  public PageLayoutInfo getPage() {
    return m_page;
  }

  @Override
  public String getId() {
    return IPageLayout.ID_EDITOR_AREA;
  }

  @Override
  public String getIdSource() {
    return "org.eclipse.ui.IPageLayout.ID_EDITOR_AREA";
  }

  /**
   * @return the {@link Control} that represents this {@link EditorAreaInfo}.
   */
  public Control getControl() {
    return m_folder;
  }

  /**
   * @return the bounds of {@link EditorAreaInfo} relative to {@link PageLayoutInfo}.
   */
  public Rectangle getBounds() {
    return m_bounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    m_folder = null;
    m_bounds = null;
    super.refresh_dispose();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    m_bounds = CoordinateUtils.getBounds(m_page.getComposite(), m_folder);
    super.refresh_fetch();
  }
}
