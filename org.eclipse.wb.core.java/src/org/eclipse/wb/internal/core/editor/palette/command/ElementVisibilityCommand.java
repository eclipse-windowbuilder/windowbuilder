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
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.xml.sax.Attributes;

import java.util.List;

/**
 * Implementation of {@link Command} that update "visible" property of {@link AbstractElementInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class ElementVisibilityCommand extends Command {
  public static final String ID = "visibleElement";
  private final String m_id;
  private final boolean m_visible;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ElementVisibilityCommand(AbstractElementInfo element, boolean visible) {
    m_id = element.getId();
    m_visible = visible;
  }

  public ElementVisibilityCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_visible = "true".equals(attributes.getValue("visible"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute(PaletteInfo palette) {
    // try category
    {
      CategoryInfo category = palette.getCategory(m_id);
      if (category != null) {
        category.setVisible(m_visible);
      }
    }
    // try entry
    {
      EntryInfo entry = palette.getEntry(m_id);
      if (entry != null) {
        entry.setVisible(m_visible);
      }
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
    addAttribute("visible", m_visible);
  }

  @Override
  public void addToCommandList(final List<Command> commands) {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        removeCommands(commands, ElementVisibilityCommand.class, m_id);
      }
    });
    commands.add(this);
  }
}
