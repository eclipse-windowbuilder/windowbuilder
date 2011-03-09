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

import org.eclipse.wb.core.model.AbstractComponentInfo;

import org.apache.commons.lang.StringUtils;

import javax.swing.GroupLayout.Alignment;

/**
 * Spring containing component.
 * 
 * @author mitin_aa
 * @coverage swing.model.layout.group
 */
public class WidgetSpringInfo extends SpringInfo {
  private final AbstractComponentInfo m_widget;
  private Alignment m_alignment = Alignment.LEADING;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetSpringInfo(AbstractComponentInfo widget) {
    m_widget = widget;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setAlignment(Alignment alignment) {
    m_alignment = alignment;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dump
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void dump(int level, StringBuffer buffer) {
    String name = m_widget.getVariableSupport().getName();
    buffer.append(StringUtils.repeat(" ", level));
    buffer.append("W align=" + m_alignment + " name=");
    buffer.append((name == null ? m_widget.toString() : name) + " sizes=");
    super.dump(level, buffer);
    buffer.append("\n");
  }
}
