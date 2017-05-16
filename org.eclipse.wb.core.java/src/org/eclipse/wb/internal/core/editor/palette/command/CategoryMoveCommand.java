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

/**
 * Implementation of {@link Command} that moves {@link CategoryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class CategoryMoveCommand extends Command {
  public static final String ID = "moveCategory";
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
  public void execute(PaletteInfo palette) {
    palette.moveCategory(m_id, m_nextCategoryId);
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
