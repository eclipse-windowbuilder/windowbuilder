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
package org.eclipse.wb.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.gef.core.EditDomain;

/**
 * Implementation of {@link EntryInfo} that sets {@link Tool} for {@link EditDomain}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class ToolEntryInfo extends EntryInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean activate(final boolean reload) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        // prepare tool
        Tool tool;
        {
          tool = createTool();
          if (tool == null) {
            return false;
          }
          tool.setUnloadWhenFinished(!reload);
        }
        // OK
        m_editPartViewer.getEditDomain().setActiveTool(tool);
        return true;
      }
    }, false);
  }

  /**
   * @return the {@link Tool} that should be set on activation, or <code>null</code> if no
   *         {@link Tool} can be activated.
   */
  public abstract Tool createTool() throws Exception;
}
