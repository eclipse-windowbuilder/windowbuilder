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

import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

/**
 * Command for creating new strings source.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class CreateSourceCommand extends AbstractCommand {
  private final SourceDescription m_sourceDescription;
  private final Object m_parameters;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreateSourceCommand(IEditableSource editableSource,
      SourceDescription sourceDescription,
      Object parameters) {
    super(editableSource);
    m_sourceDescription = sourceDescription;
    m_parameters = parameters;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public SourceDescription getSourceDescription() {
    return m_sourceDescription;
  }

  public Object getParameters() {
    return m_parameters;
  }
}
