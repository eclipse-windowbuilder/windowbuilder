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
import java.util.Map;

/**
 * Command for adding new locale with some initial values.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class AddLocaleCommand extends AbstractCommand {
  private final LocaleInfo m_locale;
  private final Map<String, String> m_values;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AddLocaleCommand(IEditableSource editableSource,
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
}
