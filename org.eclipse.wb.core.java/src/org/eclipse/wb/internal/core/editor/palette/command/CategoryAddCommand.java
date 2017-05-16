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
package org.eclipse.wb.internal.core.editor.palette.command;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;

import org.xml.sax.Attributes;

import java.util.List;

/**
 * Implementation of {@link Command} that adds new {@link CategoryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class CategoryAddCommand extends CategoryAbstractCommand {
  public static final String ID = "addCategory";
  private final String m_nextCategoryId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryAddCommand(String id,
      String name,
      String description,
      boolean visible,
      boolean open,
      String nextCategoryId) {
    super(id, name, description, visible, open);
    m_nextCategoryId = nextCategoryId;
  }

  public CategoryAddCommand(Attributes attributes) {
    super(attributes);
    m_nextCategoryId = attributes.getValue("nextCategory");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute(PaletteInfo palette) {
    // create category
    CategoryInfo category = new CategoryInfo();
    category.setId(m_id);
    updateElement(category);
    // add category
    List<CategoryInfo> categories = palette.getCategories();
    CategoryInfo nextCategory = palette.getCategory(m_nextCategoryId);
    int index = categories.indexOf(nextCategory);
    if (index != -1) {
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
    super.addAttributes();
    addAttribute("nextCategory", m_nextCategoryId);
  }
}
