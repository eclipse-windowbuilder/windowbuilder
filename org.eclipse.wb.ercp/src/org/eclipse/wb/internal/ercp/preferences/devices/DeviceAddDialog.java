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
import org.eclipse.wb.internal.ercp.devices.command.DeviceAddCommand;
import org.eclipse.wb.internal.ercp.devices.model.CategoryInfo;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.eclipse.swt.widgets.Composite;

/**
 * Dialog for adding new {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device.ui
 */
public final class DeviceAddDialog extends DeviceAbstractDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceAddDialog() {
    super(DeviceMessages.DeviceAddDialog_title, DeviceMessages.DeviceAddDialog_message);
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
    m_displayField_x.setText("0");
    m_displayField_y.setText("0");
    m_displayField_width.setText("320");
    m_displayField_height.setText("240");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  public Command getCommand(CategoryInfo targetCategory) {
    Rectangle displayBounds =
        new Rectangle(Integer.parseInt(m_displayField_x.getText()),
            Integer.parseInt(m_displayField_y.getText()),
            Integer.parseInt(m_displayField_width.getText()),
            Integer.parseInt(m_displayField_height.getText()));
    return new DeviceAddCommand(targetCategory,
        "device_" + System.currentTimeMillis(),
        m_nameField.getText(),
        m_imageField.getText(),
        displayBounds);
  }
}
