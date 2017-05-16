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

import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.util.Set;

/**
 * Command for internalizing (removing) key, i.e. replace externalized {@link Expression} with
 * {@link StringLiteral}.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class InternalizeKeyCommand extends AbstractCommand {
  private final String m_key;
  private Set<String> m_keys;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public InternalizeKeyCommand(IEditableSource editableSource, String key) {
    super(editableSource);
    m_key = key;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Set<String> getKeys() {
    if (m_keys == null) {
      m_keys = Sets.newHashSet();
      m_keys.add(m_key);
    }
    return m_keys;
  }
}
