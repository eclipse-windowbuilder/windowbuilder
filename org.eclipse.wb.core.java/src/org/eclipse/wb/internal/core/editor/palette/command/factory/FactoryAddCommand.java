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
package org.eclipse.wb.internal.core.editor.palette.command.factory;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.model.entry.FactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.InstanceFactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;

import org.xml.sax.Attributes;

/**
 * Implementation of {@link Command} that adds new {@link FactoryEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class FactoryAddCommand extends FactoryAbstractCommand {
  public static final String ID = "addFactory";
  private final String m_categoryId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryAddCommand(String id,
      String name,
      String description,
      boolean visible,
      String factoryClassName,
      String methodSignature,
      boolean forStatic,
      CategoryInfo category) {
    super(id, name, description, visible, factoryClassName, methodSignature, forStatic);
    m_categoryId = category.getId();
  }

  public FactoryAddCommand(Attributes attributes) {
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
    // create entry
    FactoryEntryInfo entry;
    if (m_forStatic) {
      entry = new StaticFactoryEntryInfo();
    } else {
      entry = new InstanceFactoryEntryInfo();
    }
    // update entry
    entry.setId(m_id);
    updateElement(entry);
    // prepare category
    CategoryInfo category = palette.getCategory(m_categoryId);
    category.addEntry(entry);
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
