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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.swt.graphics.Image;

import java.io.ByteArrayInputStream;

/**
 * Information for component on palette.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ComponentPresentation {
  private final String m_key;
  private final String m_toolkitId;
  private final String m_name;
  private final String m_description;
  private byte[] m_iconBytes;
  private Image m_icon;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentPresentation(String key,
      String toolkitId,
      String name,
      String description,
      byte[] iconBytes) {
    m_key = key;
    m_toolkitId = toolkitId;
    m_name = name;
    m_description = description;
    m_iconBytes = iconBytes;
  }

  public ComponentPresentation(String key,
      String toolkitId,
      String name,
      String description,
      Image icon) {
    m_key = key;
    m_toolkitId = toolkitId;
    m_name = name;
    m_description = description;
    m_icon = icon;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getKey() {
    return m_key;
  }

  public String getToolkitId() {
    return m_toolkitId;
  }

  public String getName() {
    return m_name;
  }

  public String getDescription() {
    return m_description;
  }

  public Image getIcon() {
    if (m_icon == null) {
      if (m_iconBytes != null) {
        m_icon = new Image(null, new ByteArrayInputStream(m_iconBytes));
      }
    }
    return m_icon;
  }
}
