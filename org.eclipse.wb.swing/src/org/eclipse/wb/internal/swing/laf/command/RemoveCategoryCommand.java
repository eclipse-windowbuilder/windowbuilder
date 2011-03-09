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
 * Implementation of {@link Command} that removes {@link CategoryInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public final class RemoveCategoryCommand extends Command {
  // constants
  public static final String ID = "remove-category";
  // fields
  private final String m_id;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public RemoveCategoryCommand(CategoryInfo category) {
    m_id = category.getID();
  }

  public RemoveCategoryCommand(Attributes attributes) {
    m_id = attributes.getValue(ATTR_ID);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    LafSupport.removeLAFCategory(m_id);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes(XmlWriter writer) {
    addAttribute(writer, ATTR_ID, m_id);
  }
}
