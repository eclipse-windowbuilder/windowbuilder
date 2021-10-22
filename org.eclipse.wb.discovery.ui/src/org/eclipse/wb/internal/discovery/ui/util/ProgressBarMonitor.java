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
package org.eclipse.wb.internal.discovery.ui.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * Wraps a SWT ProgressBar control in an IProgressMonitor interface.
 */
public class ProgressBarMonitor implements IProgressMonitor {
  private ProgressBar progressBar;
  private boolean cancelled;

  /**
   * Create a new ProgressBarMonitor.
   *
   * @param progressBar
   *          the ProgressBar control
   */
  public ProgressBarMonitor(ProgressBar progressBar) {
    this.progressBar = progressBar;
  }

  public void beginTask(String name, int totalWork) {
    cancelled = false;
    if (!progressBar.isDisposed()) {
      progressBar.setSelection(0);
      progressBar.setMinimum(0);
      progressBar.setMaximum(totalWork);
      progressBar.setVisible(true);
    }
  }

  public void setTaskName(String name) {
    // nothing to do
  }

  public void subTask(String name) {
    // nothing to do
  }

  public void internalWorked(double work) {
  }

  public void worked(int work) {
    if (!progressBar.isDisposed()) {
      progressBar.setSelection(progressBar.getSelection() + work);
    }
  }

  public boolean isCanceled() {
    return cancelled;
  }

  public void setCanceled(boolean value) {
    cancelled = value;
  }

  public void done() {
    if (!progressBar.isDisposed()) {
      progressBar.setVisible(false);
    }
  }
}
