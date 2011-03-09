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
package org.eclipse.wb.internal.ercp.devices.command;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.ercp.devices.DeviceManager;
import org.eclipse.wb.internal.ercp.devices.model.CategoryInfo;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.eclipse.swt.graphics.Image;

import org.xml.sax.Attributes;

/**
 * Implementation of {@link Command} that adds new {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class DeviceAddCommand extends DeviceAbstractCommand {
  public static final String ID = "deviceAdd";
  private final String m_categoryId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceAddCommand(CategoryInfo category,
      String id,
      String name,
      String imagePath,
      Rectangle displayBounds) {
    super(id, name, imagePath, displayBounds);
    m_categoryId = category.getId();
  }

  public DeviceAddCommand(Attributes attributes) {
    super(attributes);
    m_categoryId = attributes.getValue("categoryId");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    try {
      CategoryInfo category = DeviceManager.getCategory(m_categoryId);
      if (category != null) {
        Image image = SwtResourceManager.getImage(m_imagePath);
        category.addDevice(new DeviceInfo(m_id, m_name, m_imagePath, image, m_displayBounds));
      }
    } catch (Throwable e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    super.addAttributes();
    addAttribute("categoryId", m_categoryId);
  }
}
