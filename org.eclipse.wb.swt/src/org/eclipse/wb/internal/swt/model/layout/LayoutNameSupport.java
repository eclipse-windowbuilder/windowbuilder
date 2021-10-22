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
package org.eclipse.wb.internal.swt.model.layout;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

import java.util.Map;

/**
 * Support for managing name of {@link LayoutInfo}, so that it corresponds to the name of its parent
 * {@link CompositeInfo}.
 *
 * @author sablin_aa
 * @coverage swt.model.layout
 */
public final class LayoutNameSupport
    extends
      org.eclipse.wb.internal.core.model.layout.LayoutNameSupport<LayoutInfo> {
  public final static String[] TEMPLATES = new String[]{
      "${layoutAcronym}_${compositeName}",
      "${layoutAcronym}${compositeName-cap}",
      "${compositeName}${layoutClassName}",
      "${defaultName}"};

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutNameSupport(LayoutInfo layout) {
    super(layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTemplate() {
    IPreferenceStore preferences = m_childInfo.getDescription().getToolkit().getPreferences();
    String template = preferences.getString(IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);
    if (!isValidTemplate(TEMPLATES, template)) {
      template = getTemplateForDefault();
    }
    return template;
  }

  @Override
  protected Map<String, String> getValueMap() {
    // prepare variables
    Map<String, String> valueMap = Maps.newTreeMap();
    {
      valueMap.put("layoutAcronym", getAcronym());
      valueMap.put("layoutClassName", getClassName());
      valueMap.put("compositeName", getParentName());
      valueMap.put("compositeName-cap", getParentNameCap());
    }
    return valueMap;
  }
}