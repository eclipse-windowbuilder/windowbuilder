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

/**
 * {@link Command} for adding new {@link CategoryInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class CategoryAddCommand extends Command {
  public static final String ID = "categoryAdd";
  private final String m_id;
  private final String m_name;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryAddCommand(String id, String name) {
    m_id = id;
    m_name = name;
  }

  public CategoryAddCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_name = attributes.getValue("name");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    CategoryInfo category = new CategoryInfo(m_id, m_name);
    DeviceManager.getCategories().add(category);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    super.addAttributes();
    addAttribute("id", m_id);
    addAttribute("name", m_name);
  }
}
