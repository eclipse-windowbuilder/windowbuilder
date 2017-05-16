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

import org.eclipse.wb.core.editor.palette.model.AbstractElementInfo;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;

import org.xml.sax.Attributes;

/**
 * Abstract {@link Command} for {@link CategoryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class CategoryAbstractCommand extends AbstractElementCommand {
  protected final boolean m_open;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryAbstractCommand(String id,
      String name,
      String description,
      boolean hidden,
      boolean open) {
    super(id, name, description, hidden);
    m_open = open;
  }

  public CategoryAbstractCommand(Attributes attributes) {
    super(attributes);
    m_open = "true".equals(attributes.getValue("open"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final void updateElement(AbstractElementInfo element) {
    super.updateElement(element);
    CategoryInfo category = (CategoryInfo) element;
    category.setOpen(m_open);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    super.addAttributes();
    addAttribute("open", m_open);
  }
}
