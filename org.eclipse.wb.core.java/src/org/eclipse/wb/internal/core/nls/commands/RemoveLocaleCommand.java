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
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;

import java.util.List;

/**
 * Command for removing locale.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class RemoveLocaleCommand extends AbstractCommand {
  private final LocaleInfo m_locale;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RemoveLocaleCommand(IEditableSource editableSource, LocaleInfo locale) {
    super(editableSource);
    m_locale = locale;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public LocaleInfo getLocale() {
    return m_locale;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding to queue
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Do optimizations for "remove locale" command. Remove any existing "set values" and "add locale"
   * for same locale that existing before (if we don't have other commands on this path).
   */
  @Override
  public void addToCommandList(List<AbstractCommand> m_commands) {
    LocaleInfo locale = getLocale();
    for (int index = m_commands.size() - 1; index >= 0; index--) {
      AbstractCommand oldCommand = m_commands.get(index);
      // "set values" found
      if (oldCommand instanceof SetValuesCommand) {
        SetValuesCommand oldSetValuesCommand = (SetValuesCommand) oldCommand;
        // if same locale, remove it
        if (oldSetValuesCommand.getLocale().equals(locale)) {
          m_commands.remove(index);
        }
        // keep looking
        continue;
      }
      // "add locale" found
      if (oldCommand instanceof AddLocaleCommand) {
        AddLocaleCommand oldAddLocaleCommand = (AddLocaleCommand) oldCommand;
        // if same locale, remove it
        if (oldAddLocaleCommand.getLocale().equals(locale)) {
          m_commands.remove(index);
          // after removing "add locale" we can not expect any commands for this locale ;-)
          return;
        }
        // keep looking
        continue;
      }
      // if we found any other command, we can not change anything before it, so stop looking
      break;
    }
    //
    super.addToCommandList(m_commands);
  }
}
