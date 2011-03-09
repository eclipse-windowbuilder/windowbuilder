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
package org.eclipse.wb.internal.swing.java6.model;

import org.eclipse.wb.core.model.IAbstractComponentInfo;

import org.apache.commons.lang.StringUtils;

import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * Spring representing gaps.
 * 
 * @author mitin_aa
 * @coverage swing.model.layout.group
 */
public final class GapSpringInfo extends SpringInfo {
  private final boolean m_isContainer;
  private ComponentPlacement m_placement;
  private IAbstractComponentInfo m_widget1;
  private IAbstractComponentInfo m_widget2;
  private int m_pref = UNSET;
  private int m_max = UNSET;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GapSpringInfo() {
    this(false);
  }

  public GapSpringInfo(boolean isContainer) {
    m_isContainer = isContainer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setPlacementType(ComponentPlacement placement) {
    m_placement = placement;
  }

  public void setGapWidgets(IAbstractComponentInfo widget1, IAbstractComponentInfo widget2) {
    m_widget1 = widget1;
    m_widget2 = widget2;
  }

  public void setPreferredSize(int prefValue) {
    m_pref = prefValue;
  }

  public void setMaximumSize(int maxValue) {
    m_max = maxValue;
  }

  private boolean isPreferred() {
    return m_placement != null;
  }

  private boolean isContainer() {
    return m_isContainer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dump
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void dump(int level, StringBuffer buffer) {
    buffer.append(StringUtils.repeat(" ", level));
    if (isContainer()) {
      buffer.append("G cont sizes=");
      buffer.append((m_pref == UNSET ? "UNSET" : m_pref) + " ");
      buffer.append((m_max == UNSET ? "UNSET" : m_max));
    } else if (isPreferred()) {
      buffer.append("G pref=" + m_placement + " sizes=");
      buffer.append((m_pref == UNSET ? "UNSET" : m_pref) + " ");
      buffer.append((m_max == UNSET ? "UNSET" : m_max));
      if (m_widget1 != null) {
      }
    } else {
      buffer.append("G sizes=");
      super.dump(level, buffer);
    }
    buffer.append("\n");
  }
}
