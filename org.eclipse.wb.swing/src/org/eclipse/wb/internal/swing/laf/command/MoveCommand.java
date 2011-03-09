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
import org.eclipse.wb.internal.swing.laf.model.LafInfo;

import org.xml.sax.Attributes;

import java.util.List;
import java.util.ListIterator;

/**
 * Implementation of {@link Command} which moves the {@link LafInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public final class MoveCommand extends Command {
  // constants
  private static final String ATTR_CATEGORY = "category";
  private static final String ATTR_NEXT_LOOK_N_FEEL = "next-look-n-feel";
  public static final String ID = "move";
  // fields
  private final String m_id;
  private final String m_targetCategoryID;
  private final String m_nextLookAndFeelID;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public MoveCommand(LafInfo laf, CategoryInfo targetCategory, LafInfo lafNext) {
    m_id = laf.getID();
    m_targetCategoryID = targetCategory.getID();
    m_nextLookAndFeelID = lafNext != null ? lafNext.getID() : null;
  }

  public MoveCommand(Attributes attributes) {
    m_id = attributes.getValue(ATTR_ID);
    m_targetCategoryID = attributes.getValue(ATTR_CATEGORY);
    m_nextLookAndFeelID = attributes.getValue(ATTR_NEXT_LOOK_N_FEEL);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    LafInfo lafInfo = LafSupport.getLookAndFeel(m_id);
    CategoryInfo category = LafSupport.getCategory(m_targetCategoryID);
    if (lafInfo == null || category == null) {
      return;
    }
    // don't move before itself
    if (m_id.equals(m_nextLookAndFeelID)) {
      return;
    }
    // remove source entry
    LafSupport.removeLookAndFeel(lafInfo);
    // add to new
    LafInfo lafNext;
    if (m_nextLookAndFeelID != null
        && (lafNext = LafSupport.getLookAndFeel(m_nextLookAndFeelID)) != null) {
      int index = category.getLAFList().indexOf(lafNext);
      category.add(index, lafInfo);
    } else {
      category.add(lafInfo);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes(XmlWriter writer) {
    addAttribute(writer, ATTR_ID, m_id);
    addAttribute(writer, ATTR_CATEGORY, m_targetCategoryID);
    addAttribute(writer, ATTR_NEXT_LOOK_N_FEEL, m_nextLookAndFeelID);
  }

  @Override
  public void addToCommandList(List<Command> commands) {
    ListIterator<Command> I = commands.listIterator(commands.size());
    while (I.hasPrevious()) {
      Command command = I.previous();
      if (command instanceof MoveCommand) {
        MoveCommand moveCommand = (MoveCommand) command;
        if (m_id.equals(moveCommand.m_id)) {
          // remove moves of source entry
          I.remove();
        } else if (m_id.equals(moveCommand.m_nextLookAndFeelID)) {
          // if source entry used as target, stop optimizing
          break;
        }
      }
    }
    // add command
    commands.add(this);
  }
}
