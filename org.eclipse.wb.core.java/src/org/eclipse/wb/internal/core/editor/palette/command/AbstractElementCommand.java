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

import org.xml.sax.Attributes;

/**
 * Abstract {@link Command} that edits {@link AbstractElementInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class AbstractElementCommand extends Command {
  protected final String m_id;
  protected final String m_name;
  protected final String m_description;
  protected final boolean m_visible;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractElementCommand(String id, String name, String description, boolean visible) {
    m_id = id;
    m_name = name;
    m_description = description;
    m_visible = visible;
  }

  public AbstractElementCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_name = attributes.getValue("name");
    m_description = attributes.getValue("description");
    m_visible = "true".equals(attributes.getValue("visible"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates given {@link AbstractElementInfo}.
   */
  protected void updateElement(AbstractElementInfo element) {
    element.setName(m_name);
    element.setDescription(m_description);
    element.setVisible(m_visible);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    addAttribute("id", m_id);
    addAttribute("name", m_name);
    addAttribute("description", m_description);
    addAttribute("visible", m_visible);
  }
}
