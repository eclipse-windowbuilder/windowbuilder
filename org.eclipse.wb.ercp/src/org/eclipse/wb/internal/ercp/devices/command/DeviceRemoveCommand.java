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

import org.eclipse.wb.internal.ercp.devices.DeviceManager;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.xml.sax.Attributes;

/**
 * Implementation of {@link Command} that removes {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class DeviceRemoveCommand extends Command {
  public static final String ID = "deviceRemove";
  private final String m_id;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceRemoveCommand(DeviceInfo category) {
    m_id = category.getId();
  }

  public DeviceRemoveCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    DeviceInfo device = DeviceManager.getDevice(m_id);
    if (device != null) {
      device.getCategory().removeDevice(device);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    addAttribute("id", m_id);
  }
}
