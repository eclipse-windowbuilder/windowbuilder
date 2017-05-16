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
package org.eclipse.wb.internal.core.model.description.helpers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;

import java.util.List;
import java.util.Map;

/**
 * Helper for accessing {@link LayoutDescription}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class LayoutDescriptionHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private LayoutDescriptionHelper() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_ID = "org.eclipse.wb.core.layoutManagers";
  private static final Map<ToolkitDescription, List<LayoutDescription>> m_layouts =
      Maps.newHashMap();

  /**
   * @return the {@link List} of {@link LayoutDescription}'s contributed for given toolkit.
   */
  public static List<LayoutDescription> get(ToolkitDescription toolkit) {
    List<LayoutDescription> layouts = m_layouts.get(toolkit);
    if (layouts == null) {
      layouts = Lists.newArrayList();
      m_layouts.put(toolkit, layouts);
      //
      for (IConfigurationElement element : ExternalFactoriesHelper.getElements(POINT_ID, "layout")) {
        String toolkitId = ExternalFactoriesHelper.getRequiredAttribute(element, "toolkit");
        if (toolkitId.equals(toolkit.getId())) {
          layouts.add(new LayoutDescription(toolkit, element));
        }
      }
    }
    return layouts;
  }

  /**
   * @return the {@link LayoutDescription} with given id, or <code>null</code> if not found.
   */
  public static LayoutDescription get(ToolkitDescription toolkit, String id) {
    for (LayoutDescription layout : get(toolkit)) {
      if (layout.getId().equals(id)) {
        return layout;
      }
    }
    // not found
    return null;
  }
}
