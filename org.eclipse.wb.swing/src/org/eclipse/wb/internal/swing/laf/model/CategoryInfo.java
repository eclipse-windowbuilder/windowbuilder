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
package org.eclipse.wb.internal.swing.laf.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;

import java.util.List;

/**
 * Description for group of {@link LafInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public final class CategoryInfo extends LafEntryInfo {
  private final List<LafInfo> m_lafList = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryInfo(String id, String name) {
    super(id, name);
  }

  public CategoryInfo(IConfigurationElement element) {
    super(ExternalFactoriesHelper.getRequiredAttribute(element, "id"),
        ExternalFactoriesHelper.getRequiredAttribute(element, "name"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LAFInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link LafInfo}'s in this {@link CategoryInfo}.
   */
  public List<LafInfo> getLAFList() {
    return m_lafList;
  }

  /**
   * Adds new {@link LafInfo} using specified index.
   */
  public void add(int index, LafInfo lafInfo) {
    if (!m_lafList.contains(lafInfo)) {
      m_lafList.add(index, lafInfo);
    }
    lafInfo.setCategory(this);
  }

  /**
   * Adds new {@link LafInfo}.
   */
  public void add(LafInfo lafInfo) {
    if (!m_lafList.contains(lafInfo)) {
      m_lafList.add(lafInfo);
    }
    lafInfo.setCategory(this);
  }

  /**
   * Removes given {@link LafInfo} from this {@link CategoryInfo}.
   */
  public void remove(LafInfo lafInfo) {
    m_lafList.remove(lafInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Search
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Non-recursive. Looks up within own children for {@link LafInfo} with given <code>id</code>.
   */
  public LafInfo lookupByID(String id) {
    for (LafInfo lafInfo : m_lafList) {
      if (id.equals(lafInfo.getID())) {
        return lafInfo;
      }
    }
    return null;
  }

  /**
   * Non-recursive. Looks up within own children for {@link LafInfo} with given
   * <code>className</code>.
   */
  public LafInfo lookupByClassName(String className) {
    for (LafInfo lafInfo : m_lafList) {
      if (className.equals(lafInfo.getClassName())) {
        return lafInfo;
      }
    }
    return null;
  }
}
