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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.rcp.Activator;

import org.eclipse.swt.graphics.Image;

/**
 * {@link ObjectInfo} that represents empty position in {@link AbstractPositionCompositeInfo}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class AbstractPositionInfo extends ObjectInfo {
  private final AbstractPositionCompositeInfo m_composite;
  private final String m_method;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractPositionInfo(AbstractPositionCompositeInfo composite, String method) {
    setParent(composite);
    setBroadcastSupport(composite.getBroadcastSupport());
    m_composite = composite;
    m_method = method;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Position
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the host {@link AbstractPositionCompositeInfo}.
   */
  public AbstractPositionCompositeInfo getComposite() {
    return m_composite;
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
        return Activator.getImage("info/position/element_transparent.png");
      }

      public String getText() throws Exception {
        return m_method;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Calls {@link AbstractPositionCompositeInfo#command_CREATE(ControlInfo, String)}.
   */
  public void command_CREATE(ControlInfo control) throws Exception {
    m_composite.command_CREATE(control, m_method);
  }

  /**
   * Calls {@link AbstractPositionCompositeInfo#command_MOVE(ControlInfo, String)}.
   */
  public void command_MOVE(ControlInfo control) throws Exception {
    m_composite.command_MOVE(control, m_method);
  }
}
