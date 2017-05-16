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

import org.eclipse.wb.internal.core.editor.palette.model.entry.AttributesProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;

import java.util.List;

/**
 * Model of category - container for {@link EntryInfo} on palette.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class CategoryInfo extends AbstractElementInfo {
  private final List<EntryInfo> m_entries = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryInfo() {
  }

  public CategoryInfo(String id) {
    setId(id);
  }

  public CategoryInfo(AttributesProvider attributes) {
    // id
    {
      String id = attributes.getAttribute("id");
      Assert.isNotNull(id);
      setId(id);
    }
    // name
    {
      String name = attributes.getAttribute("name");
      Assert.isNotNull(name);
      setName(name);
    }
    // description
    setDescription(attributes.getAttribute("description"));
    // state
    setVisible(getBoolean(attributes, "visible", true));
    setOpen(getBoolean(attributes, "open", true));
    m_optional = getBoolean(attributes, "optional", false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "Category(id='" + getId() + "', name='" + getName() + "', entries=" + m_entries + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Entries
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of {@link EntryInfo}.
   */
  public List<EntryInfo> getEntries() {
    return m_entries;
  }

  /**
   * Adds given {@link EntryInfo}.
   */
  public void addEntry(EntryInfo entry) {
    addEntry(m_entries.size(), entry);
  }

  /**
   * Adds given {@link EntryInfo}.
   */
  public void addEntry(int index, EntryInfo entry) {
    m_entries.add(index, entry);
    entry.setCategory(this);
  }

  /**
   * Removes given {@link EntryInfo}.
   */
  public void removeEntry(EntryInfo entry) {
    m_entries.remove(entry);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Open
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_open;

  /**
   * @return <code>true</code> if this category in open.
   */
  public boolean isOpen() {
    return m_open;
  }

  /**
   * Sets flag if this category in open.
   */
  public void setOpen(boolean open) {
    m_open = open;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Optional
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_optional;

  /**
   * @return <code>true</code> if this category is optional, i.e. for some additional set of
   *         components and should be hidden is these components are not available. Currently we
   *         check this by checking that category is empty.
   */
  public boolean isOptional() {
    return m_optional;
  }
}
