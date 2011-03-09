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
 * {@link Command} for adding new {@link CategoryInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public final class AddCategoryCommand extends Command {
  // constants
  public static final String ID = "add-category";
  // fields
  private final String m_id;
  private final String m_name;
  private boolean m_executed;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public AddCategoryCommand(String id, String name) {
    m_id = id;
    m_name = name;
  }

  public AddCategoryCommand(Attributes attributes) {
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
    // this may be called twice because this command need to be executed while adding LAF and the user adds new category in adding LAF dialog.
    if (!m_executed) {
      LafSupport.addLAFCategory(m_id, m_name);
      m_executed = true;
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
