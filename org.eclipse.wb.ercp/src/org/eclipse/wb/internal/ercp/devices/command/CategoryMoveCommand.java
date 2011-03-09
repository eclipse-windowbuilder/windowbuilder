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
package org.eclipse.wb.internal.ercp.devices.command;

import org.eclipse.wb.internal.ercp.devices.DeviceManager;
import org.eclipse.wb.internal.ercp.devices.model.CategoryInfo;

import org.xml.sax.Attributes;

import java.util.List;

/**
 * Implementation of {@link Command} that moves {@link CategoryInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class CategoryMoveCommand extends Command {
  public static final String ID = "categoryMove";
  private final String m_id;
  private final String m_nextCategoryId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryMoveCommand(CategoryInfo category, CategoryInfo nextCategory) {
    m_id = category.getId();
    m_nextCategoryId = nextCategory != null ? nextCategory.getId() : null;
  }

  public CategoryMoveCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_nextCategoryId = attributes.getValue("nextCategory");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    CategoryInfo category = DeviceManager.getCategory(m_id);
    if (category == null) {
      return;
    }
    // don't move before itself, this is no-op
    if (m_id.equals(m_nextCategoryId)) {
      return;
    }
    // remove source
    List<CategoryInfo> categories = DeviceManager.getCategories();
    categories.remove(category);
    // add to new location
    CategoryInfo nextCategory = DeviceManager.getCategory(m_nextCategoryId);
    if (nextCategory != null) {
      int index = categories.indexOf(nextCategory);
      categories.add(index, category);
    } else {
      categories.add(category);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    addAttribute("id", m_id);
    addAttribute("nextCategory", m_nextCategoryId);
  }
}
