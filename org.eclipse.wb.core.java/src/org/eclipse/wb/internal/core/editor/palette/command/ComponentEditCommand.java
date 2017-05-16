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

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.xml.sax.Attributes;

import java.util.List;

/**
 * Implementation of {@link Command} that edits {@link ComponentEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class ComponentEditCommand extends ComponentAbstractCommand {
  public static final String ID = "editComponent";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentEditCommand(String id,
      String name,
      String description,
      boolean visible,
      String className) {
    super(id, name, description, visible, className);
  }

  public ComponentEditCommand(Attributes attributes) {
    super(attributes);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute(PaletteInfo palette) {
    EntryInfo entry = palette.getEntry(m_id);
    if (entry instanceof ComponentEntryInfo) {
      updateElement(entry);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addToCommandList(final List<Command> commands) {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        removeCommands(commands, ComponentEditCommand.class, m_id);
      }
    });
    commands.add(this);
  }
}
