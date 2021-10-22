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

import org.eclipse.wb.internal.discovery.ui.Messages;
import org.eclipse.wb.internal.discovery.ui.WBDiscoveryUiPlugin;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import java.net.URL;

/**
 * A utility class for opening URLs in an external browser.
 */
public class BrowserHelper {
  /**
   * Given a URL and a shell, open the URL in an external browser.
   *
   * @param shell
   *          the current shell
   * @param url
   *          the URL to open
   */
  public static void openUrl(Shell shell, String url) {
    try {
      IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
      support.getExternalBrowser().openURL(new URL(url));
    } catch (Exception e) {
      WBDiscoveryUiPlugin.logError(e);
      MessageDialog.openError(shell, Messages.BrowserHelper_errorOpenBrowserTitle, e.getMessage());
    }
  }
}
