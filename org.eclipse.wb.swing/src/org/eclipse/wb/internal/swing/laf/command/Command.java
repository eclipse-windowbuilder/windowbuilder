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
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Base class for LAF managing commands.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public abstract class Command {
  // constants
  protected static final String ATTR_ID = "id";
  protected static final String ATTR_NAME = "name";
  protected static final String ATTR_VALUE_TRUE = "true";
  protected static final String ATTR_VALUE_FALSE = "false";

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
  /**
   * @return the {@link String} that contains information about this {@link Command}. It will be
   *         passed back to constructor during reading.
   */
  public void write(XmlWriter xmlWriter) {
    try {
      String id = (String) getClass().getField("ID").get(null);
      xmlWriter.beginTag(id);
      addAttributes(xmlWriter);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    } finally {
      xmlWriter.closeTag();
    }
  }

  /**
   * Adds single attribute.
   */
  protected final void addAttribute(XmlWriter xmlWriter, String name, boolean value) {
    addAttribute(xmlWriter, name, value ? ATTR_VALUE_TRUE : ATTR_VALUE_FALSE);
  }

  /**
   * Adds single attribute.
   */
  protected final void addAttribute(XmlWriter xmlWriter, String name, String value) {
    if (value != null) {
      xmlWriter.writeAttribute(name, value);
    }
  }

  /**
   * Subclasses should implement this methods and use {@link #addAttribute(String, String)} to add
   * separate attributes.
   */
  protected abstract void addAttributes(XmlWriter xmlWriter);

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
