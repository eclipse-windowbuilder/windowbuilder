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
package org.eclipse.wb.core.gef.command;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.gef.core.CompoundCommand;

/**
 * Implementation of {@link CompoundCommand} for editing {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class CompoundEditCommand extends CompoundCommand {
  private final ObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompoundEditCommand(ObjectInfo object) {
    Assert.isNotNull(object);
    m_object = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void execute() throws Exception {
    ExecutionUtils.run(m_object, new RunnableEx() {
      public void run() throws Exception {
        CompoundEditCommand.super.execute();
      }
    });
  }
}
