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
package org.eclipse.wb.internal.ercp.devices.command;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;

/**
 * Abstract command for modifying devices.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
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
  public abstract void execute();

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
    try {
      String id = (String) getClass().getField("ID").get(null);
      m_stringBuffer.append(id);
    } catch (Throwable e) {
      throw new Error(e);
    }
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
        String escaped = "";
        for (int i = 0; i < value.length(); i++) {
          char c = value.charAt(i);
          if (c < 0x20) {
            escaped += "&#" + (int) c + ";";
          } else {
            escaped += c;
          }
        }
        value = escaped;
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
  protected void addAttributes() {
  }

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
}
