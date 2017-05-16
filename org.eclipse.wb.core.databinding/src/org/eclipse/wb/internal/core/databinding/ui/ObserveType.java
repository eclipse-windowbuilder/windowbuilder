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
package org.eclipse.wb.internal.core.databinding.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.Activator;

import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Special marker for separate observable's and properties to categories: beans, widgets and others.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class ObserveType {
  /**
   * Java Beans type.
   */
  public static final ObserveType BEANS = new ObserveType("Beans",
      Activator.getImage("Beans_ObserveType.gif"));
  /**
   * UI widgets type.
   */
  public static final ObserveType WIDGETS = new ObserveType("Widgets",
      Activator.getImage("Widgets_ObserveType.gif"),
      ExpandedStrategy.ExpandedAll);
  //
  public static final List<ObserveType> TYPES = Lists.newArrayList(
      ObserveType.WIDGETS,
      ObserveType.BEANS);
  //
  private final String m_name;
  private final Image m_image;
  private final ExpandedStrategy m_strategy;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType(String name, Image image) {
    this(name, image, ExpandedStrategy.None);
  }

  public ObserveType(String name, Image image, ExpandedStrategy strategy) {
    m_name = name;
    m_image = image;
    m_strategy = strategy;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the name to display for user.
   */
  public String getName() {
    return m_name;
  }

  /**
   * @return the image to display for user.
   */
  public Image getImage() {
    return m_image;
  }

  public ExpandedStrategy getExpandedStrategy() {
    return m_strategy;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return m_name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpandedStrategy
  //
  ////////////////////////////////////////////////////////////////////////////
  public static enum ExpandedStrategy {
    None, ExpandedAll
  }
}