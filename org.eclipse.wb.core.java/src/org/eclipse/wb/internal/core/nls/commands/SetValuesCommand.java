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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command for changing values for full locale.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class SetValuesCommand extends AbstractCommand {
  private final LocaleInfo m_locale;
  private final Map<String, String> m_values;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetValuesCommand(IEditableSource editableSource,
      LocaleInfo locale,
      Map<String, String> values) {
    super(editableSource);
    m_locale = locale;
    m_values = new HashMap<String, String>(values);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public LocaleInfo getLocale() {
    return m_locale;
  }

  public Map<String, String> getValues() {
    return m_values;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding to queue
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Remove any existing "set values" for same locale that existing before (if we don't have other
   * commands on this path).
   */
  @Override
  public void addToCommandList(List<AbstractCommand> commands) {
    LocaleInfo locale = getLocale();
    for (int index = commands.size() - 1; index >= 0; index--) {
      AbstractCommand oldCommand = commands.get(index);
      // "set values" found
      if (oldCommand instanceof SetValuesCommand) {
        SetValuesCommand oldSetValuesCommand = (SetValuesCommand) oldCommand;
        // if same locale, remove it
        if (oldSetValuesCommand.getLocale().equals(locale)) {
          commands.remove(index);
          // stop, we can not have more than one "set values" to remove
          break;
        }
        // keep looking
        continue;
      }
      // "rename key" found
      if (oldCommand instanceof RenameKeyCommand) {
        // skip it, "rename key" will replace key in Java source and "set values" will change bundle
        continue;
      }
      // "externalize property" found
      if (oldCommand instanceof ExternalizePropertyCommand) {
        // skip it, "externalize property" will replace direct value in Java source with externalized expression
        // and "set values" will change bundle
        continue;
      }
      // if we found any other command, we can not change anything before it, so stop looking
      break;
    }
    //
    super.addToCommandList(commands);
  }
}
