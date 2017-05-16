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
package org.eclipse.wb.internal.gef.core;

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.Command;

import java.util.List;

/**
 * An aggregation of multiple <code>{@link Command}s</code>. A {@link CompoundCommand} can be
 * {@link #unwrap() unwrapped}. Unwrapping returns the simplest equivalent form of the
 * {@link CompoundCommand}. So, if a {@link CompoundCommand} contains just one {@link Command}, that
 * {@link Command} is returned.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class CompoundCommand extends Command {
  private final List<Command> m_commands = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds the specified {@link Command} if it is not <code>null</code>.
   */
  public void add(Command command) {
    if (command != null) {
      m_commands.add(command);
    }
  }

  /**
   * Returns the {@link List} of contained {@link Command}s.
   */
  public List<Command> getCommands() {
    return m_commands;
  }

  /**
   * Returns the number of contained {@link Command}s.
   */
  public int size() {
    return m_commands.size();
  }

  /**
   * Returns <code>true</code> if the {@link CompoundCommand} is empty.
   */
  public boolean isEmpty() {
    return m_commands.isEmpty();
  }

  /**
   * Returns the simplest form of this {@link Command} that is equivalent. This is useful for
   * removing unnecessary nesting of {@link Command}s.
   */
  public Command unwrap() {
    switch (size()) {
      case 0 :
        return null;
      case 1 :
        return m_commands.get(0);
      default :
        return this;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ICommand
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() throws Exception {
    for (Command command : m_commands) {
      command.execute();
    }
  }
}