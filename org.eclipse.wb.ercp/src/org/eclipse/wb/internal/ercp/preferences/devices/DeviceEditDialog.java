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
package org.eclipse.wb.internal.ercp.preferences.devices;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.ercp.devices.DeviceMessages;
import org.eclipse.wb.internal.ercp.devices.command.Command;
import org.eclipse.wb.internal.ercp.devices.command.DeviceEditCommand;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.eclipse.swt.widgets.Composite;

/**
 * Dialog for modifying {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device.ui
 */
public final class DeviceEditDialog extends DeviceAbstractDialog {
  private final DeviceInfo m_device;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceEditDialog(DeviceInfo device) {
    super(DeviceMessages.DeviceEditDialog_title, DeviceMessages.DeviceEditDialog_message);
    m_device = device;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ///////////////////////////////////////////////////////////////////////////
  /**
   * Creates controls on this dialog.
   */
  @Override
  protected void createControls(Composite container) {
    super.createControls(container);
    m_nameField.setText(m_device.getName());
    m_imageField.setText(m_device.getImagePath());
    m_displayField_x.setText("" + m_device.getDisplayBounds().x);
    m_displayField_y.setText("" + m_device.getDisplayBounds().y);
    m_displayField_width.setText("" + m_device.getDisplayBounds().width);
    m_displayField_height.setText("" + m_device.getDisplayBounds().height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  public Command getCommand() {
    Rectangle displayBounds =
        new Rectangle(Integer.parseInt(m_displayField_x.getText()),
            Integer.parseInt(m_displayField_y.getText()),
            Integer.parseInt(m_displayField_width.getText()),
            Integer.parseInt(m_displayField_height.getText()));
    return new DeviceEditCommand(m_device.getId(),
        m_nameField.getText(),
        m_imageField.getText(),
        displayBounds);
  }
}
