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
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;

import org.xml.sax.Attributes;

import java.util.List;
import java.util.ListIterator;

/**
 * Implementation of {@link Command} that moves {@link EntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class EntryMoveCommand extends Command {
  public static final String ID = "moveEntry";
  private final String m_id;
  private final String m_categoryId;
  private final String m_nextEntryId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public EntryMoveCommand(EntryInfo entry, CategoryInfo category, EntryInfo nextEntry) {
    m_id = entry.getId();
    m_categoryId = category.getId();
    m_nextEntryId = nextEntry != null ? nextEntry.getId() : null;
  }

  public EntryMoveCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_categoryId = attributes.getValue("category");
    m_nextEntryId = attributes.getValue("nextEntry");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute(PaletteInfo palette) {
    // prepare entry to move
    EntryInfo entry = palette.getEntry(m_id);
    if (entry == null) {
      return;
    }
    // prepare target category
    CategoryInfo category = palette.getCategory(m_categoryId);
    if (category == null) {
      return;
    }
    // don't move before itself, this is no-op
    if (m_id.equals(m_nextEntryId)) {
      return;
    }
    // remove source entry
    entry.getCategory().removeEntry(entry);
    // add to new location
    EntryInfo nextEntry = palette.getEntry(m_nextEntryId);
    if (nextEntry != null) {
      int index = category.getEntries().indexOf(nextEntry);
      category.addEntry(index, entry);
    } else {
      category.addEntry(entry);
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
    addAttribute("category", m_categoryId);
    addAttribute("nextEntry", m_nextEntryId);
  }

  @Override
  public void addToCommandList(List<Command> commands) {
    ListIterator<Command> I = commands.listIterator(commands.size());
    while (I.hasPrevious()) {
      Command command = I.previous();
      if (command instanceof EntryMoveCommand) {
        EntryMoveCommand moveCommand = (EntryMoveCommand) command;
        if (m_id.equals(moveCommand.m_id)) {
          // remove moves of source entry
          I.remove();
        } else if (m_id.equals(moveCommand.m_nextEntryId)) {
          // if source entry used as target, stop optimizing
          break;
        }
      }
    }
    // add command
    commands.add(this);
  }
}
