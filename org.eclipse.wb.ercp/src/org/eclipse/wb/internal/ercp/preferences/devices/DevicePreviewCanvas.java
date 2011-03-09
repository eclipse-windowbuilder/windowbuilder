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

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * {@link Canvas} for displaying {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device.ui
 */
public final class DevicePreviewCanvas extends Canvas {
  private DeviceInfo m_device;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DevicePreviewCanvas(Composite parent, int style) {
    super(parent, style);
    addListener(SWT.Paint, new Listener() {
      public void handleEvent(Event event) {
        if (m_device != null) {
          DrawUtils.drawScaledImage(event.gc, m_device.getImage(), getClientArea());
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link DeviceInfo} for display.
   */
  public void setDevice(DeviceInfo device) {
    m_device = device;
    redraw();
  }
}
