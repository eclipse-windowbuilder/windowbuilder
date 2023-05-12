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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.RectangleSupport;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

/**
 * Model for {@link org.eclipse.jface.fieldassist.ControlDecoration}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class ControlDecorationInfo extends AbstractComponentInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ControlDecorationInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ControlInfo} that is decorated by this {@link ControlDecorationInfo}.
   */
  public ControlInfo getControl() {
    return (ControlInfo) getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private Image m_decorationImage;
  private Image m_iconImage;
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public Image getIcon() throws Exception {
      if (m_decorationImage != null && m_iconImage != null) {
        return m_iconImage;
      }
      return super.getIcon();
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
  public void refresh_dispose() throws Exception {
    super.refresh_dispose();
    // dispose image
    if (m_iconImage != null) {
      m_iconImage.dispose();
      m_iconImage = null;
    }
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    // prepare "real" image
    m_decorationImage = (Image) ReflectionUtils.invokeMethod(getObject(), "getImage()");
    // if no "real" image, set default one
    if (m_decorationImage == null) {
      ReflectionUtils.invokeMethod(
          getObject(),
          "setImage(org.eclipse.swt.graphics.Image)",
          Activator.getImage("info/ControlDecoration/default.gif"));
    }
  }

  @Override
  protected void refresh_fetch() throws Exception {
    // bounds
    {
      Control targetControl = (Control) ReflectionUtils.invokeMethod(getObject(), "getControl()");
      Rectangle decorationRectangle =
          RectangleSupport.getRectangle(ReflectionUtils.invokeMethod(
              getObject(),
              "getDecorationRectangle(org.eclipse.swt.widgets.Control)",
              targetControl));
      setModelBounds(decorationRectangle);
    }
    // image icon
    if (m_decorationImage != null) {
      m_iconImage = new Image(null, 16, 16);
      GC gc = new GC(m_iconImage);
      try {
        DrawUtils.drawImageCHCV(gc, m_decorationImage, 0, 0, 16, 16);
      } finally {
        gc.dispose();
      }
    }
    // continue
    super.refresh_fetch();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Decorates {@link ControlInfo} with this new {@link ControlDecorationInfo}.
   */
  public void command_CREATE(ControlInfo target) throws Exception {
    JavaInfoUtils.add(this, null, target, null);
  }

  /**
   * Decorates {@link ControlInfo} with this (already existing) {@link ControlDecorationInfo}.
   */
  public void command_ADD(ControlInfo target) throws Exception {
    JavaInfoUtils.move(this, null, target, null);
  }
}
