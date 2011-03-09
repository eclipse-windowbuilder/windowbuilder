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
 * {@link Command} for modifying {@link CategoryInfo} name.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public final class RenameCategoryCommand extends Command {
  // constants
  public static final String ID = "rename-category";
  // fields
  private final String m_id;
  private final String m_name;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public RenameCategoryCommand(CategoryInfo category, String name) {
    m_id = category.getID();
    m_name = name;
  }

  public RenameCategoryCommand(Attributes attributes) {
    m_id = attributes.getValue(ATTR_ID);
    m_name = attributes.getValue(ATTR_NAME);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    CategoryInfo category = LafSupport.getCategory(m_id);
    if (category != null) {
      category.setName(m_name);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes(XmlWriter writer) {
    addAttribute(writer, ATTR_ID, m_id);
    addAttribute(writer, ATTR_NAME, m_name);
  }
}
