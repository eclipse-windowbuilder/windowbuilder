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
package org.eclipse.wb.internal.swing.laf.command;

import org.eclipse.wb.internal.core.utils.XmlWriter;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.internal.swing.laf.model.CategoryInfo;

import org.xml.sax.Attributes;

/**
 * Implementation of {@link Command} that moves {@link CategoryInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public final class MoveCategoryCommand extends Command {
  // constants
  private static final String ATTR_NEXT_CATEGORY = "next-category";
  public static final String ID = "move-category";
  // fields
  private final String m_id;
  private final String m_nextCategoryID;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public MoveCategoryCommand(CategoryInfo category, CategoryInfo nextCategory) {
    m_id = category.getID();
    m_nextCategoryID = nextCategory != null ? nextCategory.getID() : null;
  }

  public MoveCategoryCommand(Attributes attributes) {
    m_id = attributes.getValue(ATTR_ID);
    m_nextCategoryID = attributes.getValue(ATTR_NEXT_CATEGORY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    // don't move before itself
    if (m_id.equals(m_nextCategoryID)) {
      return;
    }
    LafSupport.moveLAFCategory(m_id, m_nextCategoryID);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes(XmlWriter writer) {
    addAttribute(writer, ATTR_ID, m_id);
    addAttribute(writer, ATTR_NEXT_CATEGORY, m_nextCategoryID);
  }
}
