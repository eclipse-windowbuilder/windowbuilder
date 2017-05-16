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
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;

import org.xml.sax.Attributes;

/**
 * Implementation of {@link Command} that adds new {@link ComponentEditCommand}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class ComponentAddCommand extends ComponentAbstractCommand {
  public static final String ID = "addComponent";
  private final String m_categoryId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentAddCommand(String id,
      String name,
      String description,
      boolean visible,
      String className,
      CategoryInfo category) {
    super(id, name, description, visible, className);
    m_categoryId = category.getId();
  }

  public ComponentAddCommand(Attributes attributes) {
    super(attributes);
    m_categoryId = attributes.getValue("category");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute(PaletteInfo palette) {
    // create component entry
    ComponentEntryInfo component = new ComponentEntryInfo();
    component.setId(m_id);
    updateElement(component);
    // prepare category
    CategoryInfo category = palette.getCategory(m_categoryId);
    category.addEntry(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    super.addAttributes();
    addAttribute("category", m_categoryId);
  }
}
