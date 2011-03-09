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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.collections.map.MultiKeyMap;
import org.osgi.framework.Bundle;

import java.net.URL;

/**
 * Description for mobile device for eRCP.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class DeviceInfo extends AbstractDeviceInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final boolean m_contributed;
  private final Bundle m_contributionBundle;
  private String m_imagePath;
  private Image m_image;
  private Rectangle m_displayBounds;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceInfo(String id, String name, String imagePath, Image image, Rectangle displayBounds)
      throws Exception {
    m_contributed = false;
    m_contributionBundle = null;
    //
    m_id = id;
    m_name = name;
    m_imagePath = imagePath;
    m_image = image;
    m_displayBounds = displayBounds;
  }

  public DeviceInfo(IConfigurationElement element) throws Exception {
    m_contributed = true;
    m_contributionBundle = ExternalFactoriesHelper.getExtensionBundle(element);
    //
    m_id = ExternalFactoriesHelper.getRequiredAttribute(element, "id");
    m_name = ExternalFactoriesHelper.getRequiredAttribute(element, "name");
    m_imagePath = ExternalFactoriesHelper.getRequiredAttribute(element, "image");
    // display
    {
      IConfigurationElement[] displayElements = element.getChildren("display");
      Assert.equals(1, displayElements.length, "Exactly one \"display\" element expected, but "
          + displayElements.length
          + " found.");
      IConfigurationElement displayElement = displayElements[0];
      m_displayBounds =
          new Rectangle(ExternalFactoriesHelper.getRequiredAttributeInteger(displayElement, "x"),
              ExternalFactoriesHelper.getRequiredAttributeInteger(displayElement, "y"),
              ExternalFactoriesHelper.getRequiredAttributeInteger(displayElement, "width"),
              ExternalFactoriesHelper.getRequiredAttributeInteger(displayElement, "height"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link DeviceInfo} comes form plugin extension, so can not be
   *         modified by user.
   */
  public boolean isContributed() {
    return m_contributed;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the bounds of display on image.
   */
  public Rectangle getDisplayBounds() {
    return m_displayBounds;
  }

  /**
   * @return the bounds of display on image.
   */
  public void setDisplayBounds(Rectangle displayBounds) {
    m_displayBounds = displayBounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the path to the image.
   */
  public String getImagePath() {
    return m_imagePath;
  }

  /**
   * @return the {@link Image} of this device.
   */
  public Image getImage() {
    if (m_image == null) {
      m_image = getImage(m_contributionBundle, m_id, m_imagePath);
    }
    return m_image;
  }

  /**
   * Sets the {@link Image} and path to this image.
   */
  public void setImage(String imagePath, Image image) {
    m_imagePath = imagePath;
    m_image = image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Category
  //
  ////////////////////////////////////////////////////////////////////////////
  private CategoryInfo m_category;

  /**
   * @return the parent {@link CategoryInfo}.
   */
  public CategoryInfo getCategory() {
    return m_category;
  }

  /**
   * Sets the new parent {@link CategoryInfo}.
   */
  public void setCategory(CategoryInfo category) {
    m_category = category;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final MultiKeyMap/*Bundle + path -> icon*/m_forcedIcons = new MultiKeyMap();

  /**
   * Returns the {@link Image} for {@link DeviceInfo} from plugin.
   * 
   * @param element
   *          the {@link IConfigurationElement} that contributes the device.
   * @param id
   *          the id of device.
   * @param path
   *          the path to the image.
   */
  private static Image getImage(final Bundle bundle, final String id, final String path) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
      public Image runObject() throws Exception {
        Image image = (Image) m_forcedIcons.get(bundle, path);
        if (image == null) {
          URL iconURL = bundle.getEntry(path);
          Assert.isNotNull(iconURL, "Can not find forced icon for " + id + " " + path);
          image = new Image(Display.getDefault(), iconURL.openStream());
          m_forcedIcons.put(bundle, path, image);
        }
        return image;
      }
    }, null);
  }
}
