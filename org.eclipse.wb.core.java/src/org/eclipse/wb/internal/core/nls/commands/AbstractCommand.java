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
package org.eclipse.wb.internal.core.nls.commands;

import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

import java.util.List;

/**
 * Abstract command for NLS editing.
 *
 * After several attempts with "state only" editing for NLS I've decided that command approach for
 * editing is best solution. For example we can have commands like "set values", "rename keys",
 * "add locale", "remove locale", "externalize property", "internalize property", etc.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public abstract class AbstractCommand {
  private final IEditableSource m_editableSource;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractCommand(IEditableSource editableSource) {
    m_editableSource = editableSource;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IEditableSource getEditableSource() {
    return m_editableSource;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding to queue
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add command in given list, possible with some optimizations. For example if we have already
   * "set values" for same locale on top of commands queue, we can remove previous "set values" and
   * use only current one.
   */
  public void addToCommandList(List<AbstractCommand> commands) {
    commands.add(this);
  }
}
