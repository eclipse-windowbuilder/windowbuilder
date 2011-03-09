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
 * Implementation of {@link Command} that removes {@link CategoryInfo}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class CategoryRemoveCommand extends Command {
  public static final String ID = "categoryRemove";
  private final String m_id;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryRemoveCommand(CategoryInfo category) {
    m_id = category.getId();
  }

  public CategoryRemoveCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    CategoryInfo category = DeviceManager.getCategory(m_id);
    if (category != null) {
      DeviceManager.getCategories().remove(category);
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
  }
}
