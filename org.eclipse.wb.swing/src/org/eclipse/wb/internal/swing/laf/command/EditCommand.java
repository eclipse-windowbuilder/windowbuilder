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
import org.eclipse.wb.internal.swing.laf.model.UserDefinedLafInfo;

import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link Command} that edits {@link UserDefinedLafInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public class EditCommand extends EditNameCommand {
  // constants
  public static final String ID = "edit";
  private static final String ATTR_JAR_PATH = "jar-path";
  private static final String ATTR_CLASS_NAME = "class-name";
  // fields
  protected final String m_className;
  protected final String m_jarName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditCommand(String id, String name, String className, String jarName) {
    super(id, name);
    m_className = className;
    m_jarName = jarName;
  }

  public EditCommand(Attributes attributes) {
    super(attributes);
    m_className = attributes.getValue(ATTR_CLASS_NAME);
    m_jarName = attributes.getValue(ATTR_JAR_PATH);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    super.execute();
    UserDefinedLafInfo lafInfo = (UserDefinedLafInfo) LafSupport.getLookAndFeel(m_id);
    if (lafInfo != null) {
      lafInfo.setClassName(m_className);
      lafInfo.setJarFile(m_jarName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addToCommandList(List<Command> commands) {
    // remove other edit commands for this
    for (Iterator<Command> I = commands.iterator(); I.hasNext();) {
      Command command = I.next();
      if (command instanceof EditCommand) {
        EditCommand editCommand = (EditCommand) command;
        if (editCommand.m_id.equals(m_id)) {
          I.remove();
        }
      }
    }
    // do add
    commands.add(this);
  }

  @Override
  protected void addAttributes(XmlWriter writer) {
    super.addAttributes(writer);
    addAttribute(writer, ATTR_CLASS_NAME, m_className);
    addAttribute(writer, ATTR_JAR_PATH, m_jarName);
  }
}
