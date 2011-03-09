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
import org.eclipse.wb.internal.swing.laf.model.UserDefinedLafInfo;

import org.xml.sax.Attributes;

import java.util.List;

/**
 * Implementation of {@link Command} that adds new {@link UserDefinedLafInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public final class AddCommand extends EditCommand {
  // constants
  private static final String ATTR_CATEGORY_ID = "category-id";
  public static final String ID = "add";
  // fields
  private final String m_categoryID;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public AddCommand(CategoryInfo category, UserDefinedLafInfo lafInfo) {
    super(lafInfo.getID(), lafInfo.getName(), lafInfo.getClassName(), lafInfo.getJarFile());
    m_categoryID = category.getID();
  }

  public AddCommand(Attributes attributes) {
    super(attributes);
    m_categoryID = attributes.getValue(ATTR_CATEGORY_ID);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    CategoryInfo category = LafSupport.getCategory(m_categoryID);
    if (category != null) {
      UserDefinedLafInfo lafInfo = new UserDefinedLafInfo(m_id, m_name, m_className, m_jarName);
      category.add(lafInfo);
      lafInfo.setCategory(category);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes(XmlWriter writer) {
    super.addAttributes(writer);
    addAttribute(writer, ATTR_CATEGORY_ID, m_categoryID);
  }

  @Override
  public void addToCommandList(List<Command> commands) {
    commands.add(this);
  }
}
