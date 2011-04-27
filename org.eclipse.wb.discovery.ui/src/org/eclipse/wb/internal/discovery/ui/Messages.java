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
package org.eclipse.wb.internal.discovery.ui;

import org.eclipse.osgi.util.NLS;

/**
 * The I18N class for the org.eclipse.wb.discovery.ui plugin.
 */
public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.eclipse.wb.internal.discovery.ui.messages"; //$NON-NLS-1$
  public static String BrowserHelper_errorOpenBrowserTitle;
  public static String InstallToolkitWizard_errorInstalling;
  public static String InstallToolkitWizard_title;
  public static String InstallToolkitWizard_titlePattern;
  public static String InstallToolkitWizardPage_finishLabel;
  public static String InstallToolkitWizardPage_moreInfoLink;
  public static String InstallToolkitWizardPage_name;
  public static String InstallToolkitWizardPage_title;
  public static String P2Provisioner_statusInstalling;
  public static String P2Provisioner_unableInstallMessage;
  public static String P2Provisioner_unableInstallTitle;
  public static String ToolkitControl_installedStatus;
  public static String ToolkitControl_moreInfoLink;
  public static String ToolkitsPreferencePage_additionalToolkitsLabel;
  public static String ToolkitsPreferencePage_checkForUpdates;
  public static String ToolkitsPreferencePage_errorInstalling;
  public static String ToolkitsPreferencePage_errorUninstalling;
  public static String ToolkitsPreferencePage_installButton;
  public static String ToolkitsPreferencePage_showUninstalled;
  public static String ToolkitsPreferencePage_uninstallButton;
  public static String WizardToolkitUtils_descriptionPattern;
  public static String WizardToolkitUtils_titlePattern;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
