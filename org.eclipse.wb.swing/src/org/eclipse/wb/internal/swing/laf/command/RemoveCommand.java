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
import org.eclipse.wb.internal.swing.laf.model.LafInfo;

import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link Command} that removes {@link LafInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public final class RemoveCommand extends Command {
  // constants
  public static final String ID = "remove";
  // fields
  private final String m_id;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public RemoveCommand(LafInfo lafInfo) {
    m_id = lafInfo.getID();
  }

  public RemoveCommand(Attributes attributes) {
    m_id = attributes.getValue(ATTR_ID);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    LafInfo lafInfo = LafSupport.getLookAndFeel(m_id);
    if (lafInfo != null) {
      LafSupport.removeLookAndFeel(lafInfo);
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
  }

  @Override
  public void addToCommandList(List<Command> commands) {
    // remove other commands, these are not needed because this entry will be deleted
    for (Iterator<Command> I = commands.iterator(); I.hasNext();) {
      Command command = I.next();
      if (command instanceof LookAndFeelCommand) {
        LookAndFeelCommand lookAndFeelCommand = (LookAndFeelCommand) command;
        if (lookAndFeelCommand.m_id.equals(m_id)) {
          I.remove();
        }
      }
    }
    // do add
    commands.add(this);
  }
}
