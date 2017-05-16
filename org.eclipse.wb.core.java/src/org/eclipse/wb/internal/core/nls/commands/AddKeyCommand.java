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

/**
 * Command for adding new key into source.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class AddKeyCommand extends AbstractCommand {
  private final String m_key;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AddKeyCommand(IEditableSource editableSource, String key) {
    super(editableSource);
    m_key = key;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getKey() {
    return m_key;
  }
}
