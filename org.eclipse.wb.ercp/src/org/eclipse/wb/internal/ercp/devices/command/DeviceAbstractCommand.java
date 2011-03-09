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
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.xml.sax.Attributes;

/**
 * Abstract {@link Command} that works with {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public abstract class DeviceAbstractCommand extends Command {
  protected final String m_id;
  protected final String m_name;
  protected final String m_imagePath;
  protected final Rectangle m_displayBounds;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceAbstractCommand(String id, String name, String imagePath, Rectangle displayBounds) {
    m_id = id;
    m_name = name;
    m_imagePath = imagePath;
    m_displayBounds = displayBounds;
  }

  public DeviceAbstractCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_name = attributes.getValue("name");
    m_imagePath = attributes.getValue("image");
    m_displayBounds =
        new Rectangle(Integer.parseInt(attributes.getValue("displayX")),
            Integer.parseInt(attributes.getValue("displayY")),
            Integer.parseInt(attributes.getValue("displayWidth")),
            Integer.parseInt(attributes.getValue("displayHeight")));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    addAttribute("id", m_id);
    addAttribute("name", m_name);
    addAttribute("image", m_imagePath);
    addAttribute("displayX", "" + m_displayBounds.x);
    addAttribute("displayY", "" + m_displayBounds.y);
    addAttribute("displayWidth", "" + m_displayBounds.width);
    addAttribute("displayHeight", "" + m_displayBounds.height);
  }
}
