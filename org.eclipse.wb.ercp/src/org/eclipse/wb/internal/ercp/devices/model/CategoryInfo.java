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
package org.eclipse.wb.internal.ercp.devices.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;

import java.util.List;

/**
 * Description for group of {@link DeviceInfo} - mobile devices for eRCP.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class CategoryInfo extends AbstractDeviceInfo {
  private final List<DeviceInfo> m_devices = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryInfo(String id, String name) {
    m_id = id;
    m_name = name;
  }

  public CategoryInfo(IConfigurationElement element) throws Exception {
    m_id = ExternalFactoriesHelper.getRequiredAttribute(element, "id");
    m_name = ExternalFactoriesHelper.getRequiredAttribute(element, "name");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Devices
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link DeviceInfo}'s in this {@link CategoryInfo}.
   */
  public List<DeviceInfo> getDevices() {
    return m_devices;
  }

  /**
   * Adds new {@link DeviceInfo} into specified index.
   */
  public void addDevice(int index, DeviceInfo device) {
    if (!m_devices.contains(device)) {
      m_devices.add(index, device);
    }
    device.setCategory(this);
  }

  /**
   * Adds new {@link DeviceInfo}.
   */
  public void addDevice(DeviceInfo device) {
    if (!m_devices.contains(device)) {
      m_devices.add(device);
    }
    device.setCategory(this);
  }

  /**
   * Removes given {@link DeviceInfo} from this {@link CategoryInfo}.
   */
  public void removeDevice(DeviceInfo device) {
    m_devices.remove(device);
  }
}
