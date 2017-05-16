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
package org.eclipse.wb.internal.core.utils.execution;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * No-op implementation of {@link IProgressMonitor}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class NoOpProgressMonitor implements IProgressMonitor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Task
  //
  ////////////////////////////////////////////////////////////////////////////
  public void beginTask(String name, int totalWork) {
  }

  public void done() {
  }

  public void setTaskName(String name) {
  }

  public void subTask(String name) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canceled
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_canceled;

  public boolean isCanceled() {
    return m_canceled;
  }

  public void setCanceled(boolean value) {
    m_canceled = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Worked
  //
  ////////////////////////////////////////////////////////////////////////////
  public void internalWorked(double work) {
  }

  public void worked(int work) {
  }
}
