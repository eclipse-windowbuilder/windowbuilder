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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

/**
 * Command for externalizing property.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class ExternalizePropertyCommand extends AbstractCommand {
  private final JavaInfo m_component;
  private final GenericProperty m_property;
  private final String m_key;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExternalizePropertyCommand(IEditableSource editableSource,
      JavaInfo component,
      GenericProperty property,
      String key) {
    super(editableSource);
    m_component = component;
    m_property = property;
    m_key = key;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfo getComponent() {
    return m_component;
  }

  public GenericProperty getProperty() {
    return m_property;
  }

  public String getKey() {
    return m_key;
  }
}
