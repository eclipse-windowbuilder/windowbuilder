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
package org.eclipse.wb.internal.xwt.model.property.editor.font;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.graphics.Font;

/**
 * Information object about {@link Font}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public final class FontInfo {
  private final Font m_font;
  private final boolean m_doDispose;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FontInfo(Font font, boolean doDispose) {
    m_font = font;
    m_doDispose = doDispose;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dispose
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        if (m_doDispose) {
          ReflectionUtils.invokeMethod(m_font, "dispose()");
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the inner {@link Font}.
   */
  public Font getFont() {
    return m_font;
  }
}