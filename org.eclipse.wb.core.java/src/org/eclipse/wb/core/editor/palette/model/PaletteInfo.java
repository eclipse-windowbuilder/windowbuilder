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
package org.eclipse.wb.core.editor.palette.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model of palette.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class PaletteInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return StringUtils.join(m_categories.iterator(), ",\n\t");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Categories
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<CategoryInfo> m_categories = Lists.newArrayList();

  /**
   * @return the {@link List} of {@link CategoryInfo}.
   */
  public List<CategoryInfo> getCategories() {
    return m_categories;
  }

  /**
   * Adds new {@link CategoryInfo}.
   */
  public void addCategory(CategoryInfo category) {
    Assert.isLegal(
        !m_categories.contains(category),
        "Duplicate category with id = " + category.getId());
    m_categories.add(category);
  }

  public void moveCategory(String id, String nextId) {
    // don't move before itself, this is no-op
    if (id.equals(nextId)) {
      return;
    }
    // prepare category to move
    CategoryInfo category = getCategory(id);
    if (category == null) {
      return;
    }
    // remove source
    m_categories.remove(category);
    // add to new location
    CategoryInfo nextCategory = getCategory(nextId);
    if (nextCategory != null) {
      int index = m_categories.indexOf(nextCategory);
      m_categories.add(index, category);
    } else {
      m_categories.add(category);
    }
  }

  /**
   * @return the {@link CategoryInfo} with given id.
   */
  public CategoryInfo getCategory(String id) {
    for (CategoryInfo category : m_categories) {
      if (category.getId().equals(id)) {
        return category;
      }
    }
    // not found
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link EntryInfo} with given <code>id</code> or <code>null</code> if not found.
   */
  public EntryInfo getEntry(String id) {
    for (CategoryInfo category : m_categories) {
      for (EntryInfo entry : category.getEntries()) {
        if (entry.getId().equals(id)) {
          return entry;
        }
      }
    }
    // not found
    return null;
  }
}
