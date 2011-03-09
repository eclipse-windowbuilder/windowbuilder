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
import org.eclipse.wb.internal.swing.laf.model.LafInfo;

import org.xml.sax.Attributes;

/**
 * Abstract {@link Command} that works with {@link LafInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public abstract class LookAndFeelCommand extends Command {
  protected final String m_id;
  protected final String m_name;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public LookAndFeelCommand(String id, String name) {
    m_id = id;
    m_name = name;
  }

  public LookAndFeelCommand(Attributes attributes) {
    m_id = attributes.getValue(ATTR_ID);
    m_name = attributes.getValue(ATTR_NAME);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes(XmlWriter writer) {
    addAttribute(writer, ATTR_ID, m_id);
    addAttribute(writer, ATTR_NAME, m_name);
  }
}
