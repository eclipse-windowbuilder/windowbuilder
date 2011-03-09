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
package org.eclipse.wb.internal.swing.model.component.menu;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link IMenuItemInfo} for any {@link ComponentInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
public class ComponentMenuItemInfo extends AbstractMenuObject implements IMenuItemInfo {
  private final ComponentInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentMenuItemInfo(ComponentInfo component) {
    super(component);
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getModel() {
    return m_component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getImage() {
    return m_component.getImage();
  }

  public Rectangle getBounds() {
    return m_component.getBounds();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuItemInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public IMenuInfo getMenu() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  public IMenuPolicy getPolicy() {
    return IMenuPolicy.NOOP;
  }
}
