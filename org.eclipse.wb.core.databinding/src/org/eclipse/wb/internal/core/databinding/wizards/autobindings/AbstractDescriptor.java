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
package org.eclipse.wb.internal.core.databinding.wizards.autobindings;

import com.google.common.collect.Lists;

import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Abstract descriptor.
 *
 * @author lobas_av
 * @coverage bindings.wizard.auto
 */
public abstract class AbstractDescriptor {
  private final List<String> m_names = Lists.newArrayList();
  private final List<Image> m_images = Lists.newArrayList();
  private String m_name;
  private Image m_image;
  private boolean m_default;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the name with given index to display for user.
   */
  public String getName(int index) {
    return m_names.get(index);
  }

  /**
   * Add the name to display for user.
   */
  public void addName(String name) {
    m_names.add(name);
  }

  /**
   * @return the name to display for user.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Sets the name to display for user.
   */
  public void setName(String name) {
    m_name = name;
  }

  /**
   * @return the image with given index to display for user.
   */
  public Image getImage(int index) {
    return m_images.get(index);
  }

  /**
   * Add the image to display for user.
   */
  public void addImage(Image image) {
    m_images.add(image);
  }

  /**
   * @return the image to display for user.
   */
  public Image getImage() {
    return m_image;
  }

  /**
   * Sets the image to display for user.
   */
  public void setImage(Image image) {
    m_image = image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this descriptor is default.
   */
  public boolean isDefault() {
    return m_default;
  }

  /**
   * Sets default state.
   */
  public void setDefault() {
    m_default = true;
  }

  /**
   * @return <code>true</code> if this descriptor is default for given object.
   */
  public abstract boolean isDefault(Object property);
}