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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

import java.util.List;
import java.util.Map;

/**
 * Command for renaming key.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class RenameKeyCommand extends AbstractCommand {
  private final String m_oldKey;
  private final String m_newKey;
  private Map<String, String> m_oldToNewMap;
  private Map<String, String> m_newToOldMap;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RenameKeyCommand(IEditableSource editableSource, String oldKey, String newKey) {
    super(editableSource);
    m_oldKey = oldKey;
    m_newKey = newKey;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Map<String, String> getOldToNewMap() {
    if (m_oldToNewMap == null) {
      m_oldToNewMap = Maps.newHashMap();
      m_oldToNewMap.put(m_oldKey, m_newKey);
    }
    return m_oldToNewMap;
  }

  private Map<String, String> getNewToOldMap() {
    if (m_newToOldMap == null) {
      m_newToOldMap = Maps.newHashMap();
      m_newToOldMap.put(m_newKey, m_oldKey);
    }
    return m_newToOldMap;
  }

  private void merge(RenameKeyCommand oldCommand) {
    m_oldToNewMap = oldCommand.getOldToNewMap();
    m_newToOldMap = oldCommand.getNewToOldMap();
    // prepare "original" key for "old" key
    String oriKey;
    {
      // if we already renamed "very old" into "old", we can get "original"
      oriKey = m_newToOldMap.remove(m_oldKey);
      // no, this is first time when we rename "old", so use it as "original"
      if (oriKey == null) {
        oriKey = m_oldKey;
      }
    }
    // update map's
    m_oldToNewMap.put(oriKey, m_newKey);
    m_newToOldMap.put(m_newKey, oriKey);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding to queue
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Merge with any existing {@link RenameKeyCommand} for same source (if we don't have other
   * commands on this path).
   */
  @Override
  public void addToCommandList(List<AbstractCommand> commands) {
    for (int index = commands.size() - 1; index >= 0; index--) {
      AbstractCommand oldCommand = commands.get(index);
      // "set values" found
      if (oldCommand instanceof SetValuesCommand) {
        continue;
      }
      // "rename key" found
      if (oldCommand instanceof RenameKeyCommand) {
        RenameKeyCommand oldRenameKeyCommand = (RenameKeyCommand) oldCommand;
        // merge with "rename key" for same source
        if (oldRenameKeyCommand.getEditableSource() == getEditableSource()) {
          merge(oldRenameKeyCommand);
          commands.remove(index);
        }
        // keep looking
        continue;
      }
      // if we found any other command, we can not change anything before it, so stop looking
      break;
    }
    //
    super.addToCommandList(commands);
  }
}
