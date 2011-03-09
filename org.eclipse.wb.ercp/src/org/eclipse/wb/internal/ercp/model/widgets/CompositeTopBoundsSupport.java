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
package org.eclipse.wb.internal.ercp.model.widgets;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.devices.DeviceManager;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Implementation of {@link org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport} for
 * eRCP.
 * 
 * @author scheglov_ke
 * @coverage ercp.model.widgets
 */
public final class CompositeTopBoundsSupport
    extends
      org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport {
  /**
   * The {@link QualifiedName} for associating {@link DeviceInfo} with compilation unit.
   */
  public static final QualifiedName KEY_DEVICE_ID = new QualifiedName(Activator.PLUGIN_ID,
      "deviceId");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeTopBoundsSupport(CompositeInfo composite) {
    super(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    // if device selected for compilation unit, use its display size
    {
      DeviceInfo device = getDevice();
      if (device != null) {
        Rectangle displayBounds = device.getDisplayBounds();
        ControlSupport.setSize(m_component.getObject(), displayBounds.width, displayBounds.height);
        return;
      }
    }
    // no device, apply as usually
    super.apply();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Device access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link DeviceInfo} associated with this component or <code>null</code> if no device
   *         associated.
   */
  public DeviceInfo getDevice() throws Exception {
    String deviceId = getUnderlyingResource().getPersistentProperty(KEY_DEVICE_ID);
    {
      DeviceInfo device = DeviceManager.getDevice(deviceId);
      if (device != null) {
        return device;
      }
    }
    // no device
    return null;
  }

  public void setDevice(final DeviceInfo device) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        String deviceId = device != null ? device.getId() : null;
        getUnderlyingResource().setPersistentProperty(KEY_DEVICE_ID, deviceId);
        m_component.refresh();
      }
    });
  }
}
