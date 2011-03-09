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
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;

import java.util.Collection;

/**
 * A utility class to work with IStatus.
 */
public class StatusUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constants
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IStatus OK_STATUS = new Status(IStatus.OK,
      DesignerPlugin.PLUGIN_ID,
      IStatus.OK,
      "",
      null);
  public static final IStatus ERROR_STATUS = new Status(IStatus.ERROR,
      DesignerPlugin.PLUGIN_ID,
      IStatus.ERROR,
      "",
      null);

  public static IStatus createError(String message) {
    return new Status(IStatus.ERROR, DesignerPlugin.PLUGIN_ID, IStatus.ERROR, message, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compare
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Compares two instances of <code>IStatus</code>. The more severe is returned: an error is more
   * severe than a warning, and a warning is more severe than ok. If the two statuses have the same
   * severity, the second is returned.
   */
  public static IStatus getMoreSevere(IStatus s1, IStatus s2) {
    if (s1.getSeverity() > s2.getSeverity()) {
      return s1;
    } else {
      return s2;
    }
  }

  /**
   * Finds the most severe status from collection of {@link IStatus}.
   */
  public static IStatus getMostSevere(Collection<IStatus> statusCollection) {
    IStatus max = null;
    for (IStatus status : statusCollection) {
      if (status.matches(IStatus.ERROR)) {
        return status;
      }
      if (max == null || status.getSeverity() > max.getSeverity()) {
        max = status;
      }
    }
    return max;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Applies the status to the status line of a dialog page.
   */
  public static void applyToStatusLine(DialogPage page, IStatus status) {
    String message = status.getMessage();
    switch (status.getSeverity()) {
      case IStatus.OK :
        page.setMessage(message, IMessageProvider.NONE);
        page.setErrorMessage(null);
        break;
      case IStatus.WARNING :
        page.setMessage(message, IMessageProvider.WARNING);
        page.setErrorMessage(null);
        break;
      case IStatus.INFO :
        page.setMessage(message, IMessageProvider.INFORMATION);
        page.setErrorMessage(null);
        break;
      default :
        if (message.length() == 0) {
          message = null;
        }
        page.setMessage(null);
        page.setErrorMessage(message);
        break;
    }
  }

  /**
   * Applies the status to the title area of a dialog.
   */
  public static void applyToTitleAreaDialog(TitleAreaDialog dialog, IStatus status, String okMessage) {
    String message = status.getMessage();
    switch (status.getSeverity()) {
      case IStatus.OK :
        dialog.setMessage(okMessage);
        dialog.setErrorMessage(null);
        break;
      case IStatus.INFO :
        dialog.setMessage(message, IMessageProvider.INFORMATION);
        dialog.setErrorMessage(null);
        break;
      case IStatus.WARNING :
        dialog.setMessage(message, IMessageProvider.WARNING);
        dialog.setErrorMessage(null);
        break;
      case IStatus.ERROR :
        if (message.length() == 0) {
          message = null;
        }
        dialog.setMessage(null);
        dialog.setErrorMessage(message);
        break;
    }
  }
}
