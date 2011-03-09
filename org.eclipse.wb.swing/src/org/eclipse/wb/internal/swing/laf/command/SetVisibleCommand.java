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
import org.eclipse.wb.internal.swing.laf.model.LafEntryInfo;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;

import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.List;

/**
 * {@link Command} changing {@link LafEntryInfo} "visible" property.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public final class SetVisibleCommand extends Command {
  // constants
  private static final String ATTR_VISIBLE = "visible";
  public static final String ID = "set-visible";
  // fields
  private final String m_id;
  private final boolean m_visible;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetVisibleCommand(LafEntryInfo element, boolean visible) {
    m_id = element.getID();
    m_visible = visible;
  }

  public SetVisibleCommand(Attributes attributes) {
    m_id = attributes.getValue(ATTR_ID);
    m_visible = ATTR_VALUE_TRUE.equals(attributes.getValue(ATTR_VISIBLE));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    // try category
    {
      CategoryInfo category = LafSupport.getCategory(m_id);
      if (category != null) {
        category.setVisible(m_visible);
        return;
      }
    }
    // try LAF
    {
      LafInfo lafInfo = LafSupport.getLookAndFeel(m_id);
      if (lafInfo != null) {
        lafInfo.setVisible(m_visible);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes(XmlWriter writer) {
    addAttribute(writer, ATTR_ID, m_id);
    addAttribute(writer, ATTR_VISIBLE, m_visible);
  }

  @Override
  public void addToCommandList(List<Command> commands) {
    for (Iterator<Command> I = commands.iterator(); I.hasNext();) {
      Command command = I.next();
      if (command instanceof SetVisibleCommand) {
        SetVisibleCommand svCommand = (SetVisibleCommand) command;
        if (svCommand.m_id.equals(m_id)) {
          I.remove();
        }
      }
    }
    commands.add(this);
  }
}
