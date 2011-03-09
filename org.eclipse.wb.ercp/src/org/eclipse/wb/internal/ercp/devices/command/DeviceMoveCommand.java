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
import org.eclipse.wb.internal.ercp.devices.model.CategoryInfo;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.xml.sax.Attributes;

import java.util.List;
import java.util.ListIterator;

/**
 * Implementation of {@link Command} that moves {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class DeviceMoveCommand extends Command {
  public static final String ID = "deviceMove";
  private final String m_id;
  private final String m_categoryId;
  private final String m_nextDeviceId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceMoveCommand(DeviceInfo device, CategoryInfo category, DeviceInfo nextDevice) {
    m_id = device.getId();
    m_categoryId = category.getId();
    m_nextDeviceId = nextDevice != null ? nextDevice.getId() : null;
  }

  public DeviceMoveCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_categoryId = attributes.getValue("category");
    m_nextDeviceId = attributes.getValue("nextDevice");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    DeviceInfo device = DeviceManager.getDevice(m_id);
    CategoryInfo category = DeviceManager.getCategory(m_categoryId);
    if (device == null || category == null) {
      return;
    }
    // don't move before itself, this is no-op
    if (m_id.equals(m_nextDeviceId)) {
      return;
    }
    // remove source entry
    device.getCategory().removeDevice(device);
    // add to new location
    DeviceInfo nextDevice = DeviceManager.getDevice(m_nextDeviceId);
    if (nextDevice != null) {
      int index = category.getDevices().indexOf(nextDevice);
      category.addDevice(index, device);
    } else {
      category.addDevice(device);
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
    addAttribute("category", m_categoryId);
    addAttribute("nextDevice", m_nextDeviceId);
  }

  @Override
  public void addToCommandList(List<Command> commands) {
    ListIterator<Command> I = commands.listIterator(commands.size());
    while (I.hasPrevious()) {
      Command command = I.previous();
      if (command instanceof DeviceMoveCommand) {
        DeviceMoveCommand moveCommand = (DeviceMoveCommand) command;
        if (m_id.equals(moveCommand.m_id)) {
          // remove moves of source entry
          I.remove();
        } else if (m_id.equals(moveCommand.m_nextDeviceId)) {
          // if source entry used as target, stop optimizing
          break;
        }
      }
    }
    // add command
    commands.add(this);
  }
}
