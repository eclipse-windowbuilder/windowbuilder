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
import org.eclipse.wb.internal.ercp.devices.model.AbstractDeviceInfo;
import org.eclipse.wb.internal.ercp.devices.model.CategoryInfo;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.List;

/**
 * {@link Command} changing {@link AbstractDeviceInfo} "visible" property.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class ElementVisibilityCommand extends Command {
  public static final String ID = "visible";
  private final String m_id;
  private final boolean m_visible;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ElementVisibilityCommand(AbstractDeviceInfo element, boolean visible) {
    m_id = element.getId();
    m_visible = visible;
  }

  public ElementVisibilityCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_visible = "true".equals(attributes.getValue("visible"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    // try category
    {
      CategoryInfo category = DeviceManager.getCategory(m_id);
      if (category != null) {
        category.setVisible(m_visible);
      }
    }
    // try device
    {
      DeviceInfo device = DeviceManager.getDevice(m_id);
      if (device != null) {
        device.setVisible(m_visible);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    super.addAttributes();
    addAttribute("id", m_id);
    addAttribute("visible", m_visible);
  }

  @Override
  public void addToCommandList(List<Command> commands) {
    for (Iterator<Command> I = commands.iterator(); I.hasNext();) {
      Command command = I.next();
      if (command instanceof ElementVisibilityCommand) {
        ElementVisibilityCommand elementVisibilityCommand = (ElementVisibilityCommand) command;
        if (elementVisibilityCommand.m_id.equals(m_id)) {
          I.remove();
        }
      }
    }
    commands.add(this);
  }
}
