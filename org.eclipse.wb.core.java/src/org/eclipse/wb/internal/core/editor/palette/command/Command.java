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

import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.StringEscapeUtils;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract command for modifying {@link PaletteInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class Command {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Executes this {@link Command}.
   */
  public abstract void execute(PaletteInfo palette);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Writing
  //
  ////////////////////////////////////////////////////////////////////////////
  private StringBuffer m_stringBuffer;

  /**
   * @return the {@link String} that contains information about this {@link Command}. It will be
   *         passed back to constructor during reading.
   */
  @Override
  public final String toString() {
    m_stringBuffer = new StringBuffer();
    m_stringBuffer.append("\t<");
    // use ID as tag
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        String id = (String) Command.this.getClass().getField("ID").get(null);
        m_stringBuffer.append(id);
      }
    });
    //
    addAttributes();
    m_stringBuffer.append("/>");
    return m_stringBuffer.toString();
  }

  /**
   * Adds single attribute.
   */
  protected final void addAttribute(String name, boolean value) {
    addAttribute(name, value ? "true" : "false");
  }

  /**
   * Adds single attribute.
   */
  protected final void addAttribute(String name, String value) {
    if (value != null) {
      m_stringBuffer.append("\n\t\t");
      m_stringBuffer.append(name);
      m_stringBuffer.append("=\"");
      //
      value = StringEscapeUtils.escapeXml(value);
      {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
          char c = value.charAt(i);
          if (c < 0x20) {
            escaped.append("&#");
            escaped.append((int) c);
            escaped.append(";");
          } else {
            escaped.append(c);
          }
        }
        value = escaped.toString();
      }
      m_stringBuffer.append(value);
      //
      m_stringBuffer.append("\"");
    }
  }

  /**
   * Subclasses should implement this methods and use {@link #addAttribute(String, String)} to add
   * separate attributes.
   */
  protected abstract void addAttributes();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add command in given list, possible with some optimizations.
   */
  public void addToCommandList(List<Command> commands) {
    commands.add(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes all {@link Command}'s with given class and "id".
   */
  protected final void removeCommands(List<Command> commands,
      Class<? extends Command> commandClass,
      String id) throws Exception {
    // prepare "id" field
    Field idField = ReflectionUtils.getFieldByName(commandClass, "m_id");
    // check commands
    for (Iterator<Command> I = commands.iterator(); I.hasNext();) {
      Command command = I.next();
      if (command.getClass() == commandClass && idField.get(command).equals(id)) {
        I.remove();
      }
    }
  }
}
